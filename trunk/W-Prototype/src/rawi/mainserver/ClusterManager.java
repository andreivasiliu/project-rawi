package rawi.mainserver;

import java.util.LinkedList;
import java.util.List;

/** A manager that keeps and updates a list of working sessions.
 */
public class ClusterManager
{
    List<WorkSession> sessionList = new LinkedList<WorkSession>();

    /** Finds a pending task from any of the managed working sessions.
     */
    public Task getPendingTask()
    {
        // TODO
        return null;
    }
}