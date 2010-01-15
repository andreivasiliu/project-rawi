package rawi.MainServer2;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
import rawi.mainserver.TransformationModel;
import rawi.mainserver.TransformationModel.Pack;
import rawi.mainserver.TransformationModel.PackTransformer;
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
            throws RemoteException, InterruptedException, IOException
    {
        final String clusterComputerIP = "127.1";
        //System.setProperty("java.rmi.server.hostname", "5.146.43.252");

        if (!checkConnection(clusterComputerIP))
        {
            System.out.println("Skipping ClusterComputer test, there wasn't "
                    + "one running at " + clusterComputerIP);
            return;
        }

        System.out.println("Found a ClusterComputer at " + clusterComputerIP
                + ", using it for a test.");

        Notifier notifier = new Notifier("MainServer");
        notifier.start();
        RMIMainServer rmiServer = new RMIMainServer(clusterManager);
        clusterManager.addIpToScan(clusterComputerIP);

        try
        {
            WorkSession session = RaytracerWorkSession.getRaytracerWorkSession();

            clusterManager.addWorkSession(session);
            clusterManager.startWorkSession(session.getSessionId());
            clusterManager.waitUntilStopped(session.getSessionId());
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
                /*                List l = new LinkedList<FileHandle>();
                Command c = new Command("calc.exe");
                c.setSystemCommand(true);
                Task t = new Task("dummy-task-" + i, l, c);
                t.setDownloadURI("");
                t.setUploadURI("");
                dummyTasks.add(t);

                 */
                Command command = new Command(new String[] {"mycp.exe", "DecorateMessageInterface.java", "dec.txt"});
                String uploadURI = "http://localhost:8084/I-Prototype/TheUploadServlet/0/";
                String downloadURI = "http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/";

                ArrayList<FileHandle> fhl = new ArrayList<FileHandle>();
                fhl.add(new FileHandle(downloadURI, null, "mycp.exe"));
                fhl.add(new FileHandle(downloadURI, null, "DecorateMessageInterface.java"));
                Task task1 = new Task("dummy-task-" + i, fhl, command, uploadURI, downloadURI, null);

                dummyTasks.add(task1);

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
            {
                stopSession();
            }
        }

        @Override
        public synchronized void startSession()
        {
            if (dummyTasks.isEmpty())
            {
                return;
            }

            super.startSession();
        }

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Raytracer Work Session">
    private static class RaytracerWorkSession
    {
        public static TransformationModel getRaytracerModel()
        {
            TransformationModel model;

            model = new TransformationModel();
            Pack[] pack = new Pack[5];
            PackTransformer[] packTransformer = new PackTransformer[3];

            pack[0] = model.addPackNode();
            pack[0].setName("SceneFile");
            pack[0].setPattern(Pattern.compile("cod\\.cad"));

            pack[1] = model.addPackNode();
            pack[1].setName("SectionFile");
            pack[1].setIsSplitter(true);
            pack[1].setPattern(Pattern.compile("section\\..rd"), true);

            pack[2] = model.addPackNode();
            pack[2].setName("PartialResult");
            pack[2].setPattern(Pattern.compile(".*\\.(bmp|png)"));

            pack[3] = model.addPackNode();
            pack[3].setName("End Node");
            pack[3].setPattern(Pattern.compile(".*\\.(bmp|png)"));

            pack[4] = model.addPackNode();
            pack[4].setName("binaries");

            packTransformer[0] = model.addPackTransformerNode();
            packTransformer[0].setCommand(new Command("Splitter 8 1234 1234"));
            model.addOutput(pack[0], packTransformer[0]);
            model.addOutput(packTransformer[0], pack[1]);

            packTransformer[1] = model.addPackTransformerNode();
            packTransformer[1].setCommand(new Command("TinyRaytracer $SceneFile $SectionFile"));
            model.addOutput(pack[0], packTransformer[1]);
            model.addOutput(pack[1], packTransformer[1]);
            model.addOutput(packTransformer[1], pack[2]);

            packTransformer[2] = model.addPackTransformerNode();
            packTransformer[2].setCommand(new Command("Joiner $SectionFile"));
            packTransformer[2].setIsJoiner(true);
            model.addOutput(pack[1], packTransformer[2]);
            model.addOutput(pack[2], packTransformer[2]);
            model.addOutput(packTransformer[2], pack[3]);

            return model;
        }

        public static WorkSession getRaytracerWorkSession()
        {
            TransformationModel model = getRaytracerModel();

            while (true)
            {
                try
                {
                    WorkSession session = new WorkSession("raytracer-test", model);
                    return session;
                }
                catch (NullPointerException ex)
                {
                    Logger.getLogger(RaytracerWorkSession.class.getName()).log(Level.SEVERE, null, ex);
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ex2)
                    {
                        
                    }
                    continue;
                }
            }
        }
    }// </editor-fold>

    private boolean checkConnection(String IP)
    {
        try
        {
            System.out.println("Step 1");
            ClusterComputerInterface cci =
                    new RMIClientModel<ClusterComputerInterface>(IP,
                    Ports.ClusterComputerPort).getInterface();

            System.out.println("Step 2");
            ClusterComputerStatus ccs = cci.getStatus();
            System.out.println("Step 3");

            if (ccs.processors > 0)
            {
                return true;
            }

            return false;
        } catch (Exception ex)
        {
            Logger.getLogger(ClusterManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
