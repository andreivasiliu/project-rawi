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

        //MainServerInterface msi = new RMIClientModel<MainServerInterface>(
        //        Ports.MainServerPort).getInterface();
        Collection<String> IPs = new LinkedList<String>();
        IPs.add("127.0.0.1");
        //msi.notifyPresence(IPs);

        new Notifier("ClusterComputer").start();
    }
}
