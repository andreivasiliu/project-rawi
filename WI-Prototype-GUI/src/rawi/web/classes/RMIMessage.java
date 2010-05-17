package rawi.web.classes;

public class RMIMessage {
    private String source, severity, message;
    private int id;
    private static int lastId = 1;
    
    public RMIMessage(String source, String severity, String message) {
        this.source = source;
        this.severity = severity;
        this.message = message;
        this.id = lastId;
        lastId++;
    }

    public RMIMessage() {
        this("DefaultSource", "DefaultSeverity", "DefaultMessage");
        lastId++;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public String getSource() {
        return source;
    }
}
