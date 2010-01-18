package rawi.mainserver;

import rawi.exceptions.InvalidNodeTypeException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rawi.common.Command;
import rawi.exceptions.DoubleSplitterException;
import rawi.exceptions.DuplicateNameException;

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
    private Set<ModelChangeListener> listeners =
        new HashSet<ModelChangeListener>();
    
    public List<Node> getNodeList()
    {
        return nodes;
    }
    
    public Pack addPackNode()
    {
        Pack pack = new Pack();

        for (ModelChangeListener listener: listeners)
            listener.packAdded(pack);

        return pack;
    }
    
    public PackTransformer addPackTransformerNode()
    {
        PackTransformer packTransformer = new PackTransformer();

        for (ModelChangeListener listener: listeners)
            listener.packTransformerAdded(packTransformer);

        return packTransformer;
    }
    
    public void addOutput(Pack fromNode, PackTransformer toNode)
    {
        if (fromNode == null || toNode == null)
            throw new NullPointerException();
        
        fromNode.outputs.add(toNode);
        toNode.inputs.add(fromNode);

        if (fromNode.getSplitter() != null)
        {
            try
            {
                toNode.updateSplitter(fromNode.getSplitter());
            }
            catch (DoubleSplitterException e)
            {
                fromNode.outputs.remove(toNode);
                toNode.inputs.remove(fromNode);

                throw e;
            }
        }

        for (ModelChangeListener listener: listeners)
            listener.outputAdded(fromNode, toNode);
    }

    public void addOutput(PackTransformer fromNode, Pack toNode)
    {
        if (fromNode == null || toNode == null)
            throw new NullPointerException();

        fromNode.outputs.add(toNode);
        toNode.inputs.add(fromNode);

        if (fromNode.getSplitter() != null)
        {
            try
            {
                toNode.updateSplitter(fromNode.getSplitter());
            }
            catch (DoubleSplitterException e)
            {
                fromNode.outputs.remove(toNode);
                toNode.inputs.remove(fromNode);

                throw e;
            }
        }

        for (ModelChangeListener listener: listeners)
            listener.outputAdded(fromNode, toNode);
    }

    public void addOutputs(Node... nodes)
    {
        for (int i = 0; i < nodes.length - 1; i++)
        {
            if (nodes[i].getClass() == nodes[i+1].getClass())
                throw new InvalidNodeTypeException();

            if (nodes[i] instanceof Pack)
                addOutput((Pack) nodes[i], (PackTransformer) nodes[i+1]);
            else
                addOutput((PackTransformer) nodes[i], (Pack) nodes[i+1]);
        }
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

    public void addListener(ModelChangeListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(ModelChangeListener listener)
    {
        listeners.remove(listener);
    }
    
    /**
     * A node in the Transformation Model.
     */
    public abstract class Node
    {
        private int ID = ++lastUsedID;
        private String name = null;
        protected Pack splitterPack;
        private long x, y;
        
        abstract public Set<? extends Node> getInputs();
        abstract public Set<? extends Node> getOutputs();

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
        
        public Pack getSplitter()
        {
            return splitterPack;
        }

        long getCoordX()
        {
            return x;
        }

        long getCoordY()
        {
            return y;
        }

        public void setCoordX(long x)
        {
            this.x = x;
        }

        public void setCoordY(long y)
        {
            this.y = y;
        }

        protected void removeSplitter(Pack splitterPackToRemove)
        {
            if (splitterPack == null || splitterPack != splitterPackToRemove)
                return;

            splitterPack = null;

            for (Node output: getOutputs())
                output.removeSplitter(splitterPackToRemove);

            for (ModelChangeListener listener: listeners)
                listener.splitterChanged(this);
        }

        /* Note: This method is overrided by pack transformers, and this code
         * is not executed by those pack transformers that are set as joiners.
         */
        protected void updateSplitter(Pack newSplitterPack)
        {
            if (splitterPack == newSplitterPack)
                return;

            if (newSplitterPack == null)
                throw new NullPointerException();

            if (newSplitterPack != null && splitterPack != null)
                throw new DoubleSplitterException("Node " + getId() + " has " +
                        "two splitter packs.");

            try
            {
                splitterPack = newSplitterPack;

                for (Node output: getOutputs())
                    output.updateSplitter(newSplitterPack);

                // Due to the depth-first-search nature of updateSplitter,
                // listeners may be called even though another branch might
                // throw an exception. For that reason, the catch() block will
                // set all changed branches back to null, so the listeners
                // can revert their changes as well.
                for (ModelChangeListener listener: listeners)
                    listener.splitterChanged(this);
            }
            catch (DoubleSplitterException e)
            {
                this.removeSplitter(newSplitterPack);
                
                throw e;
            }
        }

        // This is overridden by packs.
        public boolean isSplitter()
        {
            return false;
        }
    }
    
    /** 
     * Contains a set of file patterns.
     * Its inputs and outputs must be of the PackTransformer type.
     */
    public class Pack extends Node
    {
        private Set<PackTransformer> inputs = new HashSet<PackTransformer>();
        private Set<PackTransformer> outputs = new HashSet<PackTransformer>();
        private Pattern pattern = Pattern.compile(".*");
        private List<Pattern> patternList;
        private boolean isSplitter = false;
        private boolean allowsEmptyPack;

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
        @Override
        public boolean isSplitter()
        {
            return isSplitter;
        }

        public void setIsSplitter(boolean isSplitter)
        {
            this.isSplitter = isSplitter;

            if (isSplitter == false)
            {
                this.removeSplitter(this);
                return;
            }

            try
            {
                this.updateSplitter(isSplitter ? this : null);
            }
            catch (DoubleSplitterException e)
            {
                this.isSplitter = false;

                throw e;
            }
        }

        public boolean allowsEmptyPack()
        {
            return allowsEmptyPack;
        }

        /** Sets whether this splitter pack will be considered "ready" when it
         * has no files. This option has no effect on packs that are not
         * splitters.
         */
        public void setAllowsEmptyPack(boolean allowsEmptyPack)
        {
            this.allowsEmptyPack = allowsEmptyPack;
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
            //setIsSplitter(false);
            this.pattern = pattern;
            this.patternList = null;

            for (ModelChangeListener listener: listeners)
                listener.patternChanged(this);
        }

        public void setPattern(Pattern pattern, boolean createMultiPack)
        {
            setIsSplitter(createMultiPack);
            this.pattern = pattern;
            this.patternList = null;

            for (ModelChangeListener listener: listeners)
                listener.patternChanged(this);
        }

        public void setPatternList(List<Pattern> patternList)
        {
            setIsSplitter(true);
            this.pattern = null;
            this.patternList = patternList;

            for (ModelChangeListener listener: listeners)
                listener.patternChanged(this);
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
        private Command command;
        private boolean isJoiner;

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

        public boolean isJoiner()
        {
            return isJoiner;
        }

        public void setIsJoiner(boolean isJoiner)
        {
            if (this.isJoiner == isJoiner)
                return;

            this.isJoiner = isJoiner;

            if (isJoiner == true)
            {
                // From false to true
                this.isJoiner = true;

                if (getSplitter() != null)
                    removeSplitter(getSplitter());
            }
            else
            {
                // From true to false
                try
                {
                    for (Pack input: inputs)
                        if (input.getSplitter() != null)
                            updateSplitter(input.getSplitter());
                }
                catch (DoubleSplitterException e)
                {
                    // If it was true, then getSplitter() was definitely null.
                    // That's what we will revert to in case of an exception.
                    if (getSplitter() != null)
                        removeSplitter(getSplitter());

                    throw e;
                }
            }
        }

        @Override
        protected void updateSplitter(Pack newSplitterPack)
        {
            if (isJoiner() == false)
                super.updateSplitter(newSplitterPack);
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
