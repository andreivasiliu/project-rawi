/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rawi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author andrei.arusoaie
 */
public class NetworkUtils {

    final static public String IPTRACKER_URL = "http://testbot73.appspot.com/";

    public static List<String> getIPList() throws SocketException {
        List<String> list = new LinkedList<String>();

        Enumeration e = NetworkInterface.getNetworkInterfaces();

        while (e.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) e.nextElement();
            Enumeration<InetAddress> e2 = ni.getInetAddresses();

            while (e2.hasMoreElements()) {
                InetAddress ip = e2.nextElement();
                if (ip instanceof Inet4Address) {
                    list.add(ip.getHostAddress());
                }
            }
        }
        return list;
    }

    public static Collection<String> getIPsFromTracker(String type)
    {
        Set<String> IPs = new HashSet<String>();

        try {
            URL servletURL = new URL
                    (IPTRACKER_URL + "GetIPServlet?type=" + type);

            BufferedReader in = new BufferedReader
                    (new InputStreamReader(servletURL.openStream()));
            String ip;

            while ((ip = in.readLine()) != null) {
                IPs.add(ip);
            }
        } catch (IOException e) {
            throw new RuntimeException("IO Exception on getIPsFromTracker", e);
        }

        return IPs;
    }
}
