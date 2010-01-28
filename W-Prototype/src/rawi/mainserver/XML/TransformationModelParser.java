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

import rawi.mainserver.TransformationModel;
import rawi.mainserver.TransformationModel.*;

public class TransformationModelParser extends DefaultHandler
{
    final String URI = "http://www.example.org/TransformationModel";
    Map<String, Node> localIdToNode = new HashMap<String, Node>();
    Map<String, Set<String>> localIdToOutputList = new HashMap<String, Set<String>>();
    TransformationModel model;
    Pack packNode = null;
    PackTransformer packTransformerNode = null;
    String currentID = null;

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
            packNode = model.addPackNode();

            currentID = attributes.getValue("id");
            localIdToNode.put(currentID, packNode);

            String nodeName = attributes.getValue("name");
            if (nodeName != null)
                packNode.setName(nodeName);

            if (attributes.getValue("isSplitter") != null
                    && attributes.getValue("isSplitter").equals("true"))
                packNode.setIsSplitter(true);

            String x = attributes.getValue("x");
            if (x != null)
                packNode.setCoordX(Long.parseLong(x));

            String y = attributes.getValue("y");
            if (y != null)
                packNode.setCoordY(Long.parseLong(y));
        }
        else if (localName.equals("packTransformerNode"))
        {
            packTransformerNode = model.addPackTransformerNode();

            currentID = attributes.getValue("id");
            localIdToNode.put(currentID, packTransformerNode);

            String nodeName = attributes.getValue("name");
            if (nodeName != null)
            {
                packTransformerNode.setName(nodeName);
            }

            if (attributes.getValue("isJoiner") != null
                    && attributes.getValue("isJoiner").equals("true"))
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
            // TODO: Throw exception if ID is null.

            if (!localIdToOutputList.containsKey(currentID))
            {
                localIdToOutputList.put(currentID, new HashSet<String>());
            }

            Set<String> outputs = localIdToOutputList.get(currentID);
            outputs.add(attributes.getValue("node"));
        } else if (localName.equals("pattern")) {
            Pattern pattern = Pattern.compile(attributes.getValue("regex"));
            packNode.setPattern(pattern);
        } else if (localName.equals("command")) {
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
        for (String from : localIdToOutputList.keySet())
        {
            Set<String> toList = localIdToOutputList.get(from);

            Node fromNode = localIdToNode.get(from);
            if (fromNode == null);// TODO: throw Exception
            for (String to : toList)
            {
                Node toNode = localIdToNode.get(to);
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
                else;// TODO: throw Exception
            }
        }
    }
}
