/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rawi.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author andrei.arusoaie
 */
public class NetworkUtils {

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

    //TODO  Get list of IP's of MainServer's
    public static List<String> getIPsFromTracker(String type)
    {
        return null;
    }
}
