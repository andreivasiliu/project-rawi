
package rawi.common;

public class ValidateXMLInfo {
    public boolean success;
    public String message;
    public int nodeID;

    public ValidateXMLInfo(boolean success, String message, int nodeID) {
        this.success = success;
        this.message = message;
        this.nodeID = nodeID;
    }
}
