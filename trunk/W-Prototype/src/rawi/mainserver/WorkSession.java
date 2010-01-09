package rawi.mainserver;

import rawi.exceptions.InvalidIdException;
import rawi.mainserver.TransformationModel.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import rawi.common.Command;
import rawi.common.FileHandle;
import rawi.common.SessionInfo;
import rawi.common.Task;
import rawi.common.TaskResult;
import rawi.exceptions.InvalidOperationException;


/**
 * A description of a working session based on a transformation model.
 */
public class WorkSession implements ModelChangeListener
{
    public enum SessionStatus { STARTED, STOPPING, STOPPED };

    SessionInfo sessionInfo;
    private String sessionId;
    TransformationModel model;
    Map<Node, NodeInstance> nodeInstances = new HashMap<Node, NodeInstance>();
    Map<Pack, PackInstance> packInstances = new HashMap<Pack, PackInstance>();
    Map<PackTransformer, PackTransformerInstance> packTransformerInstances
             = new HashMap<PackTransformer, PackTransformerInstance>();
    Set<Integer> targettedNodes = new HashSet<Integer>();
    SessionStatus status = SessionStatus.STOPPED;

    Queue<Task> pendingTasks = new ConcurrentLinkedQueue<Task>();
    Queue<Task> activeTasks = new ConcurrentLinkedQueue<Task>();

    public WorkSession(String sessionId, TransformationModel model)
    {
        if (model == null)
            throw new NullPointerException();

        this.sessionId = sessionId;
        this.model = model;
        
        for (Node node: model.getNodeList())
            createInstance(node);

        // Splitters first, because other nodes' states depend on them.
        for (NodeInstance nodeInstance: nodeInstances.values())
            if (nodeInstance.getOrigin().isSplitter())
                nodeInstance.prepareStates();

        for (NodeInstance nodeInstance: nodeInstances.values())
            if (nodeInstance.getOrigin().isSplitter() == false)
                nodeInstance.prepareStates();

        model.addListener(this);
    }
    
    public String getSessionId()
    {
        return sessionId;
    }

    public SessionStatus getSessionStatus()
    {
        return status;
    }

    // TODO: Need to figure out a better thread-safe way of doing these.
    public synchronized void startSession()
    {
        this.status = SessionStatus.STARTED;

        for (PackTransformerInstance packTransformerInstance :
            packTransformerInstances.values())
        {
            for (int i = 0; i < packTransformerInstance.subPackTransformers(); i++)
            {
                if (packTransformerInstance.getState(i) != SubPackTransformerState.PENDING)
                    continue;

                makeTask(packTransformerInstance, i);
            }
        }
    }

    private void makeTask(PackTransformerInstance packTransformerInstance,
            int subPackTransformer)
    {
        Command command = packTransformerInstance.origin.getCommand();
        Set<FileHandle> files = new HashSet<FileHandle>();
        Task task = new Task(UUID.randomUUID().toString(),
                new LinkedList<FileHandle>(files), command);

        if (sessionInfo != null)
        {
            task.setDownloadURI(sessionInfo.downloadUrl);
            task.setUploadURI(sessionInfo.uploadUrl);
        }

        // TODO: Change! *******************************************************   !!!
        task.setMainServerAddress("127.0.0.1"); // !!!

        pendingTasks.add(task);
    }

    public synchronized void stopSession()
    {
        if (this.status == SessionStatus.STARTED)
        {
            this.status = SessionStatus.STOPPING;

            if (activeTasks.size() == 0)
                this.status = SessionStatus.STOPPED;
        }
    }

    /** Returns a pending task whose dependencies are met.
     * This method is thread-safe.
     */
    public synchronized Task getPendingTask()
    {
        if (status != SessionStatus.STARTED)
            return null;

        Task task = pendingTasks.poll();

        if (task != null)
            activeTasks.add(task);

        return task;
    }

    /** Returns an uncompleted task; this task can be retrieved again with a
     * call to {@link getPendingTask()}.
     * This method is thread-safe.
     * @param task The uncompleted task to be returned.
     */
    public synchronized void returnTask(Task task)
    {
        activeTasks.remove(task);
        pendingTasks.add(task);

        if (status == SessionStatus.STOPPING && activeTasks.isEmpty())
            status = SessionStatus.STOPPED;
    }
    
    // TODO: Find out how to do that link.
    /** Marks a task as done, to allow other tasks that depend on it to be
     * returned by {@link getPendingTask()}.
     * This method is thread-safe.
     */
    public synchronized void markTaskAsFinished(Task task, TaskResult taskResult)
    {
        if (status == SessionStatus.STOPPING && activeTasks.isEmpty())
            status = SessionStatus.STOPPED;

        // TODO
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
            createInstance((Pack) node);
        else if (node instanceof PackTransformer)
            createInstance((PackTransformer) node);
    }

    private void createInstance(Pack pack)
    {
        PackInstance packInstance = new PackInstance(pack);
        this.nodeInstances.put(pack, packInstance);
        this.packInstances.put(pack, packInstance);
    }

