/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.rmi.*;

/**
 *
 * @author andrei.arusoaie
 */
public interface ClusterComputerInterface extends Remote
{
    void execute(Task task, Command command);
}
