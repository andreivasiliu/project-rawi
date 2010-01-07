/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.ClusterComputerInterface;
import rawi.common.Command;
import rawi.common.FileHandle;
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

    public static void testExecuteTask(RMIMainServer s) {
        try {
            Command command = new Command("mycp.exe DecorateMessageInterface.java dec.txt");
            String uploadURI = "http://localhost:8084/I-Prototype/TheUploadServlet/0/";
            String downloadURI = "http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/";
//            String webServerAddress = "192.168.1.3";
            String mainServerAddress = "192.168.1.3";

            RMIClientModel<ClusterComputerInterface> client = new RMIClientModel<ClusterComputerInterface>("127.0.0.1", Ports.ClusterComputerPort);
            ClusterComputerInterface clustercomputer = client.getInterface();

//            Task task = new Task(100, new String[] {"mycp.exe", "DecorateMessageInterface.java"}, command,
//                    uploadURI, downloadURI, webServerAddress, mainServerAddress);

            ArrayList<FileHandle> fhl = new ArrayList<FileHandle>();
            fhl.add(new FileHandle("mycp.exe"));
            fhl.add(new FileHandle("DecorateMessageInterface.java"));
            Task task1 = new Task("666", fhl, command, uploadURI, downloadURI, mainServerAddress);

            clustercomputer.execute(task1);
        } catch (NotBoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
