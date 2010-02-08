/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.File;

/**
 *
 * @author Andrey
 */
public class Finalizer extends Thread
{

    @Override
    public void run()
    {
        System.out.println("Deleting cache...");
        ClusterTask.deleteDir(new File("cache"));
    }

}
