package testunits;


import static org.junit.Assert.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import project.Common.ConsoleCommand;
import project.MainServer.TransformationModel;
import project.MainServer.TransformationModel.*;
import project.MainServer.WorkSession;
import project.MainServer.WorkSession.*;

public class WorkSessionTest
{
    TransformationModel model;
    WorkSession workSession;
    
    @Before
    public void setUp() throws Exception
    {
        FileReader reader = new FileReader("resources/SampleModel.xml");
        model = TransformationModel.parseFromXML(reader);
        
        workSession = new WorkSession(model);
    }

    @After
    public void tearDown() throws Exception
    {
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
        if (pack.acceptsFileName("input.txt"))
        {
            Writer fileWriter = pack.putFile("input.txt");
            // write stuff
            fileWriter.close();
        }
        pack.markAsReady();
        
        workSession.setDestination(3);
        
        workSession.getPendingTask().markAsFinished();
        
        workSession.getPackInstance("pack2");
        
    }

    @Test
    public void testRaytracerUseCase()
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
        pack[3].setPattern(Pattern.compile(".*\\.(bmp|png)"));

        packTransformer[0] = model.addPackTransformerNode();
        packTransformer[0].setCommand(new ConsoleCommand("split scene.tr"));
        model.addOutput(pack[0], packTransformer[0]);
        model.addOutput(packTransformer[0], pack[1]);

        packTransformer[1] = model.addPackTransformerNode();
        packTransformer[1].setCommand(new ConsoleCommand("render $input"));
        model.addOutput(pack[0], packTransformer[1]);
        model.addOutput(pack[1], packTransformer[1]);
        model.addOutput(packTransformer[1], pack[2]);

        packTransformer[2] = model.addPackTransformerNode();
        packTransformer[2].setCommand(new ConsoleCommand("join *.rd"));
        // TODO: set it as a joiner.
        model.addOutput(pack[1], packTransformer[2]);
        model.addOutput(pack[2], packTransformer[2]);
        model.addOutput(packTransformer[2], pack[3]);

        assertTrue(false);
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
