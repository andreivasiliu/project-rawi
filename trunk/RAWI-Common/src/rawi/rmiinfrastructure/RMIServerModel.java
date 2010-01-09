package rawi.rmiinfrastructure;

import java.net.InetAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;


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

    public void shutdownRMI()
    {
        try
        {
            UnicastRemoteObject.unexportObject(this.getRegistry(), true);
        }
        catch (NoSuchObjectException ex)
        {
            Logger.getLogger(RMIServerModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
