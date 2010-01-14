package rawi.common;

import java.io.Serializable;

public class SessionInfo implements Serializable {

    public String downloadUrl;
    public String uploadUrl;
    public String msgLogIp;
    public String modelXml;
    public String sessionId;

    public SessionInfo(String sessionId, String downloadUrl, String uploadUrl,
            String msgLogIp, String transformationModelXml) {
        this.sessionId = sessionId;
        this.downloadUrl = downloadUrl;
        this.uploadUrl = uploadUrl;
        this.msgLogIp = msgLogIp;
        this.modelXml = transformationModelXml;
    }
}
