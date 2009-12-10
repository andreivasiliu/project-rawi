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
import rawi.common.Ports;
import rawi.common.Task;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Ioana & Andrei
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Command command = new Command("mycp.exe DecorateMessageInterface.java dec.txt");
            String uploadURI = null;
            String downloadURI = "http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/";
            String webServerAddress = "";
            String mainServerAddress = "";

            RMIMainServer s = new RMIMainServer();
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
