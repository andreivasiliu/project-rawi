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
    public void test() throws IOException
    {
        String[] cmdarray = new String[] { "notepad", "abc.txt" };
        Runtime.getRuntime().exec(cmdarray);
    }
}
