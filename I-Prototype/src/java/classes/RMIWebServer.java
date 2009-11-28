package classes;

import rawi.common.RMIMessage;
import rawi.common.WebServerInterface;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import rawi.rmiinfrastructure.RMIServerModel;

public class RMIWebServer extends RMIServerModel
        implements WebServerInterface {

    List<RMIMessage> messageList = new ArrayList<RMIMessage>();

    // RMI Metod
    public void logMessage(String source, String severity, String message)
            throws RemoteException {

        RMIMessage m = new RMIMessage(source, severity, message);
        messageList.add(m);
        System.out.println("Received message: " +
                "\n\tid: " + m.getId() +
                "\n\tsource: " + source +
                "\n\tseverity: " + severity +
                "\n\tmessage: " + message);
    }

    public List<RMIMessage> getMessageList() {
        return messageList;
    }

    public List<RMIMessage> getMessageListFromID(int fromId) {
        List<RMIMessage> list = new ArrayList<RMIMessage>();
        for (RMIMessage m : messageList) {
            if (m.getId() > fromId) {
                list.add(m);
            }
        }
        return list;
    }

    public RMIWebServer() throws RemoteException {
        super(3232);
    }
}
