package rmiinfrastructure;

import java.rmi.*;

public interface ServerInterface extends Remote
{
   ReturnMessage receiveMessage(ParamMessage pm) throws RemoteException;
}