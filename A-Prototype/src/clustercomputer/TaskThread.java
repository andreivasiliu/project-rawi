/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.rmi.NotBoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.Task;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import rawi.common.FileHandle;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Andrey
 */
public class TaskThread extends Thread
{

    Task task;
    HashMap<String, Integer> map;

    public TaskThread(Task task)
    {
        map = new HashMap<String, Integer>();
        this.task = task;
    }

    public void createCurrentDir(Task task)
    {
        File currentDir = new File("task" + task.getUUID());
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

        File currentDir = new File("task" + task.getUUID());
        currentDir.mkdir();
        //System.out.println("+++++++++++++++++++++++++++++Download URI: " + task.getDownloadURI());

        for (int i = 0; i < task.getFiles().length; i++) {
            //System.out.println("+++++++++++++++++++++++++++++Downloading: " + task.getDownloadURI() + task.getFiles()[i]);
            HttpGet httpget = new HttpGet(task.getDownloadURI() + task.getFiles()[i]);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                OutputStream out = new FileOutputStream(currentDir + "\\" + task.getFiles()[i]);
                int length;
                byte[] tmp = new byte[2048];
                while ((length = instream.read(tmp)) != -1) {
                    out.write(tmp, 0, length);
                }
                out.close();
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
        File f = new File("task" + task.getUUID());

        ArrayList<FileHandle> filelist = new ArrayList<FileHandle>(getChangedOrNewFiles());

        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        PostMethod post = new PostMethod(task.getUploadURI());

        Part[] part = new Part[filelist.size()];

        for (int i = 0; i < filelist.size(); i++) {
            System.out.println("Filename = " + filelist.get(i));
            part[i] = new FilePart(filelist.get(i).getFileURL(), new File("task" + task.getUUID() + "\\" + filelist.get(i).getFileURL()));
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
        File f = new File("task" + task.getUUID());

        String[] files = f.list();

        for (int i = 0; i < files.length; i++) {
            new File(f.getName() + "/" + files[i]).delete();
        }

        f.delete();
    }

    //map(filename, file_hashcode)
    //
    public void mapFiles()
    {
        File f = new File("task" + task.getUUID());
        
        String[] files = f.list();

        for (int i = 0; i < files.length; i++) {
            File file = new File(f.getName() + "/" + files[i]);
            try {
                map.put(files[i], getBytesFromFile(file).hashCode());
            } catch (IOException ex) {
                Logger.getLogger(TaskThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // return all new files created
    // needs to be changed in future....
    public List<FileHandle> getChangedOrNewFiles()
    {
        List<FileHandle> list = new LinkedList<FileHandle>();
        
        File f = new File("task" + task.getUUID());
        
        String[] files = f.list();
        
        for (int i = 0; i < files.length; i++)
        {
            try {
                int new_file_code = getBytesFromFile(new File(f.getName() + "/" + files[i])).hashCode();
                int old_file_code = map.get(files[i]);
                if (old_file_code != new_file_code)
                    list.add(new FileHandle(task.getUploadURI(), null, files[i]));
            } catch (IOException ex) {
                Logger.getLogger(TaskThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return list;
    }

    // return a byte array coresponding to a given file
    private byte[] getBytesFromFile(File file) throws IOException
    {

        InputStream is = new FileInputStream(file);
        System.out.println("\nDEBUG: FileInputStream is " + file);

        // Get the size of the file
        long length = file.length();
        System.out.println("DEBUG: Length of " + file + " is " + length + "\n");

        /*
         * You cannot create an array using a long type. It needs to be an int
         * type. Before converting to an int type, check to ensure that file is
         * not loarger than Integer.MAX_VALUE;
         */
        if (length > Integer.MAX_VALUE) {
            System.out.println("File is too large to process");
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while ((offset < bytes.length)
                && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {

            offset += numRead;

        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;

    }

    @Override
    public void run()
    {
        try {
            // create current working folder
            createCurrentDir(task);
            
            //download files
            downloadFiles(task);

            //mapping file names and their hashcode
            mapFiles();

            String execString = task.getCommand().getExecString();
            //System.out.println("--------------------ExecString:" + execString);
            String[] envp = new String[]{"PATH=" + (new File("task" + task.getUUID()).getAbsolutePath())};
            //System.out.println("--------------------envp:" + envp[0]);
            File file = new File("task" + task.getUUID()).getAbsoluteFile();
            //System.out.println("--------------------File:" + file.getAbsolutePath());
            Runtime.getRuntime().exec(execString, null, file);
            
            //Uploading files
            uploadFiles(task);

            //delete current folder
            deleteCurrentDir(task);

            //Notification
            MainServerInterface msi;
            try {
                msi = new RMIClientModel<MainServerInterface>(task.getMainServerAddress(),
                        Ports.MainServerPort).getInterface();
                msi.taskCompleted(task.getUUID(), getChangedOrNewFiles());
            } catch (RemoteException ex) {
                Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NotBoundException ex) {
                Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (IOException ex) {
            Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
