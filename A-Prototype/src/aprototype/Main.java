/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package aprototype;

import clustercomputer.ClusterComputer;
import rawi.common.Notifier;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import rawi.common.MainServerInterface;
import rawi.common.NetworkUtils;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author andrei.arusoaie
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException, IOException,
            NotBoundException {
        ClusterComputer cc = new ClusterComputer();

        List<String> mainServerIpList = NetworkUtils.getIPsFromTracker("MainServer");
        MainServerInterface msi = new RMIClientModel<MainServerInterface>(
                "10.1.1.12",
                Ports.MainServerPort).getInterface();
        msi.notifyPresence(NetworkUtils.getIPList());

        new Notifier("ClusterComputer").start();
    }
}
