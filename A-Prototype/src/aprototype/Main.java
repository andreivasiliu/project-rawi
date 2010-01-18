/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aprototype;

import clustercomputer.ClusterComputer;
import clustercomputer.MainServerNotification;
import rawi.common.Notifier;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import rawi.common.NetworkUtils;

public class Main extends Thread
{
    public static void main(String[] args) throws RemoteException, IOException,
            NotBoundException
    {
        System.setProperty("java.rmi.server.hostname", "5.242.52.108");
        ClusterComputer cc = new ClusterComputer();

        Collection<String> mainServerIpList = NetworkUtils.getIPsFromTracker("MainServer");

        for (String serverIP : mainServerIpList)
        {
            MainServerNotification msn = new MainServerNotification(serverIP);
            msn.start();
        }

        new Notifier("ClusterComputer").start();
    }

    @Override
    public void run()
    {

    }


}