    private void createInstance(PackTransformer packTransformer)
    {
        PackTransformerInstance packTransformerInstance =
            new PackTransformerInstance(packTransformer);
        this.nodeInstances.put(packTransformer, packTransformerInstance);
        this.packTransformerInstances.put(packTransformer,
                packTransformerInstance);
    }

    public void packAdded(Pack pack)
    {
        createInstance(pack);
    }

    public void packTransformerAdded(PackTransformer packTransformer)
    {
        createInstance(packTransformer);
    }

    public void outputAdded(Pack fromPack, PackTransformer toPackTransformer)
    {
//        PackTransformerInstance instance =
//                packTransformerInstances.get(toPackTransformer);
//        if (instance.isInitialized)
//            instance.prepareStates();
    }

    public void outputAdded(PackTransformer fromPackTransformer, Pack toPack)
    {
        packInstances.get(toPack).prepareStates();
    }

    public void patternChanged(Pack pack)
    {
        packInstances.get(pack).prepareStates();
    }

    // TOOD: Not sure if this is complete...
    public void splitterChanged(Node node)
    {
        nodeInstances.get(node).prepareStates();
    }

    public void setTargetNode(int id)
    {
        if (model.getPack(id) == null && model.getPackTransformer(id) == null)
            throw new InvalidIdException("A node with the ID " + id + " was " +
                    "not found.");

        targettedNodes.add(id);

        // TODO: finish this
    }

    public void setTargetNode(Node node)
    {
        setTargetNode(node.getId());
    }

    public void setTargetNode(NodeInstance nodeInstance)
    {
        setTargetNode(nodeInstance.getOrigin().getId());
    }

    // TODO: Need to find a better name.
    public boolean isUsedInTransformation(int id)
    {
        if (targettedNodes.contains(id))
            return true;

        // TODO: finish this
        return false;
    }

    public boolean isUsedInTransformation(Node node)
    {
        return isUsedInTransformation(node.getId());
    }

    public boolean isUsedInTransformation(NodeInstance nodeInstance)
    {
        return isUsedInTransformation(nodeInstance.getOrigin().getId());
    }

    protected class PackInfo
    {
        List<FileHandle> files = new LinkedList<FileHandle>();
    }

    public abstract class NodeInstance
    {
        public abstract Node getOrigin();

        protected abstract void prepareStates();
        protected abstract int addState();
    }
    
    public enum SubPackState { IS_EMPTY, HAS_FILES };
    public enum SubPackTransformerState { DEPENDENCIES_NOT_MET, PENDING, WORKING, DONE };

    public class PackInstance extends NodeInstance
    {
        private Pack origin;
        private int subPacks = 0;
        private List<PackInfo> stateInfo;
        private List<SubPackState> state;
        
        protected PackInstance(Pack origin)
        {
            this.origin = origin;
        }
        
        @Override
        public Pack getOrigin()
        {
            return origin;
        }

        public PackInstance getSplitter()
        {
            Pack splitter = origin.getSplitter();
            
            if (splitter == null)
                return null;
            else
                return packInstances.get(splitter);
        }
        
        public int subPacks()
        {
            return subPacks;
        }

        /** A pack may have one or more states, depending on whether the
         * pack (or one of its inputs) is a multipack.
         *
         * If one or more of the pack's inputs is uninitialized, this pack
         * will also be uninitialized, and a call to this method will throw
         * an exception. See also {@link isInitialized()}.
         * @param subPack The sub-pack in a multipack, starting with 0.
         * @return A {@link SubPackState}.
         */
        public SubPackState getState(int subPack)
        {
            return state.get(subPack);
        }

        // Not used?
//        public void resetState()
//        {
//            removeAllFiles();
//            stateInfo = null;
//            state = null;
//
//            for (PackTransformer output: origin.getOutputs())
//                packTransformerInstances.get(output).resetState();
//        }


        // TODO: That link does not work. Why?
        // This function looks good.
        // TODO: Okay, it looks good, but the javadoc is outdated.

        /** If this is not a multi-pack, it prepares a single state, set to
         * {@link SubPackState.IS_EMPTY}, and calls prepareStates() on all
         * outputs.
         *
         * If it is a multi-pack, then this does nothing.
         *
         * If it is not a multi-pack, but has multiple states due to being
         * bound to a multi-pack, this will initialize as many states as
         * the splitter's, all set to IS_EMPTY.
         */
        @Override
        protected void prepareStates()
        {
            PackInstance splitter = getSplitter();
            int nrOfStates;

            if (origin.isSplitter())
                nrOfStates = origin.allowsEmptyPack() ? 0 : 1;
            else if (splitter == null)
                nrOfStates = 1;
            else
                nrOfStates = splitter.subPacks();

            // TODO: Clean current state, if any.

            stateInfo = new ArrayList<PackInfo>(nrOfStates);
            state = new ArrayList<SubPackState>(nrOfStates);

            for (int i = 0; i < nrOfStates; i++)
            {
                state.add(SubPackState.IS_EMPTY);
                stateInfo.add(new PackInfo());
            }

            subPacks = nrOfStates;

            //if (nrOfStates != 0)
            //    for (PackTransformer output: origin.getOutputs())
            //        packTransformerInstances.get(output).prepareStates();
        }

