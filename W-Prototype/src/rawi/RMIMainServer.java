package rawi;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import rawi.common.FileHandle;
import rawi.common.MainServerInterface;
import rawi.common.MainServerStatus;
import rawi.common.Ports;
import rawi.common.SessionInfo;
import rawi.common.TaskResult;
import rawi.common.ValidateXMLInfo;
import rawi.mainserver.ClusterManager;
import rawi.rmiinfrastructure.RMIServerModel;

public class RMIMainServer extends RMIServerModel
        implements MainServerInterface {

    SessionInfo sessionInfo;
    private String id = UUID.randomUUID().toString();

    private ClusterManager clusterManager;

    public RMIMainServer(ClusterManager clusterManager) throws RemoteException {
        super(Ports.MainServerPort);
        this.clusterManager = clusterManager;
    }

    public ValidateXMLInfo validateXML(String xml) throws RemoteException {
        System.out.println("Received xml: " +  xml.toString());
        if (xml.contains("hello"))
            return new ValidateXMLInfo(true, "Validation succeeded", 0);
        else
            return new ValidateXMLInfo(false, "You must say hello", 1);
    }

    public void createSession(SessionInfo sessionInfo) throws RemoteException {
        this.sessionInfo = sessionInfo;

        System.out.println("Created session: " + sessionInfo.sessionId
                + "\n\t Download URL = " + sessionInfo.downloadUrl
                + "\n\t Upload URL = " + sessionInfo.uploadUrl
                + "\n\t Message log ip = " + sessionInfo.msgLogIp);
    }

    public void taskCompleted(int id) throws RemoteException {
            throw new UnsupportedOperationException("Not supported yet.");
    }

    public void notifyPresence(Collection<String> IPs) throws RemoteException
    {
        clusterManager.addIpsToScan(IPs);
    }
    
    public MainServerStatus requestStatus() throws RemoteException {
        MainServerStatus status = new MainServerStatus();
        System.out.println("Received status request.");
        status.id = id;
        status.numberOfClusterComputers = clusterManager
                .getNumberOfClusterComputers();
        status.totalNumberOfProcessors = clusterManager
                .getTotalNumberOfProcessors();

        return status;
    }

    public void taskCompleted(String id, String clusterComputerId, List<FileHandle> files) throws RemoteException
    {
        System.out.println("Task " + id + " completed.");
        System.out.println("Files :");
        for (FileHandle f:files)
            System.out.println(" ---> " + f.getLogicalName());
        System.out.println("uploaded.");

        TaskResult taskResult = new TaskResult(id, clusterComputerId, files);

        clusterManager.taskCompleted(taskResult);
    }
}

