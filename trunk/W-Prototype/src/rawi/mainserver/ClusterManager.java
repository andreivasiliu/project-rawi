package rawi.mainserver;

import rawi.common.TaskStatus;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rawi.common.ClusterComputerInterface;
import rawi.common.ClusterComputerStatus;
import rawi.common.FileHandle;
import rawi.common.Ports;
import rawi.common.Task;
import rawi.common.TaskResult;
import rawi.common.WorkSessionStatus;
import rawi.common.exceptions.InvalidIdException;
import rawi.mainserver.WorkSession.PackInstance;
import rawi.mainserver.WorkSession.PackTransformerInstance;
import rawi.rmiinfrastructure.RMIClientModel;

/** A manager that keeps and updates a list of working sessions.
 */
public class ClusterManager implements Runnable
{
    final List<WorkSession> sessionList = new LinkedList<WorkSession>();
    final Map<String, WorkSession> sessionById = new HashMap<String, WorkSession>();
    final List<ClusterComputer> computerList = new LinkedList<ClusterComputer>();
    final Map<String, ClusterComputer> computerById = new HashMap<String, ClusterComputer>();

    final Queue<String> ipsToScan = new ConcurrentLinkedQueue<String>();
    final Map<Task, WorkSession> taskOwner = new ConcurrentHashMap<Task, WorkSession>();
    final Map<String, Task> taskById = new ConcurrentHashMap<String, Task>();
    final ExecutorService threadPool = Executors.newCachedThreadPool();
    final Object clusterManagerLock = new Object();
    boolean shutdown = false;

    public synchronized void run()
    {
        mainLoop();
    }


    public void shutdown()
    {
        synchronized (clusterManagerLock)
        {
            if (shutdown == true)
                return;

            shutdown = true;
            threadPool.shutdown();
            clusterManagerLock.notifyAll();
        }
    }

    private void mainLoop()
    {
        while (true)
        {
            synchronized (clusterManagerLock)
            {
                if (shutdown)
                    return;

                if (!ipsToScan.isEmpty())
                    scanForComputers(ipsToScan);

                if (assignTaskToComputer())
                    continue;

                try {
                    clusterManagerLock.wait(200);
                } catch (InterruptedException ex) {
                    // Silently ignore
                }
            }
        }
    }

    // Make sure these are always thread-safe.
    public int getNumberOfClusterComputers()
    {
        return computerList.size();
    }

    public int getTotalNumberOfProcessors()
    {
        int processors = 0;

        for (ClusterComputer clusterComputer: computerList)
            processors += clusterComputer.status.processors;

        return processors;
    }

    /** Finds a pending task from any of the managed working sessions.
     * This method is thread-safe.
     */
    private Task getPendingTask()
    {
        Task task;

        for (WorkSession workSession : sessionList)
        {
            if ((task = workSession.getPendingTask()) == null)
                continue;

            taskOwner.put(task, workSession);
            taskById.put(task.getId(), task);
            return task;
        }

        return null;
    }

    /**
     * Called when an attempt to give a task to a computer was not successful. A
     * call to getPendingTask will eventually return this task again.
     * This method is thread-safe.
     *
     * @param task The task to be put back into the "Pending Tasks" queue.
     */
    private void retryTask(Task task)
    {
        WorkSession workSession = taskOwner.remove(task);

        if (workSession == null)
        {
            System.err.println("Warning: A task did not have an associated " +
                    "work session.");
            return;
        }

        workSession.returnTask(task);

        // Must do something
    }

    public void taskCompleted(TaskResult taskResult)
    {
        Task task = taskById.remove(taskResult.id);

        if (task == null)
        {
            System.err.println("Warning: Received results to an unknown task.");
            return;
        }

        ClusterComputer computer = computerById.get(taskResult.clusterComputerId);
        if (computer != null)
            computer.takeTask(task);

        WorkSession workSession = taskOwner.remove(task);

        if (workSession == null)
        {
            System.err.println("Warning: A task did not have an associated " +
                    "work session.");
            return;
        }

        workSession.markTaskAsFinished(task, taskResult);
        //workSession.printStatus();

        wakeUp();
    }

    public void taskFailed(String id, String clusterComputerId, boolean impossibleTask)
    {
        ClusterComputer clusterComputer = computerById.get(clusterComputerId);
        Task task = taskById.get(id);
        // TODO: check if null.

        clusterComputer.takeTask(task);
        // TODO: a temporary hack; this needs to be handled better.
        clusterComputer.unresponsive = true;
        retryTask(task);
    }

