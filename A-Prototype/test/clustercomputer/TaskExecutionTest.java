/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package clustercomputer;

import aprototype.Main;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import rawi.common.ClusterComputerInterface;
import rawi.common.Command;
import rawi.common.FileHandle;
import rawi.common.Ports;
import rawi.common.Task;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Andrey
 */
public class TaskExecutionTest {
    @Test
    public void testExecuteTask() {
        try {
            Command command = new Command("mycp.exe DecorateMessageInterface.java dec.txt");
            String uploadURI = "http://localhost:8084/I-Prototype/TheUploadServlet/0/";
            String downloadURI = "http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/";
            String mainServerAddress = "192.168.1.3";

            RMIClientModel<ClusterComputerInterface> client = new RMIClientModel<ClusterComputerInterface>("127.0.0.1", Ports.ClusterComputerPort);
            ClusterComputerInterface clustercomputer = client.getInterface();

            ArrayList<FileHandle> fhl = new ArrayList<FileHandle>();
            fhl.add(new FileHandle("http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/", null, "mycp.exe"));
            fhl.add(new FileHandle("http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace/", null, "DecorateMessageInterface.java"));
            Task task1 = new Task("666", fhl, command, uploadURI, downloadURI, mainServerAddress);

            clustercomputer.execute(task1);
        } catch (NotBoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
