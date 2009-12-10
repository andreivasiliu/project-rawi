package rawi.common;

import java.rmi.*;

public interface MainServerInterface extends Remote
{
    ValidateXMLInfo validateXML(String xml)
            throws RemoteException;
    void startSession(String download, String upload, String msgLog,
            long sessionId) throws RemoteException;

    void taskCompleted(int id) throws RemoteException;
}