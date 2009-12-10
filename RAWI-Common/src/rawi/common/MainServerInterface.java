package rawi.common;

import java.rmi.*;

public interface MainServerInterface extends Remote
{
   ValidateXMLInfo validateXML(String xml)
           throws RemoteException;

    void taskCompleted(int id) throws RemoteException;
}