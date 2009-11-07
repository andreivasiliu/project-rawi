package project.MainServer;

import java.util.Stack;

public class JobPool
{
    private Stack<Job> jobs = new Stack<Job>();
    private static JobPool jobPool;
    
    public static JobPool getInstance()
    {
        if (jobPool == null)
            jobPool = new JobPool();
        
        return jobPool;
    }
    
    private JobPool()
    {
        
    }
    
    public Job newJob()
    {
        if (jobs.isEmpty())
            return new Job();
        
        return jobs.pop();
    }
    
    public void releaseJob(Job job)
    {
        jobs.push(job);
    }
}
