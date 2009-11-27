package rmiinfrastructure;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RMIServer extends java.rmi.server.UnicastRemoteObject
        implements ServerInterface {

    int thisPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    // This method is called from the remote client by the RMI.
    // This is the implementation of the “ReceiveMessageInterface”.
    public ReturnMessage receiveMessage(ParamMessage m) throws RemoteException {
        System.out.println("Received message: " + m.paramMessage);
        return new ReturnMessage("This is the return message");
    }

    public RMIServer() throws RemoteException {
        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("can't get inet address.");
        }
        thisPort = 3232;  // this port(registry’s port)
        System.out.println("this address=" + thisAddress + ",port=" + thisPort);
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(thisPort);
            registry.rebind("rmiServer", this);
        } catch (RemoteException e) {
            throw e;
        }
    }

    static public void main(String args[]) {
        try {
            RMIServer s = new RMIServer();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
