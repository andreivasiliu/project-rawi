package clustercomputer;

import java.rmi.server.ServerNotActiveException;
import rawi.common.ClusterComputerStatus;
import rawi.common.Task;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.UUID;
import rawi.common.ClusterComputerInterface;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIServerModel;

/**
 *
 * @author andrei.arusoaie
 */
public class ClusterComputer extends RMIServerModel implements ClusterComputerInterface{
    String uuid = UUID.randomUUID().toString();

    public ClusterComputer() throws RemoteException {
        super(Ports.ClusterComputerPort);

    }

    /**
     * This function will be called by a RMI Client.
     * It executes the command, having the task arguments.
     * @param task
     * @param command
     * @throws IOException
     */
    public void execute(Task task) throws RemoteException {
        TaskThread tt = new TaskThread(this, task);
        tt.start();
    }

    public ClusterComputerStatus getStatus() throws RemoteException
    {
        try
        {
            ClusterComputerStatus status = new ClusterComputerStatus();
            status.id = uuid;
            status.processors = Runtime.getRuntime().availableProcessors();
            status.mainServerAddr = RemoteServer.getClientHost();
            return status;
        }
        catch (ServerNotActiveException ex)
        {
            throw new RuntimeException("Cannot get the client's host address", ex);
        }
    }
}
