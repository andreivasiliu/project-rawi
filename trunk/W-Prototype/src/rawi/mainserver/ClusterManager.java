package rawi.mainserver;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import rawi.common.ClusterComputerInterface;
import rawi.common.ClusterComputerStatus;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

/** A manager that keeps and updates a list of working sessions.
 */
public class ClusterManager implements Runnable
{
    List<WorkSession> sessionList = new LinkedList<WorkSession>();
    List<ClusterComputer> computerList = new LinkedList<ClusterComputer>();
    Map<String, ClusterComputer> computerById = new HashMap<String, ClusterComputer>();

    final Queue<String> ipsToScan = new ConcurrentLinkedQueue<String>();
    final Object semaphore = new Object();

    public synchronized void run()
    {
        mainLoop();
    }

    private void mainLoop()
    {
        while (true)
        {
            if (!ipsToScan.isEmpty())
                scanForComputers(ipsToScan);
            
            // check finished/failed tasks


            assignTaskToComputer();

            synchronized (semaphore)
            {
                try {
                    semaphore.wait(200);
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
     */
    private Task getPendingTask()
    {
        for (WorkSession workSession : sessionList)
        {
            if (workSession.getSessionStatus() != WorkSession.SessionStatus.STARTED)
                continue;

            Task task = workSession.getPendingTask();

            if (task != null)
                return task;
        }

        return null;
    }

    private ClusterComputer getAvailableClusterComputer()
    {
        ClusterComputer bestClusterComputer = null;

        for (ClusterComputer clusterComputer : computerList)
        {
            ClusterComputerStatus status = clusterComputer.status;

            if (status.used_processors >= status.processors)
                continue;

            if (bestClusterComputer == null || status.used_processors <
                    bestClusterComputer.status.used_processors)
                bestClusterComputer = clusterComputer;
        }

        return bestClusterComputer;
    }

    private void assignTaskToComputer()
    {
        ClusterComputer clusterComputer;
        Task task;

        if ((clusterComputer = getAvailableClusterComputer()) == null)
            return;

        if ((task = getPendingTask()) == null)
            return;

        clusterComputer.assignTask(task);
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

        synchronized (semaphore) {
            semaphore.notify();
        }
    }

    private void scanForComputers(Queue<String> IPs)
    {
        System.out.println("Scanning for computers...");
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

    private class ClusterComputer
    {
        String id;
        ClusterComputerStatus status;
        String ipAddress;

        private void assignTask(Task task)
        {
            System.out.println("Assigning task to computer.");
            throw new UnsupportedOperationException("Not yet implemented");
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
