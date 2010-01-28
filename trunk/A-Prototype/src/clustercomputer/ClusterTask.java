package clustercomputer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import rawi.common.Task;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
public class ClusterTask
{

    ClusterComputer clusterComputer;
    Task task;
    HashMap<String, byte[]> map;

    public ClusterTask(ClusterComputer clusterComputer, Task task)
    {
        map = new HashMap<String, byte[]>();
        this.clusterComputer = clusterComputer;
        this.task = task;
    }

    public void createCurrentDir()
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
    protected void downloadFiles() throws IOException
    {
        HttpClient httpclient = new DefaultHttpClient();

        String currentDir = new File("task" + task.getId()).toString();

        List<FileHandle> files = task.getFiles();
        for (FileHandle f : files)
        {
            HttpGet httpget = new HttpGet(f.getFileURL());
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                InputStream instream = entity.getContent();
                OutputStream out = new FileOutputStream(currentDir + "/" + f.getLogicalName());
                int length;
                byte[] tmp = new byte[2048];
                while ((length = instream.read(tmp)) != -1)
                {
                    out.write(tmp, 0, length);
                }
                out.close();
                if (f.isZipFile())
                    unzipFile(currentDir + "/" + f.getLogicalName(), currentDir);
            }
        }
    }

    private static void unzipFile(String fileName, String currentDir)
            throws IOException
    {
        ZipFile zipFile = new ZipFile(fileName);

        System.out.println("Unzipping archive...");
        
        for (ZipEntry zipEntry : Collections.list(zipFile.entries()))
        {
            if (zipEntry.isDirectory())
                new File(zipEntry.getName()).mkdir();
            else
            {
                String outFileName = currentDir + "/" + zipEntry.getName();
                copyInputStream(zipFile.getInputStream(zipEntry),
                        new BufferedOutputStream(new FileOutputStream(outFileName)));
                System.out.println("Extracted " + zipEntry.getName());
            }
        }

        zipFile.close();
        new File(fileName).delete();
    }

    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;

        while((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        in.close();
        out.close();
    }

    /**
     * Uploads modified or created files after the execution,
     * a given task.
     * @param task
     * @throws IOException
     */
    protected List<FileHandle> uploadFiles() throws IOException, NoSuchAlgorithmException
    {
        ArrayList<FileHandle> filelist = new ArrayList<FileHandle>(getChangedOrNewFiles());

        for (FileHandle fileHandle : filelist)
        {
            System.out.println("Uploading " + fileHandle.getLogicalName() +
                    " to: " + task.getUploadURI() + "/" + fileHandle.getLogicalName());

            int id = uploadOnlyOneFile(new FilePart(fileHandle.getLogicalName(),
                    new File("task" + task.getId() + "/" + fileHandle.getLogicalName())),
                    fileHandle.getLogicalName());
            if (id != -1)
            {
                fileHandle.setId(Integer.toString(id));
            }
        }

        return filelist;
    }

    /* done because UploadServlet accepts only one part...*/
    private int uploadOnlyOneFile(FilePart filepart, String name) throws IOException
    {
        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        PostMethod post = new PostMethod(task.getUploadURI() + "/" + name);
        Part[] part = new Part[]
        {
            filepart
        };
        post.setRequestEntity(new MultipartRequestEntity(part, post.getParams()));
        int response = client.executeMethod(post);
        System.out.println("Upload status = " + response);
        //System.out.println("Post response:" + post.getResponseBodyAsString());
        String resp = post.getResponseBodyAsString();

        Matcher m = Pattern.compile("File ID: ([0-9]+)").matcher(resp);

        if (m.find())
        {
            return Integer.parseInt(m.group(1));
        }
        else
        {
            System.out.println("Could not get a File ID!");
            System.out.println("The response was: " + resp);
            return -1;
        }
    }

    /**
     * Delete the current task folder.
     * @param task
     */
    protected void deleteCurrentDir()
    {
        File f = new File("task" + task.getId());

        String[] files = f.list();

        for (int i = 0; i < files.length; i++)
        {
            new File(f.getName() + "/" + files[i]).delete();
        }

        f.delete();
    }

    //map(filename, file_hashcode)
    public void mapFiles() throws NoSuchAlgorithmException
    {
        File f = new File("task" + task.getId());

        String[] files = f.list();
        MessageDigest m = MessageDigest.getInstance("MD5");

        for (int i = 0; i < files.length; i++)
        {
            File file = new File(f.getName() + "/" + files[i]);
            try
            {
                map.put(files[i], m.digest(getBytesFromFile(file)));
            } catch (IOException ex)
            {
                Logger.getLogger(ClusterTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // return all new files created
    // needs to be changed in future....
    public List<FileHandle> getChangedOrNewFiles() throws NoSuchAlgorithmException
    {
        List<FileHandle> list = new LinkedList<FileHandle>();

        File f = new File("task" + task.getId());

        String[] files = f.list();

        for (int i = 0; i < files.length; i++)
        {
            try
            {
                MessageDigest m = MessageDigest.getInstance("MD5");
                byte[] new_file_code = m.digest(getBytesFromFile(new File(f.getName() + "/" + files[i])));
                byte[] old_file_code = map.get(files[i]);
                if (!equals(new_file_code, old_file_code))
                {
                    list.add(new FileHandle(task.getDownloadURI(), null, files[i]));
                }
            } catch (IOException ex)
            {
                Logger.getLogger(ClusterTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return list;
    }

    // compares two byte arrays contents
    private boolean equals(byte[] a, byte[] b)
    {
        if (null == a || null == b)
        {
            return false;
        }

        if (a.length != b.length)
        {
            return false;
        }

        for (int i = 0; i < a.length; i++)
        {
            if (a[i] != b[i])
            {
                return false;
            }
        }
        return true;
    }

    // return a byte array coresponding to a given file
    private byte[] getBytesFromFile(File file) throws IOException
    {

        InputStream is = new FileInputStream(file);
        //System.out.println("\nDEBUG: FileInputStream is " + file);

        // Get the size of the file
        long length = file.length();
        //System.out.println("DEBUG: Length of " + file + " is " + length + "\n");

        /*
         * You cannot create an array using a long type. It needs to be an int
         * type. Before converting to an int type, check to ensure that file is
         * not loarger than Integer.MAX_VALUE;
         */
        if (length > Integer.MAX_VALUE)
        {
            System.out.println("File is too large to process");
            return null;
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while ((offset < bytes.length)
                && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0))
        {

            offset += numRead;

        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length)
        {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;

    }

    public void exec() throws IOException, InterruptedException
    {
        //Execution
        File file = new File("task" + task.getId()).getAbsoluteFile();
//        String[] execString = task.getCommand().getExecString(file.getAbsolutePath());

        createLocalBatch(file.getAbsolutePath(), task.getCommand().toString());

        /*        System.out.println("Executing: ");
        for (int i = 0; i < execString.length; i++)
            System.out.print(execString[i] + " ");
        System.out.println();
*/
        
        Process p = Runtime.getRuntime().exec("\"" + file.getAbsolutePath() + "/" +"myBatch.bat" + "\"", null, file);
        BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = stdout.readLine()) != null)
            System.out.println("stdout: " + line);
        p.waitFor();
    }


    public void createLocalBatch(String path, String command) throws IOException
    {
        FileWriter f = new FileWriter(path + "/" + "myBatch.bat");
        BufferedWriter bw = new BufferedWriter(f);
        bw.write(command);
        bw.close();
    }

    //Unused
    public void run()
    {
        try
        {
            System.out.println("Received task with ID " + task.getId());
            // create current working folder
            createCurrentDir();

            //download files
            downloadFiles();

            //mapping file names and their hashcode
            mapFiles();

            //Execution
            exec();
            
            //Uploading files
            List<FileHandle> uploaded = uploadFiles();

            //delete current folder
            deleteCurrentDir();

            //Notification
            MainServerInterface msi;

            msi = new RMIClientModel<MainServerInterface>(task.getMainServerAddress(),
                    Ports.MainServerPort).getInterface();
            System.out.println("Finished. Sending results to " + task.getMainServerAddress());
            msi.taskCompleted(task.getId(), clusterComputer.uuid, uploaded);
        } catch (Exception ex)
        {
            Logger.getLogger(ClusterComputer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
