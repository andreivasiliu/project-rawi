
package rawi.common;


import java.io.Serializable;
import java.rmi.RemoteException;

public class ValidateXMLInfo implements Serializable{
    public boolean success;
    public String message;
    public int nodeID;

    public ValidateXMLInfo(boolean success, String message, int nodeID)
            throws RemoteException {
        this.success = success;
        this.message = message;
        this.nodeID = nodeID;
    }
}
