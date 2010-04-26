/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

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
public class MainServerNotification extends Thread
{

    String ip;

    public MainServerNotification(String server_ip)
    {
        ip = server_ip;
    }

    @Override
    public void run()
    {
        try
        {
            MainServerInterface msi;
            System.out.println("Notifying: " + ip + " ...");
            msi = new RMIClientModel<MainServerInterface>(
                    ip,
                    Ports.MainServerPort).getInterface();
            msi.notifyPresence(NetworkUtils.getIPList());
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
}
