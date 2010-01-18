package classes;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import rawi.common.MainServerInterface;
import rawi.common.MainServerStatus;
import rawi.common.NetworkUtils;
import rawi.common.Ports;
import rawi.common.WorkSessionStatus;
import rawi.rmiinfrastructure.RMIClientModel;

public class MainBean {

    HashMap<String, String> xmlList = new HashMap<String, String>();
    HashMap<Long, Session> sessionList = new HashMap<Long, Session>();
    HashMap<String, MainServerStatus> mainServersList =
            new HashMap<String, MainServerStatus>();
    long lastUsedSessionId = 0;

    public static MainBean getFromContext(ServletContext context) {
        return (MainBean) context.getAttribute("mainBean");
    }

    // session stuff
    public long getNextAvailableSessionId() {
        return lastUsedSessionId++;
    }

    public LinkedList<Session> getSessionList() {
        return new LinkedList<Session>(sessionList.values());
    }

    public Session getSessionById(long id) {
        return sessionList.get(id);
    }

    public void addSessionToList(Session session) {
        sessionList.put(session.id, session);
    }

    // XML stuff
    public HashMap<String, String> getXmlList() {
        return xmlList;
    }

    public void deleteXmlByName(String name) {
        System.out.println("===> 2. deleting " + name);
        xmlList.remove(name);
    }

    public List<String> getXmlNamesList() {
        return new LinkedList<String>(xmlList.keySet());
    }

    public void addXmlToList(String name, String content) {
        xmlList.put(name, content);
    }

    public String getXmlContentByName(String name) {
        if (xmlList.containsKey(name)) {
            return xmlList.get(name);
        }
        return "";
    }

    // main servers list
    public List<String> getListOfMainServers() {
        List<String> mainServersIps = new LinkedList<String>();
        List<String> mainServersIds = new LinkedList<String>();

        // get list of main servers from the ip tracker
        Collection<String> IPs = NetworkUtils.getIPsFromTracker("MainServer");

        System.out.println("Checking IPs...");
        for (String ip : IPs)
        {
            try {
                System.out.println("Checking IP: " + ip);
                // request status to each main server
                MainServerInterface msi = new RMIClientModel
                        <MainServerInterface>(ip, Ports.MainServerPort)
                        .getInterface();
                MainServerStatus msStatus = msi.requestStatus();

                // if the main server is already added in the list,
                // with a different ip (but same id), don't add it again
                if(!mainServersIds.contains(msStatus.id)) {
                    mainServersList.put(ip, msStatus);
                    mainServersIps.add(ip);
                    mainServersIds.add(msStatus.id);
                }
            } catch (RemoteException ex) {
                System.err.println("Remote exception - get list of main servers status");
                ex.printStackTrace();
            } catch (NotBoundException ex) {
                System.err.println("Not bound - get list of main servers status");
                ex.printStackTrace();
            }
        }
        System.out.println("Done checking IPs.");

        return mainServersIps;
    }

    public WorkSessionStatus getSessionStatus(long sessionId) {
        Session session = sessionList.get(sessionId);

        try {
            MainServerInterface msi = new RMIClientModel
                    <MainServerInterface>(session.mainServerIp,
                    Ports.MainServerPort).getInterface();
            return msi.getSessionStatus(session.id.toString());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