    public void addWorkSession(WorkSession workSession)
    {
        sessionList.add(workSession);
        sessionById.put(workSession.getSessionId(), workSession);
        wakeUp();
    }

    public void startWorkSession(String sessionId)
    {
        WorkSession session = getSessionById(sessionId);

        session.startSession();
        wakeUp();
    }

    public void waitUntilStopped(String sessionId)
    {
        WorkSession workSession = getSessionById(sessionId);

        synchronized (clusterManagerLock)
        {
            while (true)
            {
                try
                {
                    if (workSession.getSessionStatus() ==
                            WorkSession.SessionStatus.STOPPED)
                        return;

                    clusterManagerLock.wait();
                }
                catch (InterruptedException ex)
                {
                    // Silently ignore.
                }
            }
        }
    }

    public void stopWorkSession(String sessionId)
    {
        WorkSession workSession = getSessionById(sessionId);

        workSession.stopSession();
    }

    public boolean putFileInPack(String sessionId, FileHandle file, String packId)
    {
        WorkSession session = getSessionById(sessionId);

        PackInstance packInstance = session.getPackInstance(Integer.parseInt(packId));

        if (!packInstance.acceptsFileName(file.getLogicalName()))
            return false;

        packInstance.putFile(file);
        return true;
    }

    public void wakeUp()
    {
        synchronized (clusterManagerLock)
        {
            clusterManagerLock.notifyAll();
        }
    }

    private ClusterComputer getAvailableClusterComputer()
    {
        ClusterComputer bestClusterComputer = null;

        for (ClusterComputer clusterComputer : computerList)
        {
            ClusterComputerStatus status = clusterComputer.status;

            if (clusterComputer.taskCount() >= status.processors + 2 ||
                    clusterComputer.unresponsive)
                continue;

            if (bestClusterComputer == null || clusterComputer.taskCount() <
                    bestClusterComputer.taskCount())
                bestClusterComputer = clusterComputer;
        }

        return bestClusterComputer;
    }

    private boolean assignTaskToComputer()
    {
        ClusterComputer clusterComputer;
        Task task;

        if ((clusterComputer = getAvailableClusterComputer()) == null)
            return false;

        if ((task = getPendingTask()) == null)
            return false;

        clusterComputer.giveTask(task);
        System.out.println("Sent task " + task.getId() + " to " +
                clusterComputer.ipAddress);
        return true;
    }

    /**
     * This method will probably be called from a different thread, to notify
     * the Cluster Manager that it should look for Cluster Computers at the
     * given IPs. The method returns immediately, before the scan begins.
     *
     * @param IPs A collection of IPs that should be scanned.
     */
    public void addIpsToScan(Collection<String> IPs)
    {
        ipsToScan.addAll(IPs);

        synchronized (clusterManagerLock)
        {
            clusterManagerLock.notify();
        }
    }

    public void addIpToScan(String IP)
    {
        ipsToScan.add(IP);

        synchronized (clusterManagerLock)
        {
            clusterManagerLock.notify();
        }
    }

    private void scanForComputers(Queue<String> IPs)
    {
        System.out.println("Scanning for computers...");
        for (String ip : IPs)
            System.out.println(" - " + ip);

        String ip;
        while ((ip = IPs.poll()) != null)
            new Thread(new IPScanner(ip)).start();
    }

    private ClusterComputer getClusterComputer(String id)
    {
        ClusterComputer clusterComputer;

        synchronized (clusterManagerLock)
        {
            if ((clusterComputer = computerById.get(id)) == null)
            {
                clusterComputer = new ClusterComputer();
                clusterComputer.id = id;
                computerById.put(id, clusterComputer);
                computerList.add(clusterComputer);
            }
        }

        return clusterComputer;
    }

    private WorkSession getSessionById(String sessionId)
    {
        WorkSession session = sessionById.get(sessionId);

        if (session == null)
            throw new InvalidIdException("No work session with id '" +
                    sessionId + "' is known.");

        return session;
    }

    public WorkSessionStatus getSessionStatus(String sessionId)
    {
        return getSessionStatus(sessionId, 0, 10);
    }

