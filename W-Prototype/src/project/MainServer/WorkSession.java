package project.MainServer;

import project.MainServer.TransformationModel.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * A description of a working session based on a transformation model.
 */
public class WorkSession
{
    TransformationModel model;
    Map<Node, NodeInstance> nodeInstances;
    Map<Pack, PackInstance> packInstances;
    Map<PackTransformer, PackTransformerInstance> packTransformerInstances;
    
    public WorkSession(TransformationModel model)
    {
        if (model == null)
            throw new NullPointerException();
        
        this.model = model;
        nodeInstances = new HashMap<Node, NodeInstance>();
        packInstances = new HashMap<Pack, PackInstance>();
        packTransformerInstances = new HashMap<PackTransformer,
            PackTransformerInstance>();
        
        for (Node node: model.getNodeList())
            createInstance(node);
    }
    
    /** Returns a pending task whose dependencies are met.
     */
    public Task getPendingTask()
    {
        // TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // TODO: Find out how to do that link.
    /** Marks a task as done, to allow other tasks that depend on it to be
     * returned by {@link getPendingTask()}.
     */
    public void markTaskAsFinished(Task task)
    {
        // TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDestination(int packId)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void setDestination(String packName)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public PackInstance getPackInstance(int id)
    {
        Pack pack = model.getPack(id);
        
        if (pack == null)
            return null;
        
        return packInstances.get(pack);
    }
    
    public PackInstance getPackInstance(String name)
    {
        Pack pack = model.getPack(name);
        
        if (pack == null)
            return null;
        
        return packInstances.get(pack);
    }
    
    private void createInstance(Node node)
    {
        if (node instanceof Pack)
        {
            Pack pack = (Pack) node;
            this.nodeInstances.put(node, new PackInstance(pack));
        }
        else if (node instanceof PackTransformer)
        {
            PackTransformer packTransformer = (PackTransformer) node;
            PackTransformerInstance packTransformerInstance =
                new PackTransformerInstance(packTransformer);
            this.nodeInstances.put(packTransformer, packTransformerInstance);
            this.packTransformerInstances.put(packTransformer,
                    packTransformerInstance);
        }
    }
    
    public abstract class NodeInstance
    {
        public abstract Node getOrigin();
    }
    
    public class PackInstance extends NodeInstance
    {
        private Pack origin;
        
        protected PackInstance(Pack origin)
        {
            this.origin = origin;
        }
        
        @Override
        public Pack getOrigin()
        {
            return origin;
        }
        
        public void removeFiles(String pattern)
        {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeAllFiles()
        {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeFile(String fileName)
        {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean acceptsFileName(String fileName)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public PackFileWriter putFile(String fileName) throws IOException
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void markAsReady()
        {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    public class PackTransformerInstance extends NodeInstance
    {
        private PackTransformer origin;
        
        protected PackTransformerInstance(PackTransformer origin)
        {
            this.origin = origin;
        }
        
        @Override
        public PackTransformer getOrigin()
        {
            return origin;
        }
    }
    
    public class PackFileWriter extends FileWriter
    {

        public PackFileWriter(String fileName) throws IOException
        {
            super(fileName);
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
