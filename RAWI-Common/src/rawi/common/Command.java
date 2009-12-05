/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

/**
 *
 * @author andrei.arusoaie
 */
public class Command {
    String command;

    public Command(String command)
    {
        this.command = command;
    }

    public String getExecString()
    {
        return command;
    }
}
