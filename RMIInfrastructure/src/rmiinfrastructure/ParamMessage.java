package rmiinfrastructure;

import java.io.Serializable;

public class ParamMessage implements Serializable {
    public String paramMessage;

    public ParamMessage(String paramMessage) {
        this.paramMessage = paramMessage;
    }
}
