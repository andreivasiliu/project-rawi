
package rmiinfrastructure;

import java.io.Serializable;

public class ReturnMessage implements Serializable {
    public String returnMessage;

    public ReturnMessage(String returnMessage) {
        this.returnMessage = returnMessage;
    }
}
