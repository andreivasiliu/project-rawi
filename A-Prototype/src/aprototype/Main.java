/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aprototype;

import clustercomputer.ClusterComputer;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;

public class Main
{
    public static void main(String[] args) throws RemoteException, IOException,
            NotBoundException
    {
        //System.setProperty("java.rmi.server.hostname", "5.242.52.108");
        try
        {
            ClusterComputer cc = new ClusterComputer();
        }
        catch (ExportException e)
        {
            System.out.println("Unable to start Cluster Computer: " +
                    e.getLocalizedMessage());

            System.exit(1);
        }
    }
}
