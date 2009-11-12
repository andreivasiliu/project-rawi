/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package aprototype;

import clustercomputer.ClusterComputer;
import clustercomputer.Command;
import clustercomputer.Task;
import java.rmi.RemoteException;

/**
 *
 * @author andrei.arusoaie
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RemoteException {
        // TODO code application logic here

        ClusterComputer comp = new ClusterComputer();

        comp.execute(new Task(12,"Grafic.java"), new Command("calc"));

    }

}
