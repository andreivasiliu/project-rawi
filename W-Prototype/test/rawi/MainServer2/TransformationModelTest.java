package rawi.MainServer2;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import rawi.common.Command;
import rawi.exceptions.DoubleSplitterException;
import rawi.exceptions.DuplicateNameException;
import rawi.mainserver.TransformationModel;
import rawi.mainserver.TransformationModel.*;

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
    public void testNamedNodes()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();

        pack1.setName("Name 1");
        pack2.setName("Name 2");

        assertEquals(pack1, model.getPack("Name 1"));
        assertEquals(pack2, model.getPack("Name 2"));
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
        PackTransformer packTransformer = model.addPackTransformerNode();
        Command command = new Command("cp a.txt b.txt");

        packTransformer.setCommand(command);
    }

    @Test
    public void testSplitter()
    {
        // p0---pt0---p1---pt1---p2
        //    ^     ^    ^     ^
        //    |     |    |     \-- third
        //    |     |    \-- forth
        //    \-----\-- first and second

        Pack[] pack = new Pack[3];
        PackTransformer[] packTransformer = new PackTransformer[2];

        pack[0] = model.addPackNode();
        pack[1] = model.addPackNode();
        pack[2] = model.addPackNode();

        pack[0].setPattern(Pattern.compile(".*"), true);

        packTransformer[0] = model.addPackTransformerNode();
        packTransformer[1] = model.addPackTransformerNode();

        model.addOutput(pack[0], packTransformer[0]);
        model.addOutput(packTransformer[0], pack[1]);
        model.addOutput(packTransformer[1], pack[2]);
        model.addOutput(pack[1], packTransformer[1]);

        assertNotNull(pack[0].getSplitter());
        assertNotNull(pack[1].getSplitter());
        assertNotNull(pack[2].getSplitter());
        assertEquals(pack[0], packTransformer[1].getSplitter());
        assertEquals(pack[0], pack[2].getSplitter());
    }

    @Test(expected=DoubleSplitterException.class)
    public void testDoubleSplitterException1()
    {
        Pack[] pack = new Pack[2];
        PackTransformer packTransformer;

        pack[0] = model.addPackNode();
        pack[1] = model.addPackNode();
        packTransformer = model.addPackTransformerNode();

        pack[0].setPattern(Pattern.compile(".*\\.txt"), true);
        model.addOutput(pack[0], packTransformer);

        pack[1].setPattern(Pattern.compile(".*\\.txt"), true);
        model.addOutput(pack[1], packTransformer);
    }

    @Test(expected=DoubleSplitterException.class)
    public void testDoubleSplitterException2()
    {
        // p0---pt0---p2
        //              \
        //               pt2
        //              /
        // p1===pt1---p3
        //    ^- adding this link should throw an exception because pt2 would
        //       have as a splitter both p0 and p1


        Pack[] pack = new Pack[4];
        PackTransformer[] packTransformer = new PackTransformer[3];

        for (int i = 0; i < 4; i++)
            pack[i] = model.addPackNode();

        for (int i = 0; i < 3; i++)
            packTransformer[i] = model.addPackTransformerNode();

        pack[0].setPattern(Pattern.compile(".*\\.txt"), true);
        model.addOutput(pack[0], packTransformer[0]);

        pack[1].setPattern(Pattern.compile(".*\\.txt"), true);
        
        model.addOutput(packTransformer[0], pack[2]);
        model.addOutput(packTransformer[1], pack[3]);
        model.addOutput(pack[2], packTransformer[2]);
        model.addOutput(pack[3], packTransformer[2]);

        assertEquals(pack[0], packTransformer[2].getSplitter());

        model.addOutput(pack[1], packTransformer[1]);
    }

    @Test
    public void testSetIsSplitter()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();
        PackTransformer packTransformer = model.addPackTransformerNode();

        model.addOutput(pack1, packTransformer);
        model.addOutput(packTransformer, pack2);

        assertNull(pack2.getSplitter());
        pack1.setIsSplitter(true);
        assertEquals(pack1, pack2.getSplitter());
    }

    @Test
    public void testExceptionRecovery1()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();
        PackTransformer packTransformer1 = model.addPackTransformerNode();
        PackTransformer packTransformer2 = model.addPackTransformerNode();
        Pack pack3 = model.addPackNode();

        pack1.setIsSplitter(true);

        model.addOutput(pack1, packTransformer1);
        model.addOutput(pack2, packTransformer2);
        model.addOutput(packTransformer1, pack3);
        model.addOutput(packTransformer2, pack3);

        assertEquals("Before", pack1, pack3.getSplitter());
        assertEquals("Before", pack1, packTransformer1.getSplitter());

        boolean exceptionCaught = false;

        try
        {
            pack2.setIsSplitter(true);
        }
        catch (DoubleSplitterException e)
        {
            exceptionCaught = true;
        }

        assertTrue(exceptionCaught);

        // Check that if even though there was an exception, all nodes were
        // brought back to a valid state.

        assertNull(pack2.getSplitter());
        assertNull(packTransformer2.getSplitter());
        assertEquals("After", pack1, pack3.getSplitter());
        assertEquals("After", pack1, packTransformer1.getSplitter());
    }

    @Test
    public void testSetJoiner1()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();
        PackTransformer packTransformer = model.addPackTransformerNode();

        pack1.setIsSplitter(true);
        packTransformer.setIsJoiner(true);
        
        model.addOutput(pack1, packTransformer);
        model.addOutput(packTransformer, pack2);
        
        assertNull(pack2.getSplitter());
    }

    @Test
    public void testSetJoiner2()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();
        PackTransformer packTransformer = model.addPackTransformerNode();

        model.addOutput(pack1, packTransformer);
        model.addOutput(packTransformer, pack2);

        pack1.setIsSplitter(true);
        assertNotNull(pack2.getSplitter());
        packTransformer.setIsJoiner(true);
        assertNull(packTransformer.getSplitter());
        assertNull(pack2.getSplitter());

        packTransformer.setIsJoiner(false);
        assertNotNull(packTransformer.getSplitter());
        assertNotNull(pack2.getSplitter());
    }

    @Test(expected=DoubleSplitterException.class)
    public void testDoubleSplitterException3()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();
        PackTransformer packTransformer = model.addPackTransformerNode();

        packTransformer.setIsJoiner(true);
        pack1.setIsSplitter(true);
        pack2.setIsSplitter(true);

        model.addOutput(pack1, packTransformer);
        model.addOutput(pack2, packTransformer);
        model.addOutput(packTransformer, pack2);

        packTransformer.setIsJoiner(false);
    }

    @Test
    public void testExceptionRecovery2()
    {
        Pack pack1 = model.addPackNode();
        Pack pack2 = model.addPackNode();
        Pack pack3 = model.addPackNode();
        PackTransformer packTransformer = model.addPackTransformerNode();

        pack1.setIsSplitter(true);
        pack2.setIsSplitter(true);
        packTransformer.setIsJoiner(true);

        model.addOutput(pack1, packTransformer);
        model.addOutput(pack2, packTransformer);
        model.addOutput(packTransformer, pack3);

        assertNull(pack3.getSplitter());

        boolean exceptionCaught = false;

        try
        {
            packTransformer.setIsJoiner(false);
        }
        catch (DoubleSplitterException e)
        {
            exceptionCaught = true;
        }

        assertTrue(exceptionCaught);

        assertNull(packTransformer.getSplitter());
        assertNull(pack3.getSplitter());
    }

    @Test
    public void testExceptionRecovery3()
    {
        Pack[] pack = new Pack[4];
        PackTransformer[] packTransformer = new PackTransformer[3];

        for (int i = 0; i < 4; i++)
            pack[i] = model.addPackNode();

        for (int i = 0; i < 3; i++)
            packTransformer[i] = model.addPackTransformerNode();

        pack[0].setIsSplitter(true);
        pack[1].setIsSplitter(true);
        packTransformer[1].setIsJoiner(true);

        model.addOutput(pack[0], packTransformer[0]);
        model.addOutput(pack[1], packTransformer[1]);
        model.addOutput(packTransformer[0], pack[2]);
        model.addOutput(packTransformer[1], pack[3]);
        model.addOutput(pack[2], packTransformer[2]);
        model.addOutput(pack[3], packTransformer[2]);

        assertNotNull(pack[2].getSplitter());
        assertNull(pack[3].getSplitter());
        assertEquals(pack[0], packTransformer[2].getSplitter());

        boolean exceptionCaught = false;

        try
        {
            packTransformer[1].setIsJoiner(false);
        }
        catch (DoubleSplitterException e)
        {
            exceptionCaught = true;
        }

        assertTrue(exceptionCaught);

        assertNotNull(pack[2].getSplitter());
        assertNull(pack[3].getSplitter());
        assertEquals(pack[0], packTransformer[2].getSplitter());
    }
}
