/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author PIC
 */
public class Jetty extends Thread
{
    private Server server;
    private URL war;

    public Jetty(Server server, URL war)
    {
        this.server = server;
        this.war = war;
    }


    @Override
    public void run()
    {
        try
        {
            server.start();
            server.join();
        }
        catch (Exception ex)
        {
            Logger.getLogger(Jetty.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
