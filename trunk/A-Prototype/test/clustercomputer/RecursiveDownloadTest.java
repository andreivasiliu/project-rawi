/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
public class RecursiveDownloadTest
{

    @Test
    public void Method() throws RemoteException, NotBoundException
    {
        Command command = new Command("mycp.exe folder1/folder2/folder3/folder4/folder5/testfile.txt gingle.txt");
        String uploadURI = "http://localhost:8084/I-Prototype/TheUploadServlet/0/";
        //String downloadURI = "http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace";
        String downloadURI = "http://localhost:8084/I-Prototype/TheDownloadServlet/0";
        String mainServerAddress = "127.0.0.1";

        RMIClientModel<ClusterComputerInterface> client = new RMIClientModel<ClusterComputerInterface>("127.0.0.1", Ports.ClusterComputerPort);
        ClusterComputerInterface clustercomputer = client.getInterface();

        ArrayList<FileHandle> fhl = new ArrayList<FileHandle>();
        fhl.add(new FileHandle("http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace", "100", "mycp.exe"));
        fhl.add(new FileHandle("http://students.info.uaic.ro/~andrei.arusoaie/rawi_workspace", "101", "folder1/folder2/folder3/folder4/folder5/testfile.txt"));
        Task task1 = new Task("666", fhl, command, uploadURI, downloadURI, mainServerAddress);

        clustercomputer.execute(task1);
    }

}
