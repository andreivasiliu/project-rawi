package project.MainServer;


import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import project.Common.FileHandle;
import project.Exceptions.DoubleSplitterException;
import project.MainServer.TransformationModel.*;
import project.MainServer.WorkSession.*;
import project.MainServer.XML.TransformationModelParser;

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
        
        workSession = new WorkSession(model);
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

        workSession.setDestination(3);

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
