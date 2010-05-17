package rawi.web.classes;

import java.io.File;

public class UploadedFile {
    public long id;
    public String logicalName;
    public File theFile;
    public boolean zipFile;

    public UploadedFile(long id, String logicalName, File theFile, boolean zipFile) {
        this.id = id;
        this.logicalName = logicalName;
        this.theFile = theFile;
        this.zipFile = zipFile;
    }
}
