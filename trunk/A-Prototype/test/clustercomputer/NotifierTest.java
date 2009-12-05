/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Andrei
 */
public class NotifierTest
{
	@Test
	public void notifierTest() throws InterruptedException
	{
		Notifier n = new Notifier();
		n.start();
		Thread.sleep(50);
		assertTrue(n.isAlive());
		n.shutdown = true;
		n.interrupt();
		n.join();
		assertFalse(n.isAlive());
	}
}
