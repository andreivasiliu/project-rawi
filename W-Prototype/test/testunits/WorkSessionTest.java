package testunits;


import static org.junit.Assert.assertFalse;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import project.MainServer.TransformationModel;
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
    public void testUseCase() throws IOException
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
    
    
    /* TODO:
     * 
     */
    
    @Test
    public void testAllDone()
    {
        assertFalse("Still need to add tests!", true);
    }
}
