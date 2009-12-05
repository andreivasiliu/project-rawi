package clustercomputer;

import java.rmi.NotBoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.Task;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import rawi.common.ClusterComputerInterface;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;
import rawi.rmiinfrastructure.RMIServerModel;

/**
 *
 * @author andrei.arusoaie
 */
public class ClusterComputer extends RMIServerModel implements ClusterComputerInterface
{
	public ClusterComputer() throws RemoteException
	{
		super(Ports.ClusterComputerPort);
	}

	/**
	 * This function will be called by a RMI Client.
	 * It executes the command, having the task arguments.
	 * @param task
	 * @param command
	 * @throws IOException
	 */
	public void execute(Task task)
	{
		try
		{
			//createCurrentDir(task);
			//downloadFiles(task);
			Runtime.getRuntime().exec(task.getCommand().getExecString(), null, new File("task" + task.getId()).getAbsoluteFile());
			//uploadFiles(task);
			//deleteCurrentDir(task);
			//Notification

			MainServerInterface msi;
			try
			{
				msi = new RMIClientModel<MainServerInterface>(task.getMainServerAddress(),
						Ports.MainServerPort).getInterface();
				msi.taskCompleted(task.getId());
			} catch (RemoteException ex)
			{
				Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
			} catch (NotBoundException ex)
			{
				Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
			}

		} catch (IOException ex)
		{
			Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}

	public void createCurrentDir(Task task)
	{
		File currentDir = new File("task" + task.getId());
		currentDir.mkdir();
	}

	/**
	 * Downloads a Task component from the repository_uri.
	 * @param task
	 * @param repository_uri
	 * @throws IOException
	 */
	protected void downloadFiles(Task task) throws IOException
	{
		HttpClient httpclient = new DefaultHttpClient();
		File currentDir = new File("task" + task.getId());
		currentDir.mkdir();

		for (int i = 0; i < task.getFiles().length; i++)
		{
			HttpGet httpget = new HttpGet(task.getDownloadURI() + task.getFiles()[i]);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				InputStream instream = entity.getContent();
				OutputStream out = new FileOutputStream(currentDir + "\\" + task.getFiles()[i]);
				int length;
				byte[] tmp = new byte[2048];
				while ((length = instream.read(tmp)) != -1)
				{
					out.write(tmp);
				}
			}
		}

	}

	/**
	 * Uploads modified or created files after the execution,
	 * a given task.
	 * @param task
	 * @throws IOException
	 */
	protected void uploadFiles(Task task) throws IOException
	{
		File f = new File("task" + task.getId());
		boolean found;
		ArrayList<String> filelist = new ArrayList<String>();

		System.out.println("Folder name:" + f.getName());
		if (f.isDirectory())
		{
			String[] files = f.list();

			System.out.println("Folder size:" + files.length);
			for (int i = 0; i < files.length; i++)
			{
				found = false;
				for (int j = 0; j < task.getFiles().length; j++)
				{
					if (files[i].equals(task.getFiles()[j]))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					filelist.add(files[i]);
				}
			}
		}

		org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
		PostMethod post = new PostMethod(task.getUploadURI());

		Part[] part = new Part[filelist.size()];

		for (int i = 0; i < filelist.size(); i++)
		{
			System.out.println("Filename = " + filelist.get(i));
			part[i] = new FilePart(filelist.get(i), new File("task" + task.getId() + "\\" + filelist.get(i)));
		}

		post.setRequestEntity(new MultipartRequestEntity(part, post.getParams()));

		// Execute the upload
		int response = client.executeMethod(post);

		System.out.println("Code " + response);

	}

	/**
	 * Delete the current task folder.
	 * @param task
	 */
	protected void deleteCurrentDir(Task task)
	{
		File f = new File("task" + task.getId());

		String[] files = f.list();

		for (int i = 0; i < files.length; i++)
		{
			new File(f.getName() + "/" + files[i]).delete();
		}

		f.delete();
	}

	

}
