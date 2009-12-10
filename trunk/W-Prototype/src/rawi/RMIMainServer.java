package rawi;

import java.rmi.RemoteException;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.common.ValidateXMLInfo;
import rawi.rmiinfrastructure.RMIServerModel;

public class RMIMainServer extends RMIServerModel
        implements MainServerInterface {

    String  webServerDownloadURL,
            webServerUploadURL,
            messageLogIp;
    long sessionId;


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

    public void startSession(String download, String upload, String msgLog,
            long sessionId) throws RemoteException {
        webServerDownloadURL = download;
        webServerUploadURL = upload;
        messageLogIp = msgLog;
        this.sessionId = sessionId;

        System.out.println("Started session: " + sessionId
                + "\n\t Download URL = " + webServerDownloadURL
                + "\n\t Upload URL = " + webServerUploadURL
                + "\n\t Message log ip = " + messageLogIp);
    }

    public void taskCompleted(int id) throws RemoteException {
            throw new UnsupportedOperationException("Not supported yet.");
    }
}

