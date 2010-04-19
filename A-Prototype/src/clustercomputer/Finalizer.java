/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import rawi.common.MainServerInterface;
import rawi.common.NetworkUtils;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Andrey
 */
public class Finalizer extends Thread
{

    ClusterComputer clustercomputer;

    public Finalizer(ClusterComputer clustercomputer)
    {
        this.clustercomputer = clustercomputer;
    }

    @Override
    public void run()
    {
        System.out.println("Deleting cache...");
        ClusterTask.deleteDir(new File("cache"));

        clustercomputer.updateIPs();

        for (final String ip : clustercomputer.recentServerRequests.keySet())
        {
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        MainServerInterface msi;
                        System.out.println("Saying good bye to: " + ip + " ...");
                        msi = new RMIClientModel<MainServerInterface>(
                                ip,
                                Ports.MainServerPort).getInterface();
                        msi.goodBye(clustercomputer.uuid);
                    }
                    catch (IOException ex)
                    {
                        // Silently ignore.
                    }
                    catch (NotBoundException ex)
                    {
                        // Silently ignore.
                    }

                }
            }, "GoodByeThread");

            t.start();
        }
    }
}