        // ? Probably not used.
//        public void markAsReady()
//        {
//            if (subPacks == 0)
//                throw new InvalidOperationException("The pack does not have " +
//                        "any files inside it.");
//
//            if (subPacks != 1 && getSplitter() != null && !origin.isSplitter())
//                throw new InvalidOperationException("Cannot mark a whole pack " +
//                        "as ready when its number of sub-packs depends on " +
//                        "a different pack.");
//
//            for (int i = 0; i < subPacks; i++)
//                if (state.get(i) == SubPackState.IS_EMPTY)
//                    throw new InvalidOperationException("Some sub-packs are " +
//                            "empty.");
//
//            for (int i = 0; i < subPacks; i++)
//                markAsReady(i);
//
//            for (PackTransformer output: origin.getOutputs())
//                packTransformerInstances.get(output).prepareStates();
//        }

        // Probably not used either
//        public void markAsReady(int subPack)
//        {
////            if (!isInitialized())
////                throw new NodeNotInitializedException();
//
//            if (state.get(subPack) == SubPackState.IS_EMPTY)
//                throw new InvalidOperationException("Cannot mark as ready a " +
//                        "sub-pack that has no files.");
//
//            state.set(subPack, SubPackState.READY);
//        }

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
            return origin.acceptsFileName(fileName);
        }

        public void putFile(FileHandle file)
        {
            if (getSplitter() == null || (origin.isSplitter() &&
                    !origin.allowsEmptyPack() && getState(0) ==
                    SubPackState.IS_EMPTY))
                putFile(0, file);
            else if (origin.isSplitter())
                putFile(addState(), file);
            else
                throw new InvalidOperationException("Must specify a sub-pack " +
                        "to putFile() on packs that have multiple states.");
        }

        public void putFile(int subPack, FileHandle file)
        {
            stateInfo.get(subPack).files.add(file);
            state.set(subPack, SubPackState.HAS_FILES);
        }

        protected int addState()
        {
            stateInfo.add(new PackInfo());
            state.add(SubPackState.IS_EMPTY);
            subPacks += 1;

            if (origin.isSplitter())
            {
                // Going through the whole node list probably not the most
                // efficient way of doing it... but it's the easiest way to
                // avoid adding yet another node list or go into the
                // complications of recursion.
                for (Node node: model.getNodeList())
                    if (node.getSplitter() == this.origin &&
                            node != this.origin)
                        nodeInstances.get(node).addState();
            }

            return subPacks - 1;
        }
    }
    
    public class PackTransformerInstance extends NodeInstance
    {
        private PackTransformer origin;
        private List<SubPackTransformerState> state;
        
        protected PackTransformerInstance(PackTransformer origin)
        {
            this.origin = origin;
        }
        
        @Override
        public PackTransformer getOrigin()
        {
            return origin;
        }

        // Not used?
//        public void resetState()
//        {
//            state = null;
//
//            for (Pack output: origin.getOutputs())
//                packInstances.get(output).resetState();
//        }

        @Override
        protected void prepareStates()
        {
            PackInstance splitter = packInstances.get(origin.splitterPack);
            int nrOfStates = (splitter == null) ? 1 : splitter.subPacks();

            state = new ArrayList<SubPackTransformerState>(1);

            for (int i = 0; i < nrOfStates; i++)
                addState();

//            for (Pack output: origin.getOutputs())
//                packInstances.get(output).prepareStates();
        }

        @Override
        protected int addState()
        {
            state.add(SubPackTransformerState.DEPENDENCIES_NOT_MET);
            updateState(state.size() - 1);

            return state.size() - 1;
        }

        protected void updateState(int subPackTransformer)
        {
            //SubPackTransformerState state = SubPackTransformerState.PENDING;

            for (Pack input : origin.getInputs())
            {
                PackInstance packInstance = packInstances.get(input);

                if (origin.isJoiner())
                {
                    for (int i = 0; i < packInstance.subPacks(); i++)
                        if (packInstance.getState(subPackTransformer) !=
                                SubPackState.HAS_FILES)
                        {
                            state.set(subPackTransformer,
                                    SubPackTransformerState.DEPENDENCIES_NOT_MET);
                            return;
                        }
                }
                else
                {
                    if (packInstance.getState(subPackTransformer) !=
                            SubPackState.HAS_FILES)
                    {
                        state.set(subPackTransformer,
                                SubPackTransformerState.DEPENDENCIES_NOT_MET);
                        return;
                    }
                }
            }

            if (state.get(subPackTransformer) == SubPackTransformerState.DEPENDENCIES_NOT_MET)
                state.set(subPackTransformer, SubPackTransformerState.PENDING);

            // TODO: If a state can ever be downgraded from "pending" back to
            // "dependencies not met", then more code needs to be added.
        }

        private SubPackTransformerState getState(int subPackTransformer)
        {
            return state.get(subPackTransformer);
        }

        private int subPackTransformers()
        {
            return state.size();
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
