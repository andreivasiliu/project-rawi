/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import rawi.common.FileHandle;

/**
 *
 * @author Andrey
 */
public class ClusterCache
{

    private HashMap<String, CacheFileData> resources;
    private final static long MAX_CACHE_SIZE = 100 * 1024 * 1024;

    public ClusterCache()
    {
        resources = new HashMap<String, CacheFileData>();
        File file = new File("cache");

        file.mkdir();

        String[] files = file.list();

        for (String f : files)
        {
            resources.put(f, new CacheFileData(f));
        }
    }

    public boolean isInCache(FileHandle file)
    {
        return resources.containsKey(file.getUniqueId());
    }

    public void copyFromCache(FileHandle file, String taskPath)
            throws IOException
    {
        if (resources.containsKey(file.getUniqueId()))
        {
            copy("cache/" + file.getUniqueId(), taskPath + "/" + file.getLogicalName());
            Calendar calendar = Calendar.getInstance();
            ((CacheFileData)resources.get(file.getUniqueId())).downloadDate = calendar.getTime();
        }
    }

    public void setInCache(FileHandle file, String taskPath)
            throws IOException
    {
        if (!resources.containsKey(file.getUniqueId()))
        {
            copy(taskPath + "/" + file.getLogicalName(), "cache/" + file.getUniqueId());
            resources.put(file.getUniqueId(), new CacheFileData(file.getUniqueId()));
            refresh();
        }
    }

    private void refresh()
    {
        while (cacheIsOverLimit())
        {
            deleteOldestFile();
        }
    }

    private boolean cacheIsOverLimit()
    {
        long cacheSize = 0;

        Set<Entry<String, CacheFileData>> entries = resources.entrySet();
        for (Entry e : entries)
        {
            cacheSize += ((CacheFileData) e.getValue()).size;
        }

        return cacheSize > MAX_CACHE_SIZE ? true : false;
    }

    private void deleteOldestFile()
    {
        Set<Entry<String, CacheFileData>> entries = resources.entrySet();
        Entry old = null;
        for (Entry e : entries)
        {
            if (old == null)
            {
                old = e;
            }
            else if (((CacheFileData) old.getValue()).downloadDate.getTime() > ((CacheFileData) e.getValue()).downloadDate.getTime())
            {
                old = e;
            }
        }

        resources.remove((String) old.getKey());
    }

    private void copy(String from, String to) throws IOException
    {
        FileInputStream in = new FileInputStream(from);
        FileOutputStream out = new FileOutputStream(to);
        byte[] bytes = new byte[4096];
        int bytesRead;

        while ((bytesRead = in.read(bytes, 0, 4096)) != -1) {
            out.write(bytes, 0, bytesRead);
        }
        
        in.close();
        out.close();
    }
}

class CacheFileData
{

    Date downloadDate;
    long size;

    public CacheFileData(String UUID)
    {
        Calendar calendar = Calendar.getInstance();
        downloadDate = calendar.getTime();
        size = new File("cache/" + UUID).length();
    }

    public CacheFileData(Date downloadDate, long size)
    {
        this.downloadDate = downloadDate;
        this.size = size;
    }

}
