package classes;

import java.rmi.*;

public interface LogMessageInterface extends Remote
{
   void logMessage(String source, String severity, String message)
           throws RemoteException;
}