    // Probably one of the ugliest methods ever.
    public WorkSessionStatus getSessionStatus(String sessionId,
            int subStatesOffset, int maxSubStates)
    {
        System.out.println("Get session status with offset: " + subStatesOffset +
                " and max sub states: " + maxSubStates);

        WorkSession session = getSessionById(sessionId);
        WorkSessionStatus sessionStatus = new WorkSessionStatus();

        sessionStatus.timeUntilStopped = session.timeUntilStopped;
        sessionStatus.working = session.getSessionStatus() !=
                WorkSession.SessionStatus.STOPPED;

        for (PackInstance packInstance : session.packInstances.values())
        {
            WorkSessionStatus.Pack pack = sessionStatus.addPack(
                    packInstance.getOrigin().getId());
            pack.name = packInstance.getOrigin().getName();
            pack.x = packInstance.getOrigin().getCoordX();
            pack.y = packInstance.getOrigin().getCoordY();
            pack.subStates = packInstance.subPacks();

            // FIXME: Just counting them for now.
            for (int i = 0; i < packInstance.subPacks(); i++)
                if (packInstance.getState(i) == WorkSession.SubPackState.HAS_FILES)
                    pack.fullSubPacks++;
                else
                    pack.emptySubPacks++;
            pack.subStatesShown = Math.min(maxSubStates, pack.subStates);
            pack.subStateOffset = Math.min(subStatesOffset,
                    Math.max(0, pack.subStates - pack.subStatesShown));

            pack.status = new ArrayList<WorkSessionStatus.PackStatus>(packInstance.subPacks());
            pack.subStateFiles = new ArrayList<Collection<FileHandle>>(packInstance.subPacks());
            for (int i = 0; i < packInstance.subPacks(); i++)
            {
                if (packInstance.getState(i) == WorkSession.SubPackState.HAS_FILES)
                    pack.addState(WorkSessionStatus.PackStatus.HAS_FILES,
                            packInstance.getFiles(i));
                else
                    pack.addState(WorkSessionStatus.PackStatus.EMPTY, null);
            }

            if (packInstance.getOrigin().getSplitter() != null)
                pack.isMulti = true;
        }

        for (PackTransformerInstance packTransformerInstance :
                session.packTransformerInstances.values())
        {
            WorkSessionStatus.PackTransformer packTransformer =
                    sessionStatus.addPackTransformer(packTransformerInstance
                    .getOrigin().getId());
            packTransformer.name = packTransformerInstance.getOrigin().getName();
            packTransformer.x = packTransformerInstance.getOrigin().getCoordX();
            packTransformer.y = packTransformerInstance.getOrigin().getCoordY();
            packTransformer.subStates = packTransformerInstance.subPackTransformers();

            // FIXME: Just counting them for now. Not scalable.
            for (int i = 0; i < packTransformerInstance.subPackTransformers(); i++)
            {
                WorkSession.SubPackTransformerState subState = packTransformerInstance.getState(i);
                if (subState == WorkSession.SubPackTransformerState.DONE)
                    packTransformer.doneTasks++;
                else if (subState == WorkSession.SubPackTransformerState.WORKING)
                    packTransformer.workingTasks++;
                else if (subState == WorkSession.SubPackTransformerState.PENDING)
                    packTransformer.pendingTasks++;
                else if (subState == WorkSession.SubPackTransformerState.DEPENDENCIES_NOT_MET)
                    packTransformer.dnmTasks++;
            }
            packTransformer.subStatesShown = Math.min(maxSubStates, packTransformer.subStates);
            packTransformer.subStateOffset = Math.min(subStatesOffset,
                    Math.max(0, packTransformer.subStates - packTransformer.subStatesShown));

            packTransformer.status = new ArrayList<WorkSessionStatus.PackTransformerStatus>
                    (packTransformerInstance.subPackTransformers());
            for (int i = 0; i < packTransformerInstance.subPackTransformers(); i++)
            {
                packTransformer.status.add(WorkSessionStatus.PackTransformerStatus.valueOf(packTransformerInstance.getState(i).name()));
            }

            if (packTransformerInstance.getOrigin().getSplitter() != null)
                packTransformer.isMulti = true;
        }

        for (WorkSession.NodeInstance nodeInstance : session.nodeInstances.values())
        {
            for (TransformationModel.Node output : nodeInstance.getOrigin().getOutputs())
                sessionStatus.addOutput(nodeInstance.getOrigin().getId(),
                    output.getId());
        }

        return sessionStatus;
    }

    private class ClusterComputer
    {
        String id;
        ClusterComputerStatus status;
        String ipAddress;
        boolean unresponsive = false;

        private Set<Task> activeTasks = new HashSet<Task>();

        synchronized void giveTask(Task task)
        {
            activeTasks.add(task);
            task.setMainServerAddress(status.mainServerAddr);
            TaskGiver taskGiver = new TaskGiver(task, this);
            threadPool.execute(taskGiver);
        }

        synchronized boolean takeTask(Task task)
        {
            return activeTasks.remove(task);
        }

