package rawi.common;

import java.rmi.*;
import java.util.Collection;

public interface MainServerInterface extends Remote
{
    /** Used by a WebServer to validate a TransformationModel XML. */
    ValidateXMLInfo validateXML(String xml) throws RemoteException;

    /** Used by a WebServer to create a WorkSession. */
    void createSession(SessionInfo sessionInfo) throws RemoteException;

    /** Used by a ClusterComputer to mark a task as finished. */
    void taskCompleted(int id) throws RemoteException;

    /** Used by a ClusterComputer to make its presence known to the MainServer. */
    void notifyPresence(Collection<String> IPs) throws RemoteException;

    MainServerStatus requestStatus() throws RemoteException;
}