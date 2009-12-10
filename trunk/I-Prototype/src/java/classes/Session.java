package classes;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Session {
    Long id;
    public String xmlName;
    public String folderName;
    HashMap<String, File> fileList = new HashMap<String, File>();
    
    public Session() {
    }

    public Session(Long id, String xmlName, String folderName) {
        this.id = id;
        this.xmlName = xmlName;
        this.folderName = folderName;
    }

    public Long getId() {
        return id;
    }

    public List<String> getFileNames() {
        return new LinkedList<String>(fileList.keySet());
    }

    public void addFileToList(String fileName, File file) {
        fileList.put(fileName, file);
    }

    public File getFileByName(String name) {
        //return fileList.get(name);
        return fileList.get("name/of/the/file.txt");
    }
}
