package rawi.common;

import java.rmi.*;
import java.util.Collection;
import java.util.List;

public interface MainServerInterface extends Remote
{

    /** Used by a WebServer to validate a TransformationModel XML. */
    ValidateXMLInfo validateXML(String xml) throws RemoteException;

    /** Used by a WebServer to create a WorkSession. */
    void createSession(SessionInfo sessionInfo) throws RemoteException;

    /** Used by a WebServer to start a WorkSession. */
    void startSession(String sessionId) throws RemoteException;

    /** Used by a WebServer to stop a WorkSession. */
    void stopSession(String sessionId) throws RemoteException;

    /** Used by a ClusterComputer to mark a task as finished. */
    void taskCompleted(String id, String clusterComputerId,
            List<FileHandle> files) throws RemoteException;

    /** Used by a ClusterComputer to mark a task as failed. */
    void taskFailed(String id, String clusterComputerId, Throwable exception,
            boolean impossibleTask) throws RemoteException;

    /** Used by a ClusterComputer to make its presence known to the MainServer. */
    void notifyPresence(Collection<String> IPs) throws RemoteException;

    MainServerStatus requestStatus() throws RemoteException;

    /** Used by a WebServer to associate an uploaded file with a pack. */
    boolean putFileInPack(String sessionId, FileHandle file, String packId)
            throws RemoteException;

    WorkSessionStatus getSessionStatus(String sessionId) throws RemoteException;

}
