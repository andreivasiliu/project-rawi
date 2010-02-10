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
import rawi.common.WorkSessionStatus;
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
        //System.out.println("Received xml: " +  xml.toString());

        try
        {
            Reader modelReader = new StringReader(xml);
            TransformationModelParser.parseFromXML(modelReader);

            return new ValidateXMLInfo(true, "Validation succeeded", 0);
        }
        catch (Exception ex)
        {
            return new ValidateXMLInfo(false, ex.toString(), 0);
        }
    }

    public void createSession(SessionInfo sessionInfo) throws RemoteException {
        try
        {
            this.sessionInfo = sessionInfo;
            Reader modelReader = new StringReader(sessionInfo.modelXml);
            TransformationModel model = TransformationModelParser.parseFromXML(modelReader);
            WorkSession workSession = new WorkSession(sessionInfo.sessionId, model);
            workSession.setSessionInfo(sessionInfo);
            workSession.printStatus();
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

    /* This methos is required to return AFTER the task has been removed
     * from the ClusterManager's list of active tasks. */
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
        System.out.println("Put File in Pack request received: "
                + "\t sessionId = " + sessionId
                + "\t fileName = " + file.getLogicalName() 
                + "\t packId = " + packId);
        return clusterManager.putFileInPack(sessionId, file, packId);
    }

    public void startSession(String sessionId) throws RemoteException
    {
        System.out.println("Start session request. Session id = " + sessionId);
        clusterManager.startWorkSession(sessionId);
    }

    public void stopSession(String sessionId) throws RemoteException
    {
        System.out.println("Stop session request. Session id = " + sessionId);
        clusterManager.stopWorkSession(sessionId);
    }

    public WorkSessionStatus getSessionStatus(String sessionId) throws RemoteException
    {
        System.out.println("Session status requested.");
        return clusterManager.getSessionStatus(sessionId);
    }

    public void taskFailed(String id, String clusterComputerId, Throwable exception,
            boolean impossibleTask) throws RemoteException
    {
        System.out.println("Received exception from " + clusterComputerId +
                " regarding task " + id);
        System.out.println("Exception received: " + exception.getMessage());
        clusterManager.taskFailed(id, clusterComputerId, impossibleTask);
    }
}

