package classes;

import java.util.HashMap;
import java.util.LinkedList;
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

    public void addXMLToList(String name, String content) {
        xmlList.put(name, content);
    }

    public String getXMLContentByName(String name) {
        if (xmlList.containsKey(name)) {
            return xmlList.get(name);
        }
        return "";
    }
}
