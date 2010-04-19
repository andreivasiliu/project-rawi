package clustercomputer;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.server.ServerNotActiveException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.ClusterComputerStatus;
import rawi.common.Task;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import rawi.common.ClusterComputerInterface;
import rawi.common.FileHandle;
import rawi.common.MainServerInterface;
import rawi.common.NetworkUtils;
import rawi.common.Notifier;
import rawi.common.Ports;
import rawi.common.TaskStatus;
import rawi.rmiinfrastructure.RMIClientModel;
import rawi.rmiinfrastructure.RMIServerModel;

/**
 *
 * @author andrei.arusoaie
 */
public class ClusterComputer extends RMIServerModel implements ClusterComputerInterface
{

    String uuid = UUID.randomUUID().toString();
    BlockingQueue<ClusterTask> stage1Queue = new LinkedBlockingQueue<ClusterTask>();
    BlockingQueue<ClusterTask> stage2Queue = new LinkedBlockingQueue<ClusterTask>();
    BlockingQueue<ClusterTask> stage3Queue = new LinkedBlockingQueue<ClusterTask>();
    final Map<String, TaskStatus> statusOfTask = new HashMap<String, TaskStatus>();
    ClusterCache cache;
    HashMap<String, Date> recentServerRequests = new HashMap<String, Date>();

    public ClusterComputer() throws RemoteException
    {
        super(Ports.ClusterComputerPort);

        // start all needed threads
        //start download thread
        Download d = new Download();
        d.start();

        //start execution threads for each processor
        int proc = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < proc; i++)
        {
            Execute exec = new Execute();
            exec.start();
        }

        //start upload thread
        Upload u = new Upload();
        u.start();

        //clear cache and add finalizer
        ClusterTask.deleteDir(new File("cache"));
        cache = new ClusterCache();
        Runtime.getRuntime().addShutdownHook(new Finalizer(this));

        Collection<String> mainServerIpList = NetworkUtils.getIPsFromTracker("MainServer");

        for (String serverIP : mainServerIpList)
        {
            MainServerNotification msn = new MainServerNotification(serverIP);
            msn.start();
        }

