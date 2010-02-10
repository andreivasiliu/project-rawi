package rawi.common;

import java.io.Serializable;

public class TaskStatus implements Serializable
{
    public enum StatusType { RUNNING, COMPLETED, FAILED, INEXISTENT };
    private StatusType status;

    public TaskStatus(StatusType status)
    {
        this.status = status;
    }

    public StatusType getStatus()
    {
        return status;
    }

    public void setStatus(StatusType status)
    {
        this.status = status;
    }
}
