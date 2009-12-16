package rawi.common;

import java.io.Serializable;

public class SessionInfo implements Serializable {

    public String downloadUrl,
                  uploadUrl,
                  msgLogIp;
    public long sessionId;

    public SessionInfo(String downloadUrl, String uploadUrl, String msgLogIp, long sessionId) {
        this.downloadUrl = downloadUrl;
        this.uploadUrl = uploadUrl;
        this.msgLogIp = msgLogIp;
        this.sessionId = sessionId;
    }
}