        new Notifier("ClusterComputer").start();
    }

    /**
     * This function will be called by a RMI Client.
     * It executes the command, having the task arguments.
     * @param task
     * @param command
     * @throws IOException
     */
    public void execute(Task task) throws RemoteException
    {
        System.out.println("Received task: " + task.getId());
        System.out.println("Upload URL: " + task.getUploadURI());
        System.out.print("Command:");
        String[] cmdArray = task.getCommand().getCommandArray();
        for (int i = 0; i < cmdArray.length; i++)
        {
            System.out.print(" " + cmdArray[i]);
        }
        System.out.println();

        System.out.println("Files to download:");
        for (FileHandle file : task.getFiles())
        {
            System.out.println(" - " + file.getLogicalName() + " (" + file.getFileURL() + ")");
        }

        ClusterTask ctask = new ClusterTask(this, task, cache);

        synchronized (statusOfTask)
        {
            statusOfTask.put(task.getId(), new TaskStatus(
                    TaskStatus.StatusType.RUNNING));
        }

        stage1Queue.add(ctask);
        //tt.start();

    }

    public void registerIP(String ip)
    {
        // new Date() returns the current time.
        recentServerRequests.put(ip, new Date());
        updateIPs();
    }

    public void updateIPs()
    {
        // Remove entries older than five minutes.
        final long fiveMinutes = 300000;
        HashMap<String, Date> recentServerRequestsClone =
                (HashMap<String, Date>)recentServerRequests.clone();

        for(String s: recentServerRequestsClone.keySet())
            if (recentServerRequestsClone.get(s).getTime() -
                    new Date().getTime() > fiveMinutes)
            {
                recentServerRequests.remove(s);
            }
    }

    public ClusterComputerStatus getStatus() throws RemoteException
    {
        try
        {
            System.out.println("Received a getStatus request from "
                    + RemoteServer.getClientHost());
            ClusterComputerStatus status = new ClusterComputerStatus();
            status.id = uuid;
            status.processors = Runtime.getRuntime().availableProcessors();
            status.mainServerAddr = RemoteServer.getClientHost();
            registerIP(status.mainServerAddr);
            return status;
        } catch (ServerNotActiveException ex)
        {
            throw new RuntimeException("Cannot get the client's host address", ex);
        }
    }

    public TaskStatus taskStatus(Task task) throws RemoteException
    {
        synchronized (statusOfTask)
        {
            if (!statusOfTask.containsKey(task.getId()))
                return new TaskStatus(TaskStatus.StatusType.INEXISTENT);
            else
                return statusOfTask.get(task.getId());
        }
    }

    class Download extends Thread
    {

        public Download()
        {
            super("Download");
        }

        @Override
        public void run()
        {
            while (true)
            {
                String address = "";
                String taskId = "";
                try
                {
                    // get the task from queue
                    ClusterTask ctask = stage1Queue.take();
                    address = ctask.task.getMainServerAddress();
                    taskId = ctask.task.getId();
                    // jobs
                    System.out.println("Task: " + ctask.task.getId() + " - begining download.");
                    ctask.createCurrentDir();
                    ctask.downloadFiles();
                    ctask.mapFiles();
                    System.out.println("Task: " + ctask.task.getId() + " - finished download.");

                    // send task to next stage
                    stage2Queue.add(ctask);
                } catch (NoSuchAlgorithmException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                } catch (IOException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                }
            }
        }

    }

    class Execute extends Thread
    {

        public Execute()
        {
            super("Execute");
        }

        @Override
        public void run()
        {

            String address = "";
            String taskId = "";

            while (true)
            {
                try
                {
                    // get the task from stage 2 queue
                    ClusterTask ctask = stage2Queue.take();
                    address = ctask.task.getMainServerAddress();
                    taskId = ctask.task.getId();

                    // jobs
                    System.out.println("Task: " + ctask.task.getId() + " - begining execution.");
                    ctask.exec();
                    System.out.println("Task: " + ctask.task.getId() + " - finished execution.");

                    // send to next stage
                    stage3Queue.add(ctask);

                } catch (IOException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                }
            }
        }

    }

    class Upload extends Thread
    {

        public Upload()
        {
            super("Upload");
        }

        @Override
        public void run()
        {
            String address = "";
            String taskId = "";

            while (true)
            {
                try
                {
                    //get task from stage 3
                    ClusterTask ctask = stage3Queue.take();
                    address = ctask.task.getMainServerAddress();
                    taskId = ctask.task.getId();

                    //jobs
                    System.out.println("Task: " + ctask.task.getId() + " - begining upload.");
                    //Uploading files
                    List<FileHandle> uploaded = ctask.uploadFiles();
                    //delete current folder
                    ctask.deleteCurrentDir();
                    //Notification
                    System.out.println("Finished. Sending results to " + ctask.task.getMainServerAddress());
                    getServer(ctask.task.getMainServerAddress()).taskCompleted(ctask.task.getId(), ctask.clusterComputer.uuid, uploaded);
                    System.out.println("Task: " + ctask.task.getId() + " - finished upload.");

                    //                    }
                    // task completed.
                    synchronized (statusOfTask)
                    {
                        statusOfTask.put(taskId, new TaskStatus(
                                TaskStatus.StatusType.COMPLETED));
                    }
                } catch (NoSuchAlgorithmException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                } catch (RemoteException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                } catch (IOException ex)
                {
                    Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
                    fail(address, taskId, ex, true);
                }
            }
        }

    }

    public MainServerInterface getServer(String address)
    {
        try
        {
            return new RMIClientModel<MainServerInterface>(address, Ports.MainServerPort).getInterface();
        } catch (RemoteException ex)
        {
            Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex)
        {
            Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void fail(String address, String taskId, Throwable ex, boolean impossibleTask)
    {
        try
        {
            getServer(address).taskFailed(taskId, uuid, ex, impossibleTask);
        } catch (RemoteException ex1)
        {
            Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex1);
        }

        synchronized (statusOfTask)
        {
            statusOfTask.put(taskId, new TaskStatus(
                    TaskStatus.StatusType.FAILED));
        }
    }

}
