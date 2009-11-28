package rawi.common;

import java.rmi.*;

public interface MainServerInterface extends Remote
{
   ValidateXMLInfo validateXML(String xml)
           throws RemoteException;
}