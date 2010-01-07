/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

import java.io.Serializable;

/**
 *
 * @author Andrey
 */
public class FileHandle implements Serializable
{
    String baseURL;
    String id;
    String logicalName;

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
        //return baseURL + "/" + id + "/" + logicalName;
        return logicalName;
    }

    public String getId()
    {
        return id;
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
}
