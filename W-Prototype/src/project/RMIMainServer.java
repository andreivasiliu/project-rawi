
package project;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import rawi.common.MainServerInterface;
import rawi.common.RMIMessage;
import rawi.common.ValidateXMLInfo;

public class RMIMainServer extends java.rmi.server.UnicastRemoteObject
        implements MainServerInterface {

    int thisPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.
    List<RMIMessage> messageList = new ArrayList<RMIMessage>();

    // This method is called from the remote client by the RMI.
    // This is the implementation of the “WebServerInterface”.
    public ValidateXMLInfo validateXml(String message)
            throws RemoteException {

        System.out.println("Received message: " +  message);
        return new ValidateXMLInfo(true, "Validation succeeded", 0);
    }

    public RMIMainServer() throws RemoteException {
        try {
            // get the address of this host.
            thisAddress = (InetAddress.getLocalHost()).toString();
        } catch (Exception e) {
            throw new RemoteException("can't get inet address.");
        }

        thisPort = 3230;  // this port(registry’s port)
        System.out.println("this address=" + thisAddress + ",port=" + thisPort);

        // create the registry and bind the name and object.
        registry = LocateRegistry.createRegistry(thisPort);
        registry.rebind("MainServer", this);
    }
}

