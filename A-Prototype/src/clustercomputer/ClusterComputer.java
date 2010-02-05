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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import rawi.common.ClusterComputerInterface;
import rawi.common.FileHandle;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
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
    ClusterCache cache = new ClusterCache();

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

        //add finalizer
        Runtime.getRuntime().addShutdownHook(new Finalizer());
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
        stage1Queue.add(ctask);
        //tt.start();

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
            return status;
        } catch (ServerNotActiveException ex)
        {
            throw new RuntimeException("Cannot get the client's host address", ex);
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
    }

    private void deleteCache()
    {
        ClusterTask.deleteDir(new File("cache"));
    }

}
