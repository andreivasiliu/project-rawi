package classes;

import java.io.File;
import java.util.HashMap;

public class Session {
    Long id;
    public String xmlName;
    public String folderName;
    public String mainServerIp;
    HashMap<Long, UploadedFile> fileList = new HashMap<Long, UploadedFile>();
    long lastUsedFileId = 0;

    public Session() {
    }

    public Session(Long id, String xmlName, String folderName, String mainServerIp) {
        this.id = id;
        this.xmlName = xmlName;
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

    public File getFileById(long id) {
        return fileList.get(id).theFile;
        //return fileList.get("name/of/the/file.txt");
    }

    public void deleteFileById(long id) {
        fileList.remove(id);
    }
}
