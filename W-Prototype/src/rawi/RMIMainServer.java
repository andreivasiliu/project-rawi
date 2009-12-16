package rawi;

import java.rmi.RemoteException;
import java.util.UUID;
import rawi.common.MainServerInterface;
import rawi.common.MainServerStatus;
import rawi.common.Ports;
import rawi.common.SessionInfo;
import rawi.common.ValidateXMLInfo;
import rawi.rmiinfrastructure.RMIServerModel;

public class RMIMainServer extends RMIServerModel
        implements MainServerInterface {

    SessionInfo sessionInfo;
    private String id = UUID.randomUUID().toString();


    public RMIMainServer() throws RemoteException {
        super(Ports.MainServerPort);
    }

    public ValidateXMLInfo validateXML(String xml) throws RemoteException {
        System.out.println("Received xml: " +  xml.toString());
        if (xml.contains("hello"))
            return new ValidateXMLInfo(true, "Validation succeeded", 0);
        else
            return new ValidateXMLInfo(false, "You must say hello", 1);
    }

    public void createSession(SessionInfo sessionInfo) throws RemoteException {
        this.sessionInfo = sessionInfo;

        System.out.println("Created session: " + sessionInfo.sessionId
                + "\n\t Download URL = " + sessionInfo.downloadUrl
                + "\n\t Upload URL = " + sessionInfo.uploadUrl
                + "\n\t Message log ip = " + sessionInfo.msgLogIp);
    }

    public void taskCompleted(int id) throws RemoteException {
            throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public MainServerStatus requestStatus() throws RemoteException {
        MainServerStatus status = new MainServerStatus();
        System.out.println("Received status request.");
        status.id = id;

        return status;
    }
}

