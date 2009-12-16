package classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;
import rawi.common.MainServerInterface;
import rawi.common.MainServerStatus;
import rawi.common.Ports;
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

        try {
            // get list of main servers from the ip tracker
            URL servletURL = new URL
                    ("http://testbot73.appspot.com/GetIPServlet?type=MainServer");

            BufferedReader in = new BufferedReader
                    (new InputStreamReader(servletURL.openStream()));
            String line; // main server's id
            while ((line = in.readLine()) != null) {

                // request status to each main server
                MainServerInterface msi = new RMIClientModel
                        <MainServerInterface>(line, Ports.MainServerPort)
                        .getInterface();
                MainServerStatus msStatus = msi.requestStatus();

                // if the main server is already added in the list,
                // with a different ip (but same id), don't add it again
                if(!mainServersIds.contains(msStatus.id)) {
                    mainServersList.put(line, msStatus);
                    mainServersIps.add(line);
                    mainServersIds.add(msStatus.id);
                }
            }

        } catch (RemoteException ex) {
            System.err.println("Remote exception - get list of main servers status");
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            System.err.println("Not bound - get list of main servers status");
            ex.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.println("Eroare la citirea url-ului");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Eroare in-out la citirea de pe site.");
            e.printStackTrace();
        }

        return mainServersIps;
    }
}
