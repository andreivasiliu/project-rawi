package rawi.common;

public class SessionInfo {

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
