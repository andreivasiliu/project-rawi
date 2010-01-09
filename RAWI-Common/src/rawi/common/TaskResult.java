package rawi.common;

import java.util.List;

public class TaskResult
{
    public final String id;
    public final String clusterComputerId;
    public final List<FileHandle> files;

    public TaskResult(String id, String clusterComputerId, List<FileHandle> files)
    {
        this.id = id;
        this.clusterComputerId = clusterComputerId;
        this.files = files;
    }
}
