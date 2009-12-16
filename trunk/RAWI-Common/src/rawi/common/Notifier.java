/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rawi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Notifier
 * Notifies it's presence as an active cluster computer to IPTracker.
 * @author andrei.arusoaie
 */
public class Notifier extends Thread {

    boolean shutdown = false;
    String type;

    public Notifier(String type)
    {
        super("Notifier-Thread");
        this.type = type;
    }

    @Override
    public void run() {
        try {
            //get IPTracker's URL
            URL url = new URL("http://testbot73.appspot.com/PutIPServlet");
            //URL url = new URL("http://localhost:3333/PutIPServlet");
            while (true) {
                try {
                    if (shutdown) {
                        //stop thread
                        break;
                    }

                    //connect and send parameters
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.connect();

                    //set request parameters
                    PrintStream out = new PrintStream(conn.getOutputStream());
                    out.print("name=" + getIPList() + "&type=" + type);
                    out.close();

                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    while (reader.readLine() != null)
                        continue;
                    reader.close();

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
                if (ip instanceof Inet4Address) {
                    if (list.length() != 0)
                        list += ",";
                    list += ip.getHostAddress();
                }
            }
        }
        return list;
    }
}
