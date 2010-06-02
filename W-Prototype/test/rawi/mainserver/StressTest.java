package rawi.mainserver;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import rawi.common.FileHandle;
import rawi.common.Task;
import rawi.common.TaskResult;
import rawi.common.WorkSessionStatus;
import rawi.mainserver.WorkSession.*;

public class StressTest
{
    ClusterManager clusterManager;
    Thread managerThread;
    WorkSession session;

    @Before
    public void setUp() throws Exception
    {
        clusterManager = new ClusterManager();
        managerThread = new Thread(clusterManager);
        managerThread.start();

        //System.out.println("Setting up giant work session...");
        session = WorkSession.createGiantWorkSession("giant-test-session");
        clusterManager.addWorkSession(session);
        //System.out.println("Giant work session created.");
    }

    @After
    public void tearDown() throws Exception
    {
        clusterManager.shutdown();
        managerThread.join();
    }

    @Test
    public void testGetSessionStatus()
    {
        WorkSessionStatus status =
                clusterManager.getSessionStatus("giant-test-session");

        assertEquals(2, status.getPacks().size());

        for (WorkSessionStatus.Pack pack : status.getPacks())
            assertEquals("Has exactly 30000 subStates", 30000, pack.subStates);
    }

    @Test
    public void testStartSession()
    {
        System.out.println("Starting session...");
        clusterManager.startWorkSession("giant-test-session");
        System.out.println("Session started.");
    }

    @Test
    public void testGetPendingTask()
    {
        Stack<Task> bunchOfTasks = new Stack<Task>();

        clusterManager.startWorkSession("giant-test-session");

        //session.printStatus();

        for (int i = 0; i < 150; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                Task task = session.getPendingTask();
                assertNotNull("Chunk " + i + ", task " + j, task);
                bunchOfTasks.push(task);
            }

            for (int j = 0; j < 100; j++)
            {
                List<FileHandle> files = new LinkedList<FileHandle>();
                files.add(new FileHandle("result-" + i + "-" + j + ".txt"));

                Task task = bunchOfTasks.pop();
                TaskResult result = new TaskResult(task.getId(), "test-computer", files);

                session.markTaskAsFinished(task, result);
            }
        }

        PackTransformerInstance transfInst =
                session.getPackTransformerInstance("transformer1");
        
        WorkSessionStatus status = 
                clusterManager.getSessionStatus("giant-test-session");

        //session.printStatus();

        for (WorkSessionStatus.PackTransformer transf : status.getPackTransformers())
        {
            assertEquals("Has exactly 15000 completed tasks", 15000, transf.doneTasks);
        }
    }
}
