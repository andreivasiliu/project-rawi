package rmiinfrastructure;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {
    public static void main(String args[]) {
        ServerInterface rmiServer;
        Registry registry;
        String serverAddress = "127.0.0.1";
        String serverPort = "3232";

        try {
            // get the “registry”
            registry = LocateRegistry.getRegistry(
                    serverAddress,
                    (new Integer(serverPort)).intValue());
            // look up the remote object
            rmiServer =
                    (ServerInterface) (registry.lookup("rmiServer"));
            // call the remote method
            ReturnMessage rm = rmiServer.receiveMessage
                    (new ParamMessage("Message sent by the client."));
            System.out.println("Message received by the client: " +
                    rm.returnMessage);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
