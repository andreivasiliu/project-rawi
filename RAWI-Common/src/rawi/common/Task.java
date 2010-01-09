/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author andrei.arusoaie
 */
public class Task implements Serializable{
    String id;
    List<FileHandle> files;
    Command command;
    String uploadURI, downloadURI, mainServerAddress;

    public Task(String id, List<FileHandle> files, Command command, String uploadURI, String downloadURI, String mainServerAddress)
    {
        this.id = id;
        this.files = files;
        this.command = command;
        this.uploadURI = uploadURI;
        this.downloadURI = downloadURI;
        this.mainServerAddress = mainServerAddress;
    }

    public Task(String id, List<FileHandle> files, Command command)
    {
        this.id = id;
        this.files = files;
        this.command = command;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Command getCommand()
    {
        return command;
    }

    public void setCommand(Command command)
    {
        this.command = command;
    }

    public String getDownloadURI()
    {
        return downloadURI;
    }

    public void setDownloadURI(String downloadURI)
    {
        this.downloadURI = downloadURI;
    }

    public List<FileHandle> getFiles()
    {
        return files;
    }

    public void setFiles(List<FileHandle> files)
    {
        this.files = files;
    }

    public String getMainServerAddress()
    {
        return mainServerAddress;
    }

    public void setMainServerAddress(String mainServerAddress)
    {
        this.mainServerAddress = mainServerAddress;
    }

    public String getUploadURI()
    {
        return uploadURI;
    }

    public void setUploadURI(String uploadURI)
    {
        this.uploadURI = uploadURI;
    }
}
