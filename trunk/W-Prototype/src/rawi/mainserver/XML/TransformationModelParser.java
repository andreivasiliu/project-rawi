package rawi.mainserver.XML;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import rawi.common.Command;
import rawi.exceptions.InvalidIdException;
import rawi.exceptions.InvalidNodeTypeException;

import rawi.mainserver.TransformationModel;
import rawi.mainserver.TransformationModel.*;

public class TransformationModelParser extends DefaultHandler
{
    final static String URI = "http://www.example.org/TransformationModel";
    private Map<Integer, Node> idToNode = new HashMap<Integer, Node>();
    private Map<Integer, Set<Integer>> idToOutputList = new HashMap<Integer, Set<Integer>>();
    private TransformationModel model;
    private Pack packNode = null;
    private PackTransformer packTransformerNode = null;
    private Integer currentID = null;

    /**
     * Will produce a graph from an XML description.
     *
     * @param inputXml A reader for the source XML.
     * @return A new TransformationModel object that corresponds with the XML
     * description.
     * @throws SAXException
     * @throws IOException
     */
    public static TransformationModel parseFromXML(Reader inputXml)
            throws SAXException, IOException
    {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        TransformationModel model = new TransformationModel();

        // TODO: Add more error-checking.
        reader.setContentHandler(new TransformationModelParser(model));

        reader.parse(new InputSource(inputXml));

        return model;
    }

    private TransformationModelParser(TransformationModel model)
    {
        this.model = model;
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException
    {
        if (!uri.equals(URI))
            return;

        if (localName.equals("packNode"))
        {
            currentID = parseInteger(attributes.getValue("id"));
            if (currentID == null)
                throw new InvalidIdException("The XML contains an invalid id " +
                        "attribute in a packNode element.");

            packNode = model.addPackNode(currentID);
            idToNode.put(currentID, packNode);

            String nodeName = attributes.getValue("name");
            if (nodeName != null)
                packNode.setName(nodeName);

            if (isTrue(attributes.getValue("isSplitter")))
                packNode.setIsSplitter(true);

            if (isTrue(attributes.getValue("allowsMultipleFiles")))
                packNode.setAllowsMultipleFiles(true);

            String x = attributes.getValue("x");
            if (x != null)
                packNode.setCoordX(Long.parseLong(x));

            String y = attributes.getValue("y");
            if (y != null)
                packNode.setCoordY(Long.parseLong(y));
        }
        else if (localName.equals("packTransformerNode"))
        {
            currentID = parseInteger(attributes.getValue("id"));
            if (currentID == null)
                throw new InvalidIdException("The XML contains an invalid id " +
                        "attribute in a packNode element.");

            packTransformerNode = model.addPackTransformerNode(currentID);
            idToNode.put(currentID, packTransformerNode);

            String nodeName = attributes.getValue("name");
            if (nodeName != null)
            {
                packTransformerNode.setName(nodeName);
            }

            if (isTrue(attributes.getValue("isJoiner")))
                packTransformerNode.setIsJoiner(true);

            String x = attributes.getValue("x");
            if (x != null)
                packTransformerNode.setCoordX(Long.parseLong(x));

            String y = attributes.getValue("y");
            if (y != null)
                packTransformerNode.setCoordY(Long.parseLong(y));
        }
        else if (localName.equals("output"))
        {
            Integer outputId = parseInteger(attributes.getValue("node"));
            if (outputId == null)
                throw new InvalidIdException("The XML contains an invalid node " +
                        "attribute in an output element.");

            if (!idToOutputList.containsKey(currentID))
            {
                idToOutputList.put(currentID, new HashSet<Integer>());
            }

            Set<Integer> outputs = idToOutputList.get(currentID);
            outputs.add(outputId);
        }
        else if (localName.equals("pattern"))
        {
            String pattern = attributes.getValue("regex");

            if (pattern == null || pattern.equals(""))
                pattern = ".*";

            packNode.setPattern(Pattern.compile(pattern));
        }
        else if (localName.equals("command"))
        {
            Command cmd = new Command(attributes.getValue("exec").split(" "));
            packTransformerNode.setCommand(cmd);
        }

    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException
    {
        if (localName.equals("packNode"))
        {
            packNode = null;
        }
        else if (localName.equals("packTransformerNode"))
        {
            packTransformerNode = null;
        }
        else if (localName.equals("transformationGraph"))
        {
            linkAllNodes();
        }
    }

    private void linkAllNodes()
    {
        for (Integer from : idToOutputList.keySet())
        {
            Set<Integer> toList = idToOutputList.get(from);

            Node fromNode = idToNode.get(from);
            if (fromNode == null);// TODO: throw Exception
            for (Integer to : toList)
            {
                Node toNode = idToNode.get(to);
                if (toNode == null);// TODO: throw Exception

                if (fromNode instanceof Pack
                        && toNode instanceof PackTransformer)
                {
                    model.addOutput((Pack) fromNode, (PackTransformer) toNode);
                }
                else if (fromNode instanceof PackTransformer
                        && toNode instanceof Pack)
                {
                    model.addOutput((PackTransformer) fromNode, (Pack) toNode);
                }
                else
                    throw new InvalidNodeTypeException("Two nodes of the " +
                            "same type cannot be directly connected.");
            }
        }
    }

    // Utility functions.

    private static boolean isTrue(String str)
    {
        if (str != null && str.equalsIgnoreCase("true"))
            return true;

        return false;
    }

    private static Integer parseInteger(String str)
    {
        if (str == null)
            return null;
        
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }
}
