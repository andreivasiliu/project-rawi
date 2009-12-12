package classes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletContext;

public class MainBean {

    HashMap<String, String> xmlList = new HashMap<String, String>();
    HashMap<Long, Session> sessionList = new HashMap<Long, Session>();
    long lastUsedSessionId = 0;

    public static MainBean getFromContext(ServletContext context) {
        return (MainBean)context.getAttribute("mainBean");
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
}
