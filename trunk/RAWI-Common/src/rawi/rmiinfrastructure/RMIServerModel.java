package rawi.rmiinfrastructure;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public abstract class RMIServerModel extends java.rmi.server.UnicastRemoteObject {

    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    public Registry getRegistry() {
        return registry;
    }

    public RMIServerModel(int port) throws RemoteException {
        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("can't get inet address.");
        }
        System.out.println("this address=" + thisAddress + ",port=" + port);
        try {
            // create the registry and bind the name and object.
            registry = LocateRegistry.createRegistry(port);
            registry.rebind("rmiServer", this);
        } catch (RemoteException e) {
            throw e;
        }
    }
    
}
