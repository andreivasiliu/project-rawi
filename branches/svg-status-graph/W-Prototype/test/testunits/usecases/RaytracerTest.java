package testunits.usecases;

import java.io.IOException;
import java.util.Stack;
import java.util.regex.Pattern;

import org.junit.*;
import rawi.common.Command;
import rawi.common.FileHandle;
import rawi.common.Task;
import static org.junit.Assert.*;

import rawi.mainserver.TransformationModel;
import rawi.mainserver.TransformationModel.*;
import rawi.mainserver.WorkSession;
import rawi.mainserver.WorkSession.*;

public class RaytracerTest
{
    static TransformationModel model;
    static WorkSession session;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        model = null;
        session = null;
    }

    @AfterClass
    public static void tearDownAfterClass()
    {
        model = null;
        session = null;
    }

    @Test
    public void testRaytracerModel()
    {
        model = new TransformationModel();
        Pack[] pack = new Pack[4];
        PackTransformer[] packTransformer = new PackTransformer[3];

        pack[0] = model.addPackNode();
        pack[0].setName("Start Node");
        pack[0].setPattern(Pattern.compile(".*\\.tr"));

        pack[1] = model.addPackNode();
        pack[1].setPattern(Pattern.compile(".*\\.rd"), true);

        pack[2] = model.addPackNode();
        pack[2].setPattern(Pattern.compile(".*\\.(bmp|png)"));

        pack[3] = model.addPackNode();
        pack[3].setName("End Node");
        pack[3].setPattern(Pattern.compile(".*\\.(bmp|png)"));

        packTransformer[0] = model.addPackTransformerNode();
        packTransformer[0].setCommand(new Command("split scene.tr"));
        model.addOutput(pack[0], packTransformer[0]);
        model.addOutput(packTransformer[0], pack[1]);

        packTransformer[1] = model.addPackTransformerNode();
        packTransformer[1].setCommand(new Command("render $input"));
        model.addOutput(pack[0], packTransformer[1]);
        model.addOutput(pack[1], packTransformer[1]);
        model.addOutput(packTransformer[1], pack[2]);

        packTransformer[2] = model.addPackTransformerNode();
        packTransformer[2].setCommand(new Command("join *.rd"));
        packTransformer[2].setIsJoiner(true);
        model.addOutput(pack[1], packTransformer[2]);
        model.addOutput(pack[2], packTransformer[2]);
        model.addOutput(packTransformer[2], pack[3]);
    }

    @Test
    public void testRaytracerSession() throws IOException
    {
        PackInstance packInstance;

        session = new WorkSession("raytracer-test-session", model);

        packInstance = session.getPackInstance("Start Node");
        assertNotNull(packInstance);
        assertTrue(packInstance.acceptsFileName("scene.tr"));

        //session.setDestination("End Node");

        packInstance.putFile(new FileHandle("scene.tr"));

        Stack<Task> taskStack = new Stack();
        Task task;

        while (taskStack.empty() == false &&
               (task = session.getPendingTask()) != null)
        {
            if (task != null)
            {
                System.out.println("Received task.");
                taskStack.push(task);
            }
            else
            {
                System.out.println("Marking task as finished.");
                session.markTaskAsFinished(task, null);
            }
        }
    }
}
