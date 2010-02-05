/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clustercomputer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    {
        if (resources.containsKey(file.getUniqueId()))
        {
            copy("cache/" + file.getUniqueId(), taskPath + "/" + file.getLogicalName());
        }
    }

    public void setInCache(FileHandle file, String taskPath)
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

    private void copy(String from, String to)
    {
        FileReader in = null;
        {
            FileWriter out = null;
            try
            {
                File inputFile = new File(from);
                File outputFile = new File(to);
                in = new FileReader(inputFile);
                out = new FileWriter(outputFile);
                int c;
                while ((c = in.read()) != -1)
                {
                    out.write(c);
                }
            } catch (IOException ex)
            {
                Logger.getLogger(ClusterCache.class.getName()).log(Level.SEVERE, null, ex);
            }
            finally
            {
                try
                {
                    in.close();
                } catch (IOException ex)
                {
                    Logger.getLogger(ClusterCache.class.getName()).log(Level.SEVERE, null, ex);
                }
                try
                {
                    out.close();
                } catch (IOException ex)
                {
                    Logger.getLogger(ClusterCache.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
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
