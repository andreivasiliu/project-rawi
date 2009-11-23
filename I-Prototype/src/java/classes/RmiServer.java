package classes;


import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class RmiServer extends java.rmi.server.UnicastRemoteObject
        implements LogMessageInterface {

    int thisPort;
    String thisAddress;
    Registry registry;    // rmi registry for lookup the remote objects.

    List<RMIMessage> messageList = new ArrayList<RMIMessage>();

    // This method is called from the remote client by the RMI.
    // This is the implementation of the “LogMessageInterface”.
    public void logMessage(String source, String severity, String message)
            throws RemoteException {
        
        RMIMessage m = new RMIMessage(source, severity, message);
        messageList.add(m);
        System.out.println("Received message: " +
                "\n\tid: " + m.getId() +
                "\n\tsource: " + source +
                "\n\tseverity: " + severity +
                "\n\tmessage: " + message);
    }

    public List<RMIMessage> getMessageList() {
        return messageList;
    }

    public List<RMIMessage> getMessageListFromID(int fromId) {
        List<RMIMessage> list = new ArrayList<RMIMessage>();
        for(RMIMessage m: messageList) {
            if(m.getId() > fromId)
                list.add(m);
        }
        return list;
    }


    public RmiServer() throws RemoteException {

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

//    static public void main(String args[]) {
//        try {
//            RmiServer s = new RmiServer();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }
}
