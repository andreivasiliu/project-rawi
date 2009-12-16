package rawi.common;

import java.rmi.*;

public interface MainServerInterface extends Remote
{
    ValidateXMLInfo validateXML(String xml)
            throws RemoteException;
    void createSession(SessionInfo sessionInfo) throws RemoteException;

    void taskCompleted(int id) throws RemoteException;

    MainServerStatus requestStatus() throws RemoteException;
}