        synchronized int taskCount()
        {
            return activeTasks.size();
        }

        synchronized boolean hasTask(Task task)
        {
            return activeTasks.contains(task);
        }
    }

    private class IPScanner implements Runnable
    {
        private String ip;

        public IPScanner(String ip)
        {
            this.ip = ip;
        }

        public void run()
        {
            try
            {
                ClusterComputerInterface cci =
                        new RMIClientModel<ClusterComputerInterface>(ip,
                        Ports.ClusterComputerPort).getInterface();

                ClusterComputerStatus status = cci.getStatus();

                ClusterComputer clusterComputer = getClusterComputer(status.id);
                clusterComputer.status = status;
                clusterComputer.ipAddress = ip;

                System.out.println("Found computer with" +
                        " ID " +  status.id +
                        " at " + ip +
                        " (" + status.processors + " processors)");
            }
            catch (NotBoundException ex)
            {
                // Silently ignore
            }
            catch (RemoteException ex)
            {
                // Silently ignore
            }
        }
    }

    private class TaskGiver implements Runnable
    {
        private final Task task;
        private final ClusterManager.ClusterComputer clusterComputer;
        private int monitorFailures = 0;

        public TaskGiver(Task task, ClusterManager.ClusterComputer clusterComputer)
        {
            this.task = task;
            this.clusterComputer = clusterComputer;
        }

        public void run()
        {
            giveTaskToComputer();
        }

        private void giveTaskToComputer()
        {
            try
            {
                ClusterComputerInterface cci =
                        new RMIClientModel<ClusterComputerInterface>(
                        clusterComputer.ipAddress, Ports.ClusterComputerPort)
                        .getInterface();

                cci.execute(task);
            }
            catch (Exception ex)
            {
                System.out.println("Delivery of task " + task.getId().toString() +
                        " failed. Exception: " + ex.toString());
                clusterComputer.unresponsive = true;
                clusterComputer.takeTask(task);
                retryTask(task);
                return;
            }

            monitorClusterComputer();
        }

        // TODO: The exception messages can probably be improved.
        private void monitorClusterComputer()
        {
            while (true)
            {
                try
                {
                    TaskStatus taskStatus;

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ex)
                    {
                        // Do nothing
                    }

                    try
                    {
                        ClusterComputerInterface cci =
                                new RMIClientModel<ClusterComputerInterface>(
                                clusterComputer.ipAddress, Ports.ClusterComputerPort)
                                .getInterface();

                        taskStatus = cci.taskStatus(task);
                    }
                    catch (NotBoundException ex)
                    {
                        if (++monitorFailures >= 3)
                            throw new Exception("Failed to contact the " +
                                    "ClusterComputer in three tries.", ex);
                        else
                            continue;
                    }
                    catch (RemoteException ex)
                    {
                        if (++monitorFailures >= 3)
                            throw new Exception("Failed to contact the " +
                                    "ClusterComputer in three tries.", ex);
                        else
                            continue;
                    }

                    if (!clusterComputer.hasTask(task))
                        return;

                    // This was a successful RMI call, so reset the counter.
                    monitorFailures = 0;

                    switch (taskStatus.getStatus())
                    {
                        case COMPLETED:
                            throw new Exception("The ClusterComputer reported the " +
                                    "task as completed, yet it is still registered " +
                                    "as active.");
                        case FAILED:
                            throw new Exception("The ClusterComputer reported the " +
                                    "task as failed, yet it is still registered " +
                                    "as active.");
                        case INEXISTENT:
                            throw new Exception("The ClusterComputer no longer " +
                                    "knows anything about this task.");

                        case RUNNING:
                            // Do nothing
                        }
                }
                catch (Exception ex)
                {
                    System.out.println("While monitoring computer at " +
                            clusterComputer.ipAddress +
                            " for task " + task.getId());
                    System.out.println("Exception: " + ex.toString());
                    Throwable cause = ex.getCause();
                    while (cause != null)
                    {
                        System.out.println("Caused by: " + cause);
                        cause = cause.getCause();
                    }

                    clusterComputer.unresponsive = true;
                    clusterComputer.takeTask(task);
                    retryTask(task);
                    return;
                }

                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException ex)
                {
                    // Do nothing
                }
            }
        }
    }

    /* Events:
     *
     * session is created
     * session is started/stopped
     * session is destroyed
     * cluster computer announces itself
     * cluster computer quits
     * cluster computer finished a task
     * cluster computer failed a task because it cannot do it
     * cluster computer failed a task because it cannot be done
     */
}
