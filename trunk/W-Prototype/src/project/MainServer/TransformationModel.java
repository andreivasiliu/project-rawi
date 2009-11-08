package project.MainServer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import project.Common.Command;
import project.Exceptions.DuplicateNameException;

/** 
 * An abstract description of the processing graph.
 *
 */
public class TransformationModel
{
    private List<Node> nodes = new LinkedList<Node>();
    private Map<Integer, Pack> packIdToPack = new HashMap<Integer, Pack>();
    private Map<String, Pack> packNameToPack = new HashMap<String, Pack>();
    private Map<Integer, PackTransformer> packTransformerIdToPack =
        new HashMap<Integer, PackTransformer>();
    private Map<String, PackTransformer> packTransformerNameToPack =
        new HashMap<String, PackTransformer>();
    private int lastUsedID = 0;
    
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
        reader.setContentHandler(new DefaultHandler()
        {
            final String URI = "http://www.example.org/TransformationModel";
            
            Map<String, Node> localIdToNode = new HashMap<String, Node>();
            Map<String, Set<String>> localIdToOutputList = new HashMap<String, Set<String>>();
            TransformationModel model;
            
            Pack packNode = null;
            PackTransformer packTransformerNode = null;
            String currentID = null;
            
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
                }
                else if (localName.equals("packTransformerNode"))
                {
                    packTransformerNode = model.addPackTransformerNode();
                    
                    currentID = attributes.getValue("id");
                    localIdToNode.put(currentID, packTransformerNode);
                    
                    String nodeName = attributes.getValue("name"); 
                    if (nodeName != null)
                        packTransformerNode.setName(nodeName);
                }
                else if (localName.equals("output"))
                {
                    // TODO: Throw exception if ID is null.
                    
                    if (!localIdToOutputList.containsKey(currentID))
                        localIdToOutputList.put(currentID, new HashSet<String>());
                    
                    Set<String> outputs = localIdToOutputList.get(currentID);
                    outputs.add(attributes.getValue("node"));
                }
            }
            
            @Override
            public void endElement(String uri, String localName, String name)
                    throws SAXException
            {
                if (localName.equals("packNode"))
                    packNode = null;
                else if (localName.equals("packTransformerNode"))
                    packTransformerNode = null;
                else if (localName.equals("transformationGraph"))
                    linkAllNodes();
            }

            private void linkAllNodes()
            {
                for (String from: localIdToOutputList.keySet())
                {
                    Set<String> toList = localIdToOutputList.get(from);
                    
                    Node fromNode = localIdToNode.get(from);
                    if (fromNode == null)
                        ;// TODO: throw Exception
                    for (String to: toList)
                    {
                        Node toNode = localIdToNode.get(to);
                        if (toNode == null)
                            ;// TODO: throw Exception
                        
                        if (fromNode instanceof Pack && toNode instanceof PackTransformer)
                            model.addOutput((Pack)fromNode, (PackTransformer)toNode);
                        else if (fromNode instanceof PackTransformer && toNode instanceof Pack)
                            model.addOutput((PackTransformer)fromNode, (Pack)toNode);
                        else
                            ;// TODO: throw Exception
                    }
                }
            }
            
