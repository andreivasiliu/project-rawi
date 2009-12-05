/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import org.junit.Test;
import rawi.common.Command;
import rawi.common.Task;
import static org.junit.Assert.*;

/**
 *
 * @author Andrei
 */
public class DownloadUploadTest
{
	@Test
	public void downloadtest() throws RemoteException, IOException
	{
		Task task = new Task(10, new String[]{"index.html"}, new Command("calc"),"http://www.google.ro/","http://www.google.ro/","");
		ClusterComputer cc = new ClusterComputer();
		cc.downloadFiles(task);
		assertTrue(new File("task10/index.html").exists());
		//cc.uploadFiles(task);

	}

	//@Test
	public void uploadtest() throws RemoteException, IOException
	{
		Task task = new Task(10, new String[]{"myfile.txt"}, new Command("calc"), "http://localhost:8084/I-Prototype/TheUploadServlet", "http://www.google.ro/", "");
		ClusterComputer cc = new ClusterComputer();
		cc.uploadFiles(task);
	}
}
