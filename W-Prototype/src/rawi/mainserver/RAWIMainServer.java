package rawi.mainserver;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.NetworkUtils;
import rawi.common.Notifier;

public class RAWIMainServer implements Runnable
{
    public void run()
    {
        final ClusterManager clusterManager = new ClusterManager();
        Thread t = new Thread(clusterManager, "Cluster-Manager-Thread");
        t.start();
        System.out.println("Started cluster manager.");

        try
        {
            RMIMainServer s = new RMIMainServer(clusterManager);
        }
        catch (RemoteException ex)
        {
            Logger.getLogger(RAWIMainServer.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        System.out.println("Started RMI server.");

        new Notifier("MainServer").start();
        System.out.println("Started IP notifier.");

        Thread t2 = new Thread()
        {
            @Override
            public void run()
            {
                Collection<String> IPs = NetworkUtils.getIPsFromTracker("ClusterComputer");
                clusterManager.addIpsToScan(IPs);
            }
        };
        t2.start();
    }
}
