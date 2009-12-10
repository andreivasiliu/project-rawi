
package project;

import java.rmi.RemoteException;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.common.ValidateXMLInfo;
import rawi.rmiinfrastructure.RMIServerModel;

public class RMIMainServer extends RMIServerModel
        implements MainServerInterface {
    
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

	public void taskCompleted(int id) throws RemoteException
	{
		//throw new UnsupportedOperationException("Not supported yet.");
	}
}

