/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author Andrey
 */
public class FileHandle implements Serializable
{
    String uniqueId = UUID.randomUUID().toString();
    String baseURL;
    String id;
    String logicalName;
    boolean zipFile = false;

    public FileHandle(String logicalName)
    {
        this.logicalName = logicalName;
    }

    public FileHandle(String baseURL, String id, String logicalName)
    {
        this.baseURL = baseURL;
        this.id = id;
        this.logicalName = logicalName;
    }

    public String getFileURL()
    {
        if (id == null)
            return baseURL + "/" + logicalName;
        else
            return baseURL + "/" + id + "/" + logicalName;
    }

    public String getId()
    {
        return id;
    }

    public String getUniqueId()
    {
        return uniqueId;
    }

    public String getLogicalName()
    {
        return logicalName;
    }

    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setLogicalName(String logicalName)
    {
        this.logicalName = logicalName;
    }

    public boolean isZipFile()
    {
        return zipFile;
    }

    public void setZipFile(boolean zipFile)
    {
        this.zipFile = zipFile;
    }
}
