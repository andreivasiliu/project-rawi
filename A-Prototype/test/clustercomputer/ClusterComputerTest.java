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
public class ClusterComputerTest {

	
	public void testExecute() throws Exception
	{
		Task task = new Task(10, new String[]{}, new Command("calc"), "", "", "", "");

		ClusterComputer cc = new ClusterComputer();
		cc.deleteCurrentDir(task);
		assertFalse(new File("task10").exists());

		cc.downloadFiles(task);
		assertTrue(new File("task10").exists());

		cc.deleteCurrentDir(task);
		assertFalse(new File("task10").exists());
	}

	@Test
	public void testDelete() throws RemoteException
	{
		Task task = new Task(10, new String[]{}, new Command("calc"), "", "", "", "");
		ClusterComputer cc = new ClusterComputer();

		cc.deleteCurrentDir(task);
	}

	@Test
	public void executeTest() throws RemoteException, IOException
	{
		Task task = new Task(10, new String[]{}, new Command("calc"), "", "", "", "");
		ClusterComputer cc = new ClusterComputer();
		cc.execute(task);
	}

}