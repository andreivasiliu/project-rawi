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
    String UUID;
    List<FileHandle> files;
    Command command;
    String uploadURI, downloadURI, mainServerAddress;

    public Task(String UUID, List<FileHandle> files, Command command, String uploadURI, String downloadURI, String mainServerAddress)
    {
        this.UUID = UUID;
        this.files = files;
        this.command = command;
        this.uploadURI = uploadURI;
        this.downloadURI = downloadURI;
        this.mainServerAddress = mainServerAddress;
    }

    public String getUUID()
    {
        return UUID;
    }

    public void setUUID(String UUID)
    {
        this.UUID = UUID;
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
