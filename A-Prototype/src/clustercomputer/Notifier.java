/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrei
 */
/**
 * Notifier
 * Notifies it's presence as an active cluster computer to IPTracker.
 * @author andrei.arusoaie
 */
public class Notifier extends Thread {

    boolean shutdown = false;

    @Override
    public void run() {
        try {
            //get IPTracker's URL
            //URL url = new URL("http://testbot73.appspot.com/PutIPServlet");
            URL url = new URL("http://testbot73.appspot.com/PutIPServlet");
            while (true) {
                try {
                    if (shutdown) {
                        //stop thread
                        break;
                    }

                    //connect and send parameters
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    PrintStream out = new PrintStream(conn.getOutputStream());
                    //set request parameters
                    out.print("name=" + getIPList() + "&type=ClusterComputer");
                    out.close();
                    conn.connect();
                    conn.getInputStream().close();

                    //wait
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Notifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getIPList() throws SocketException {
        String list = "";

        Enumeration e = NetworkInterface.getNetworkInterfaces();

        while (e.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) e.nextElement();
            Enumeration e2 = ni.getInetAddresses();

            while (e2.hasMoreElements()) {
                InetAddress ip = (InetAddress) e2.nextElement();
                if (list.length() != 0)
                    list += ",";
                list += ip.getHostAddress();
            }
        }
        return list;
    }
}
