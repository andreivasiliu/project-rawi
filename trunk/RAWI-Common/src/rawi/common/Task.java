/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

import java.io.Serializable;

/**
 *
 * @author andrei.arusoaie
 */
public class Task implements Serializable{
    String UUID;
    String[] files;
    Command command;
    String uploadURI, downloadURI, mainServerAddress;

    public Task(String UUID, String[] files, Command command, String uploadURI, String downloadURI, String mainServerAddress)
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

    public Command getCommand()
    {
        return command;
    }

    public String getDownloadURI()
    {
        return downloadURI;
    }

    public String[] getFiles()
    {
        return files;
    }

    public String getMainServerAddress()
    {
        return mainServerAddress;
    }

    public String getUploadURI()
    {
        return uploadURI;
    }
}
