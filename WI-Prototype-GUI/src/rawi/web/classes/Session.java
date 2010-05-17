package rawi.web.classes;

import java.util.HashMap;
import rawi.common.SessionInfo;

public class Session {
    Long id;
    public String xmlName;
    public String xmlContents;
    public String folderName;
    public String mainServerIp;
    public SessionInfo sessionInfo;
    HashMap<Long, UploadedFile> fileList = new HashMap<Long, UploadedFile>();
    long lastUsedFileId = 0;

    public Session() {
    }

    public Session(Long id, String xmlName, String xmlContents,
            String folderName, String mainServerIp) {
        this.id = id;
        this.xmlName = xmlName;
        this.xmlContents = xmlContents;
        this.folderName = folderName;
        this.mainServerIp = mainServerIp;
    }

    public Long getId() {
        return id;
    }

    public long getNextAvailableFileId() {
        return lastUsedFileId++;
    }

    // returns a list of strings like: "Id = 3, FileName = name"
    public HashMap<Long, String> getFileIdsAndNames() {
        HashMap<Long, String> namesList = new HashMap<Long, String>();
        for(long fileId : fileList.keySet())
            namesList.put(fileId, fileList.get(fileId).logicalName);
        return namesList;
    }

    public void addFileToList(UploadedFile file) {
        fileList.put(file.id, file);
    }

    public UploadedFile getFileById(long id) {
        return fileList.get(id);
        //return fileList.get("name/of/the/file.txt");
    }

    public void deleteFileById(long id) {
        fileList.remove(id);
    }
}
