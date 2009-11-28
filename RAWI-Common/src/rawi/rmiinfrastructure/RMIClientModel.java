package rawi.rmiinfrastructure;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClientModel<T> {
    T rmiServer;

    public RMIClientModel(int port)
            throws RemoteException, NotBoundException {
        Registry registry;
        String serverAddress = "127.0.0.1";

        // get the “registry”
        registry = LocateRegistry.getRegistry(serverAddress, port);

        // look up the remote object
        rmiServer = (T) (registry.lookup("rmiServer"));
    }

    public T getInterface()
    {
        return rmiServer;
    }
}
