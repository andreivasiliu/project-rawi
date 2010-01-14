package rawi;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;
import rawi.common.FileHandle;
import rawi.common.MainServerInterface;
import rawi.common.MainServerStatus;
import rawi.common.Ports;
import rawi.common.SessionInfo;
import rawi.common.TaskResult;
import rawi.common.ValidateXMLInfo;
import rawi.mainserver.ClusterManager;
import rawi.mainserver.TransformationModel;
import rawi.mainserver.WorkSession;
import rawi.mainserver.XML.TransformationModelParser;
import rawi.rmiinfrastructure.RMIServerModel;

public class RMIMainServer extends RMIServerModel
        implements MainServerInterface
{
    SessionInfo sessionInfo;
    private String id = UUID.randomUUID().toString();

    private ClusterManager clusterManager;

    public RMIMainServer(ClusterManager clusterManager) throws RemoteException
    {
        super(Ports.MainServerPort);
        this.clusterManager = clusterManager;
    }

    public ValidateXMLInfo validateXML(String xml) throws RemoteException
    {
        System.out.println("Received xml: " +  xml.toString());
        if (xml.contains("hello"))
            return new ValidateXMLInfo(true, "Validation succeeded", 0);
        else
            return new ValidateXMLInfo(false, "You must say hello", 1);
    }

    public void createSession(SessionInfo sessionInfo) throws RemoteException {
        try
        {
            this.sessionInfo = sessionInfo;
            Reader modelReader = new StringReader(sessionInfo.modelXml);
            TransformationModel model = TransformationModelParser.parseFromXML(modelReader);
            WorkSession workSession = new WorkSession(sessionInfo.sessionId, model);
            clusterManager.addWorkSession(workSession);

            System.out.println("Created session: " + sessionInfo.sessionId +
                    "\n\t Download URL = " + sessionInfo.downloadUrl +
                    "\n\t Upload URL = " + sessionInfo.uploadUrl +
                    "\n\t Message log ip = " + sessionInfo.msgLogIp);
        }
        catch (SAXException ex)
        {
            // TODO: Somehow notify the webserver of this
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            // TODO: Somehow notify the webserver of this
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void notifyPresence(Collection<String> IPs) throws RemoteException
    {
        clusterManager.addIpsToScan(IPs);
    }
    
    public MainServerStatus requestStatus() throws RemoteException
    {
        MainServerStatus status = new MainServerStatus();
        System.out.println("Received status request.");
        status.id = id;
        status.numberOfClusterComputers = clusterManager
                .getNumberOfClusterComputers();
        status.totalNumberOfProcessors = clusterManager
                .getTotalNumberOfProcessors();

        return status;
    }

    public void taskCompleted(String id, String clusterComputerId,
            List<FileHandle> files) throws RemoteException
    {
        System.out.println("Task " + id + " completed.");
        System.out.println("Files :");
        for (FileHandle f:files)
            System.out.println(" ---> " + f.getLogicalName());
        System.out.println("uploaded.");

        TaskResult taskResult = new TaskResult(id, clusterComputerId, files);

        clusterManager.taskCompleted(taskResult);
    }

    public boolean putFileInPack(String sessionId, FileHandle file, String packId)
            throws RemoteException
    {
        return clusterManager.putFileInPack(sessionId, file, packId);
    }

    public void startSession(String sessionId) throws RemoteException
    {
        clusterManager.startWorkSession(sessionId);
    }

    public void stopSession(String sessionId) throws RemoteException
    {
        clusterManager.stopWorkSession(sessionId);
    }
}

