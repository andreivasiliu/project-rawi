package rawi.common;

import java.rmi.*;

public interface WebServerInterface extends Remote
{
   void logMessage(String source, String severity, String message)
           throws RemoteException;
}