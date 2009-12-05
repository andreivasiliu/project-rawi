/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrei
 */
public class Notifier extends Thread
{

	boolean shutdown = false;

	@Override
	public void run()
	{
		try
		{
			URL url = new URL("http://testbot73.appspot.com/PutIPServlet");

			while (true)
			{
				try
				{
					if (shutdown)
					{
						break;
					}

					URLConnection conn = url.openConnection();
					conn.addRequestProperty("type", "ClusterComputer");
					conn.addRequestProperty("name", "192.168.0.1");
					conn.setDoInput(false);
					conn.setDoOutput(false);
					conn.connect();

					Thread.sleep(60000);
				}
				catch (InterruptedException ex)
				{
				}
			}
		} catch (IOException ex)
		{
			Logger.getLogger(Notifier.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
