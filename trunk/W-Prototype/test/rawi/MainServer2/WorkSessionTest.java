package rawi.MainServer2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rawi.common.Command;
import rawi.common.FileHandle;
import rawi.common.Task;
import rawi.common.TaskResult;

import rawi.exceptions.DoubleSplitterException;
import rawi.mainserver.TransformationModel;
import rawi.mainserver.TransformationModel.*;
import rawi.mainserver.WorkSession;
import rawi.mainserver.WorkSession.*;
import rawi.mainserver.XML.TransformationModelParser;

public class WorkSessionTest
{
    TransformationModel model;
    WorkSession workSession;

    @Before
    public void setUp() throws Exception
    {
        InputStream stream = this.getClass().
                getResourceAsStream("/resources/SampleModel.xml");
        Reader reader = new InputStreamReader(stream);
        model = TransformationModelParser.parseFromXML(reader);
        
        workSession = new WorkSession("test-session", model);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testNamedNodes()
    {
        assertNotNull(workSession.getPackInstance("pack1"));
        assertNotNull(workSession.getPackInstance("pack2"));
    }

    @Test
    public void testMultiPacks()
    {
        model.getPack("pack1").setPattern(Pattern.compile(".*\\.txt"), true);
        assertTrue(model.getPack("pack1").isSplitter());
        assertNotNull(model.getPack("pack2").getSplitter());

        PackInstance packInstance = workSession.getPackInstance("pack1");
        packInstance.putFile(new FileHandle("file1.txt"));
        packInstance.putFile(new FileHandle("file2.txt"));
        packInstance.putFile(new FileHandle("file3.txt"));
        assertEquals(3, packInstance.subPacks());
        assertEquals(3, workSession.getPackInstance("pack2").subPacks());
    }

    @Test
    public void testNewPackInstanceOnNewPack()
    {
       Pack pack = model.addPackNode();
       PackInstance packInstance = workSession.getPackInstance(pack.getId());
       assertNotNull(packInstance);
    }

    @Test(expected=DoubleSplitterException.class)
    public void testDoubleMultiPack()
    {
        Pack pack = model.addPackNode();
        pack.setPattern(Pattern.compile(".*"), true);

        PackTransformer packTransformer = model.getPackTransformer("transformer1");
        model.addOutput(pack, packTransformer);

        pack = model.getPack("pack1");
        pack.setPattern(Pattern.compile(".*"), true);
    }

    @Test
    public void testCommands()
    {
        PackInstance packInstance = workSession.getPackInstance("pack1");
        packInstance.getOrigin().setIsSplitter(true);
        packInstance.putFile(new FileHandle("hello.txt"));
        packInstance.putFile(new FileHandle("bye.txt"));

        PackTransformer packTransformer = model.getPackTransformer("transformer1");
        packTransformer.setCommand(new Command("say", "$pack1"));
        packTransformer.setIsJoiner(true);

        workSession.startSession();
        Task task = workSession.getPendingTask();
        String[] cmdArray = task.getCommand().getCommandArray();
        
        assertEquals(cmdArray[1], "hello.txt");
        assertTrue(cmdArray.length == 3);
        assertEquals(cmdArray[2], "bye.txt");
    }

    @Test
    public void testTaskResult()
    {
        PackInstance packInstance = workSession.getPackInstance("pack1");
        packInstance.putFile(new FileHandle("hello.txt"));

        PackTransformer packTransformer = model.getPackTransformer("transformer1");
        packTransformer.setCommand(new Command("say", "$pack1"));

        workSession.startSession();
        Task task = workSession.getPendingTask();

        assertEquals(task.getCommand().getCommandArray()[1], "hello.txt");

        List<FileHandle> resultFiles = new LinkedList<FileHandle>();
        resultFiles.add(new FileHandle("result.txt"));
        TaskResult taskResult = new TaskResult(task.getId(), "test", resultFiles);

        workSession.markTaskAsFinished(task, taskResult);

        packInstance = workSession.getPackInstance("pack2");
        Collection<FileHandle> files = packInstance.getFiles(0);
        assertEquals(files.size(), 1);
        assertTrue(files.contains(resultFiles.get(0)));
    }

    @Test
    public void testSetTargetNode()
    {
        // p0--pt0--p1--pt1--p3
        //              /
        //          p2-/

        Pack[] pack = new Pack[4];
        PackTransformer[] packTransformer = new PackTransformer[2];

        for (int i = 0; i < 4; i++)
            pack[i] = model.addPackNode();

        for (int i = 0; i < 2; i++)
            packTransformer[i] = model.addPackTransformerNode();

        model.addOutputs(pack[0], packTransformer[0], pack[1],
                packTransformer[1], pack[3]);
        model.addOutput(pack[2], packTransformer[1]);

        assertTrue(workSession.isUsedInTransformation(pack[2]));

        // By node ID
        workSession.setTargetNode(pack[0].getId());
        assertTrue(workSession.isUsedInTransformation(pack[0]));
        assertFalse(workSession.isUsedInTransformation(pack[1]));
        assertFalse(workSession.isUsedInTransformation(pack[2]));
        assertFalse(workSession.isUsedInTransformation(pack[3]));

        // By model node
        workSession.setTargetNode((Node) packTransformer[1]);
        assertTrue(workSession.isUsedInTransformation(pack[0]));
        assertFalse(workSession.isUsedInTransformation(pack[1]));
        assertFalse(workSession.isUsedInTransformation(pack[2]));
        assertFalse(workSession.isUsedInTransformation(pack[3]));

        // By session node
        workSession.setTargetNode(workSession.getPackInstance(pack[3].getId()));

    }

    /* Use case:
     * 
     * - Create a new work session, linked to a model
     * - Get a node by its ID/name
     * - Check whether that node accepts a file's name
     * - Upload that file to that node, and mark the node as "Ready"
     * - Select one, some, or all nodes as destinations
     * - Repeatedly choose and do one of the following:
     *   - Request a pending job from the work session
     *   - Mark a previously requested job as finished
     * 
     * 
     * Things to test:
     * 
     * - If the work session is capable of updating itself when something in
     * the transformation model changes.
     * - Whether it is thread-safe.
     */
    
    @Test
    public void testSimpleUseCase() throws IOException
    {
        PackInstance pack = workSession.getPackInstance("pack1");

        pack.removeFiles("*.txt");
        pack.removeFile("input.txt");
        pack.removeAllFiles();
        assertTrue(pack.acceptsFileName("input.txt"));

        pack.putFile(new FileHandle("input.txt"));

        workSession.setTargetNode(3);

        workSession.getPackInstance("pack2");

    }
    
    /* TODO:
     * 
     */
    
    @Test
    public void testAllDone()
    {
        assertFalse("Still need to add tests!", true);
    }
}
