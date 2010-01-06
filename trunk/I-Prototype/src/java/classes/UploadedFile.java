package classes;

import java.io.File;

public class UploadedFile {
    public long id;
    public String logicalName;
    public File theFile;

    public UploadedFile(long id, String logicalName, File theFile) {
        this.id = id;
        this.logicalName = logicalName;
        this.theFile = theFile;
    }
}
