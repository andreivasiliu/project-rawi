package rawi.common;

import java.rmi.*;

public interface MainServerInterface extends Remote
{
   ValidateXMLInfo validateXml(String xml)
           throws RemoteException;
}