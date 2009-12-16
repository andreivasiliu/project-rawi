/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.ClusterComputerInterface;
import rawi.common.Command;
import rawi.common.Notifier;
import rawi.common.Ports;
import rawi.common.Task;
import rawi.mainserver.ClusterManager;
import rawi.rmiinfrastructure.RMIClientModel;

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

        if (false)
        {
            testExecuteTask(s);
            return;
        }
    }

    private static void testExecuteTask(RMIMainServer s) {
        try {
            Command command = new Command("mycp.exe DecorateMessageInterface.java dec.txt");
            String uploadURI = null;
            String downloadURI = "http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/";
            String webServerAddress = "";
            String mainServerAddress = "";

            RMIClientModel<ClusterComputerInterface> client = new RMIClientModel<ClusterComputerInterface>("10.1.1.8", Ports.ClusterComputerPort);
            ClusterComputerInterface clustercomputer = client.getInterface();

            Task task = new Task(100, new String[] {"mycp.exe", "DecorateMessageInterface.java"}, command,
                    uploadURI, downloadURI, webServerAddress, mainServerAddress);

            clustercomputer.execute(task);
        } catch (NotBoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
