package rawi;

import java.rmi.RemoteException;
import java.util.Collection;
import rawi.common.NetworkUtils;
import rawi.common.Notifier;
import rawi.mainserver.ClusterManager;

/**
 *
 * @author Ioana & Andrei & Whyte
 */
public class Main
{
    public static void main(String[] args) throws RemoteException
    {
        ClusterManager clusterManager = new ClusterManager();
        Thread t = new Thread(clusterManager, "Cluster-Manager-Thread");
        t.start();
        System.out.println("Started cluster manager.");

        new Notifier("MainServer").start();
        System.out.println("Started IP notifier.");

        RMIMainServer s = new RMIMainServer(clusterManager);
        System.out.println("Started RMI server.");

        System.out.println("Beginning...");
        Collection<String> IPs = NetworkUtils.getIPsFromTracker("ClusterComputer");
        clusterManager.addIpsToScan(IPs);
        System.out.println("Stopped.");
    }
}
