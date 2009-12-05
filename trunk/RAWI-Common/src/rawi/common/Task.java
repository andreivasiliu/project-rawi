/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.common;

/**
 *
 * @author andrei.arusoaie
 */
public class Task {
    int id;
    String[] files;
	Command command;
	String uploadURI, downloadURI, mainServerAddress;

	public Task(int id, String[] files, Command command, String uploadURI,
			String downloadURI, String mainServerAddress)
	{
		this.id = id;
		this.files = files;
		this.command = command;
		this.uploadURI = uploadURI;
		this.downloadURI = downloadURI;
		this.mainServerAddress = mainServerAddress;
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

	public int getId()
	{
		return id;
	}

	public String getUploadURI()
	{
		return uploadURI;
	}

	public String getMainServerAddress()
	{
		return mainServerAddress;
	}
}
