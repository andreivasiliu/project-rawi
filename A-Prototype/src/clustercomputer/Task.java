/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clustercomputer;

/**
 *
 * @author andrei.arusoaie
 */
public class Task {
    int id;
    String[] files;
    String repository_uri;

    public Task(int id, String... args)
    {
        this.id = id;
        files = args;
    }
}
