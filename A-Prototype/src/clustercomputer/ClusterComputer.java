package clustercomputer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
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

/**
 *
 * @author andrei.arusoaie
 */
public class ClusterComputer
{

	String thisAddress;
	int PORT = 8000;
	String NAME = "CLUSTER";
	String URI = "http://localhost:8081/web/TheUploadServlet";
	Registry registry;

	public ClusterComputer() throws RemoteException
	{
		/*        try {

		// get the address of this host.

		thisAddress = (InetAddress.getLocalHost()).toString();

		} catch (Exception e) {

		throw new RemoteException("can't get inet address.");

		}

		try {
		registry = LocateRegistry.createRegistry(PORT);
		registry.rebind(NAME, this);

		} catch (RemoteException e) {

		throw e;

		}*/
	}

	/**
	 * This function will be called by a RMI Client.
	 * It executes the command, having the task arguments.
	 * @param task
	 * @param command
	 * @throws IOException
	 */
	public void execute(Task task, Command command) throws IOException
	{
		downloadFiles(task, URI);
		Runtime.getRuntime().exec(command.getExecString());
		uploadFiles(task);
		deleteCurrentDir(task);
	}


	/**
	 * Downloads a Task component from the repository_uri.
	 * @param task
	 * @param repository_uri
	 * @throws IOException
	 */
	private void downloadFiles(Task task, String repository_uri) throws IOException
	{
		HttpClient httpclient = new DefaultHttpClient();
		File currentDir = new File("task" + task.id);
		currentDir.mkdir();

		for (int i = 0; i < task.files.length; i++)
		{
			HttpGet httpget = new HttpGet(repository_uri + "/" + task.files[i]);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				InputStream instream = entity.getContent();
				OutputStream out = new FileOutputStream(currentDir + "\\" + task.files[i]);
				int l;
				byte[] tmp = new byte[2048];
				while ((l = instream.read(tmp)) != -1)
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
	private void uploadFiles(Task task) throws IOException
	{

		File f = new File("task" + task.id);
		boolean found;
		ArrayList<String> filelist = new ArrayList<String>();

		if (f.isDirectory())
		{
			String[] files = f.list();

			for (int i = 0; i < files.length; i++)
			{
				found = false;
				for (int j = 0; j < task.files.length; j++)
				{
					if (files[i].equals(task.files[j]))
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
		PostMethod post = new PostMethod(URI);

		Part[] part = new Part[filelist.size()];

		for (int i = 0; i < filelist.size(); i++)
		{
			System.out.println("Filename = " + filelist.get(i));
			part[i] = new FilePart(filelist.get(i), new File("task10\\" + filelist.get(i)));
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
	private void deleteCurrentDir(Task task)
	{
		File f = new File("task" + task.id);
		f.delete();
	}
}
