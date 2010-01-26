package rawi.mainserver;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
import rawi.common.exceptions.InvalidIdException;
import rawi.mainserver.WorkSession.PackInstance;
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
        workSession.printStatus();

        wakeUp();
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
        {
            System.out.println(" - " + ip);
        }

        String ip;

        while ((ip = IPs.poll()) != null)
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

    private ClusterComputer getClusterComputer(String id)
    {
        ClusterComputer clusterComputer;

        if ((clusterComputer = computerById.get(id)) == null)
        {
            clusterComputer = new ClusterComputer();
            clusterComputer.id = id;
            computerById.put(id, clusterComputer);
            computerList.add(clusterComputer);
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
    }

    private class TaskGiver implements Runnable
    {
        private final Task task;
        private final ClusterManager.ClusterComputer clusterComputer;

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
            catch (RemoteException ex)
            {
                clusterComputer.unresponsive = true;
                clusterComputer.takeTask(task);
                retryTask(task);
            }
            catch (NotBoundException ex)
            {
                clusterComputer.unresponsive = true;
                clusterComputer.takeTask(task);
                retryTask(task);
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