            public DefaultHandler withModel(TransformationModel model)
            {
                this.model = model;
                return this;
            }
        }.withModel(model));
        
        reader.parse(new InputSource(inputXml));

        return model;
    }
    
    public List<Node> getNodeList()
    {
        return nodes;
    }
    
    public Pack addPackNode()
    {
        return new Pack();
    }
    
    public PackTransformer addPackTransformerNode()
    {
        return new PackTransformer();
    }
    
    public void addOutput(Pack fromNode, PackTransformer toNode)
    {
        if (fromNode == null || toNode == null)
            throw new NullPointerException();
        
        fromNode.outputs.add(toNode);
        toNode.inputs.add(fromNode);
    }
    
    public void addOutput(PackTransformer fromNode, Pack toNode)
    {
        if (fromNode == null || toNode == null)
            throw new NullPointerException();
        
        fromNode.outputs.add(toNode);
        toNode.inputs.add(fromNode);
    }
    
    public Pack getPack(int packId)
    {
        return packIdToPack.get(packId);
    }

    public Pack getPack(String packName)
    {
        return packNameToPack.get(packName);
    }
    
    public PackTransformer getPackTransformer(int id)
    {
        return packTransformerIdToPack.get(id);
    }
    
    public PackTransformer getPackTransformer(String name)
    {
        return packTransformerNameToPack.get(name);
    }
    
    /**
     * A node in the Transformation Model.
     */
    public abstract class Node
    {
        private int ID = ++lastUsedID;
        private String name = null;
        
        private Node()
        {
            nodes.add(this);
        }
        
        public int getId()
        {
            return ID;
        }
        
        public String getName()
        {
            return name;
        }
        
        public abstract void setName(String name);
        
        abstract public Set<? extends Node> getInputs();
        abstract public Set<? extends Node> getOutputs();
    }
    
    /** 
     * Contains a set of file patterns.
     * Its inputs and outputs must be of the PackTransformer type.
     */
    public class Pack extends Node
    {
        private Set<PackTransformer> inputs = new HashSet<PackTransformer>();
        private Set<PackTransformer> outputs = new HashSet<PackTransformer>();
        Pattern pattern = Pattern.compile(".*");
        List<Pattern> patternList;
        private boolean isMultiPack = false;
        
        private Pack()
        {
            super();
            
            packIdToPack.put(getId(), this);
        }

        @Override
        public Set<PackTransformer> getInputs()
        {
            return inputs;
        }

        @Override
        public Set<PackTransformer> getOutputs()
        {
            return outputs;
        }

        @Override
        public void setName(String name)
        {
            if (packNameToPack.containsKey(name) &&
                    packNameToPack.get(name) != this)
                throw new DuplicateNameException();
            
            packNameToPack.put(name, this);
            super.name = name;
        }

        /**
         * Returns whether this pack has multiple states.
         */
        public boolean isMultiPack()
        {
            return isMultiPack;
        }

        public boolean acceptsFileName(String fileName)
        {
            if (pattern != null)
            {
                Matcher m = pattern.matcher(fileName);
                return m.matches();
            }
            else
            {
                for (Pattern p: patternList)
                {
                    Matcher m = p.matcher(fileName);
                    if (m.matches())
                        return true;
                }
                
                return false;
            }
        }

        public void setPattern(Pattern pattern)
        {
            this.pattern = pattern;
            this.patternList = null;
            isMultiPack = false;
        }

        public void setPattern(Pattern pattern, boolean createMultiPack)
        {
            this.pattern = pattern;
            this.patternList = null;
            isMultiPack = createMultiPack;
        }

        public void setPatternList(List<Pattern> patternList)
        {
            this.pattern = null;
            this.patternList = patternList;
            isMultiPack = true;
        }
    }
    
    /**
     * Contains rules and commands to transform a pack into another pack.
     * Its inputs and outputs must be of the Pack type.
     */
    public class PackTransformer extends Node
    {
        private Set<Pack> inputs = new HashSet<Pack>();
        private Set<Pack> outputs = new HashSet<Pack>();
        Command command;

        private PackTransformer()
        {
            super();
            
            packTransformerIdToPack.put(getId(), this);
        }
        
        @Override
        public Set<Pack> getInputs()
        {
            return inputs;
        }

        @Override
        public Set<Pack> getOutputs()
        {
            return outputs;
        }

        @Override
        public void setName(String name)
        {
            if (packTransformerNameToPack.containsKey(name) &&
                    packTransformerNameToPack.get(name) != this)
                throw new DuplicateNameException();
            
            packTransformerNameToPack.put(name, this);
            super.name = name;
        }

        public Command getCommand()
        {
            return command;
        }

        public void setCommand(Command command)
        {
            this.command = command;
        }
    }
    
    /**
     * A pack transformer that does nothing.
     */
    public class FileDistributer extends PackTransformer
    {
        // TODO
    }
}
