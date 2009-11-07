package testunits;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import project.Exceptions.DuplicateNameException;
import project.MainServer.TransformationModel;
import project.MainServer.TransformationModel.*;

public class TransformationModelTest
{
    TransformationModel model;
    
    @Before
    public void setUp() throws Exception
    {
        model = new TransformationModel();
    }
    
    @Test
    public void testAddPack()
    {
        model.addPackNode();
        assertEquals(1, model.getNodeList().size());
    }
    
    @Test
    public void testAddPackTransformer()
    {
        model.addPackTransformerNode();
        assertEquals(1, model.getNodeList().size());
    }

    @Test
    public void testAddOutput()
    {
        Pack pack = model.addPackNode();
        PackTransformer packTransformer = model.addPackTransformerNode();
        
        model.addOutput(pack, packTransformer);
        assertTrue(pack.getOutputs().contains(packTransformer));
        assertTrue(packTransformer.getInputs().contains(pack));
    }
    
    @Test(expected=DuplicateNameException.class)
    public void testDuplicateName() throws DuplicateNameException
    {
        model.addPackNode().setName("name1");
        model.addPackNode().setName("name1");
    }
    
    @Test
    public void testSetPattern()
    {
        Pack pack = model.addPackNode();
        Pattern pattern = Pattern.compile(".*");
        
        pack.setPattern(pattern);
        pack.setPattern(pattern, true);
        pack.setPattern(pattern, false);
    }
    
    @Test
    public void testSetPatternList()
    {
        Pack pack = model.addPackNode();
        List<Pattern> patternList = new LinkedList<Pattern>();
        
        patternList.add(Pattern.compile(".*\\.txt"));
        patternList.add(Pattern.compile(".*\\.xml"));
        pack.setPatternList(patternList);
    }
    
    @Test
    public void testAcceptsFileName()
    {
        Pack pack = model.addPackNode();
        
        pack.setPattern(Pattern.compile(".*\\.txt"));
        assertTrue(pack.acceptsFileName("something.txt"));
        assertFalse(pack.acceptsFileName("something.c"));
        
        List<Pattern> patternList = new LinkedList<Pattern>();
        patternList.add(Pattern.compile(".*\\.py"));
        patternList.add(Pattern.compile(".*\\.pyc"));
        pack.setPatternList(patternList);
        assertTrue(pack.acceptsFileName("something.py"));
        assertTrue(pack.acceptsFileName("something.pyc"));
        assertFalse(pack.acceptsFileName("something.pycd"));
    }
    
    @Test
    public void testSetCommand()
    {
        assertTrue(false);
    }
    
    /* TODO:
     * 
     * - setCommand/Script
     * - setJoiner/Splitter
     */
    
    @Test
    public void testAllDone()
    {
        assertFalse("Still need to add tests!", true);
    }
}
