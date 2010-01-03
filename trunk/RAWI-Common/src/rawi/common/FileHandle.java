/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

/**
 *
 * @author Andrey
 */
public class FileHandle
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
}
