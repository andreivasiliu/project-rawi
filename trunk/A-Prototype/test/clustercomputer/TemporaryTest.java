/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clustercomputer;

import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author Andrey
 */
public class TemporaryTest
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        String[] cmdarray = new String[] { "C:\\Documents and Settings\\Andrei\\Desktop\\Splitter.exe", "2", "100", "100" };
        System.out.println("Exit status: " + Runtime.getRuntime().exec(cmdarray).waitFor());
    }
}
