/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clustercomputer;

import java.io.PrintStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.Ports;
import rawi.common.WebServerInterface;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Andrei
 */
public class LogPrinter extends PrintStream {
	private final String source;

	public LogPrinter(String source) {
		super(System.out);
		this.source = source;
	}

	@Override
	public void println(String x)
	{
		super.println(x);
		super.println();
		try {
			WebServerInterface wsi = new RMIClientModel<WebServerInterface>(Ports.WebServerPort).getInterface();
			wsi.logMessage("Cluster (" + source + ")", "Console Message", x);
		} catch (RemoteException ex) {
			Logger.getLogger(LogPrinter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NotBoundException ex) {
			Logger.getLogger(LogPrinter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
