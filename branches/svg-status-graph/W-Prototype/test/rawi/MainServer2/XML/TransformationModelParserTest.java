package rawi.MainServer2.XML;

import rawi.mainserver.*;
import java.io.InputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import java.io.InputStreamReader;
import java.io.Reader;
import org.junit.Before;
import org.junit.Test;
import rawi.mainserver.XML.TransformationModelParser;

public class TransformationModelParserTest
{
    TransformationModel model;

    @Before
    public void setUp() throws Exception
    {
        InputStream stream = this.getClass().
                getResourceAsStream("/resources/SampleModel.xml");
        Reader reader = new InputStreamReader(stream);
        model = TransformationModelParser.parseFromXML(reader);
    }

    @Test
    public void testNodeCount()
    {
        assertNotNull(model);
        assertEquals(3, model.getNodeList().size());
    }

    @Test
    public void testIDs()
    {
        assertNotNull(model.getPack(1));
        assertNotNull(model.getPackTransformer(2));
        assertNotNull(model.getPack(3));
    }

    @Test
    public void testOutputs()
    {
        assertTrue("1 to 2", model.getPack(1).getOutputs().contains(model.getPackTransformer(2)));
        assertTrue("2 to 3", model.getPackTransformer(2).getOutputs().contains(model.getPack(3)));
    }

    @Test
    public void testGetters()
    {
        assertNotNull("Pack by ID", model.getPack(1));
        assertNotNull("Transformer by ID", model.getPackTransformer(2));
        assertNotNull("Pack by name", model.getPack("pack1"));
        assertNotNull("Transformer by name", model.getPackTransformer("transformer1"));
    }
}
