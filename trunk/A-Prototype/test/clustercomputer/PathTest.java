/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clustercomputer;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

/**
 *
 * @author PIC
 */
public class PathTest
{
    @Test
    public void testPath() throws MalformedURLException
    {
        URL url = new URL("http://www.infoiasi.ro/abc/cd/filename.ext?me=4&you=2");
        System.out.println(url.getHost());
        System.out.println(url.getPath().substring(url.getPath().lastIndexOf("/") + 1) + "-" + sha(url.toString()));
        System.out.println(url.getQuery());
    }

        private String sha(String url)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA");
            return new String(md.digest(url.getBytes()));
        }
        catch (NoSuchAlgorithmException ex)
        {
//            Logger.getLogger(ProgramDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "234";
    }

}
