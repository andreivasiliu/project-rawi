package rawi.MainServer2;

import java.rmi.RemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import rawi.RMIMainServer;
import rawi.common.ClusterComputerInterface;
import rawi.common.ClusterComputerStatus;
import rawi.common.Command;
import rawi.common.FileHandle;
import rawi.common.Notifier;
import rawi.common.Ports;
import rawi.common.Task;
import rawi.common.TaskResult;
import rawi.mainserver.ClusterManager;
import rawi.mainserver.WorkSession;
import rawi.rmiinfrastructure.RMIClientModel;

public class ClusterManagerTest
{
    ClusterManager clusterManager;
    Thread managerThread;

    @Before
    public void setUp() throws Exception
    {
        clusterManager = new ClusterManager();
        managerThread = new Thread(clusterManager);

        managerThread.start();
    }

    @After
    public void tearDown() throws Exception
    {
        clusterManager.shutdown();
        managerThread.join();
    }

    @Test
    public void testCreationAndDestruction()
    {
        DummyWorkSession dummySession =
                new DummyWorkSession(0);
        clusterManager.addWorkSession((WorkSession) dummySession);
    }

    @Test
    public void testStartAndWait()
    {
        DummyWorkSession dummySession =
                new DummyWorkSession(0);
        clusterManager.addWorkSession((WorkSession) dummySession);
        clusterManager.startWorkSession(dummySession.getSessionId());
        clusterManager.waitUntilStopped(dummySession.getSessionId());
    }

    @Test
    public void testManagerWithRealClusterComputer()
            throws RemoteException, InterruptedException
    {
        final String clusterComputerIP = "127.0.0.1";

        if (!checkConnection(clusterComputerIP))
        {
            System.out.println("Skipping ClusterComputer test, there wasn't " +
                    "one running at " + clusterComputerIP);
            return;
        }
        
        System.out.println("Found a ClusterComputer at " + clusterComputerIP +
                ", using it for a test.");

        Notifier notifier = new Notifier("MainServer");
        notifier.start();
        RMIMainServer rmiServer = new RMIMainServer(clusterManager);
        clusterManager.addIpToScan(clusterComputerIP);

        try
        {
            DummyWorkSession dummySession = new DummyWorkSession(3);
            clusterManager.addWorkSession(dummySession);
            clusterManager.startWorkSession(dummySession.getSessionId());
            clusterManager.waitUntilStopped(dummySession.getSessionId());
        }
        finally
        {
            notifier.shutdown();
            notifier.join();
            rmiServer.shutdownRMI();
        }

        System.out.println("ClusterComputer test ended successfully.");
    }

    // <editor-fold defaultstate="collapsed" desc="Dummy Work Session private class">
    private class DummyWorkSession extends WorkSession
    {
        Queue<Task> dummyTasks = new ConcurrentLinkedQueue<Task>();
        Queue<Task> activeTasks = new ConcurrentLinkedQueue<Task>();

        public DummyWorkSession(int tasks)
        {
            super("dummy-test-session", new rawi.mainserver.TransformationModel());

            for (int i = 0; i < tasks; i++)
            {
                List l = new LinkedList<FileHandle>();
                Command c = new Command("calc.exe");
                c.setSystemCommand(true);
                Task t = new Task("dummy-task-" + i, l, c);
                t.setDownloadURI("");
                t.setUploadURI("");
                dummyTasks.add(t);
            }
        }

        @Override
        public Task getPendingTask()
        {
            Task task = dummyTasks.poll();

            if (task != null)
            {
                System.out.println("DummyWorkSession: Sent him a task.");
                activeTasks.add(task);
            }

            return task;
        }

        @Override
        public void returnTask(Task task)
        {
            System.out.println("DummyWorkSession: Task was returned.");
            dummyTasks.add(task);
        }

        @Override
        public void markTaskAsFinished(Task task, TaskResult taskResult)
        {
            System.out.println("DummyWorkSession: Finished a task!");
            activeTasks.remove(task);
            if (activeTasks.isEmpty() && dummyTasks.isEmpty())
                stopSession();
        }

        @Override
        public synchronized void startSession()
        {
            if (dummyTasks.isEmpty())
                return;

            super.startSession();
        }
    }// </editor-fold>

    private boolean checkConnection(String IP)
    {
        try
        {
            ClusterComputerInterface cci =
                    new RMIClientModel<ClusterComputerInterface>(IP,
                    Ports.ClusterComputerPort).getInterface();

            ClusterComputerStatus ccs = cci.getStatus();

            if (ccs.processors > 0)
                return true;

            return false;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
}