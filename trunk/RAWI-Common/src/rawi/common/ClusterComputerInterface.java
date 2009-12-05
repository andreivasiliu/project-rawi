/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rawi.common;

import java.rmi.*;

/**
 *
 * @author andrei.arusoaie
 */
public interface ClusterComputerInterface extends Remote
{
    void execute(Task task);
}
