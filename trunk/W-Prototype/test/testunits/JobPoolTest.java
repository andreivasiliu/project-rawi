package testunits;

import org.junit.Test;
import static org.junit.Assert.*;

import project.MainServer.Job;
import project.MainServer.JobPool;


public class JobPoolTest
{
    @Test
    public void testGetInstance()
    {
        JobPool pool = JobPool.getInstance();
        assertEquals(pool, JobPool.getInstance());
    }

    @Test
    public void testPool()
    {
        assertNotNull(JobPool.getInstance().newJob());
    }
    
    @Test
    public void testRelease()
    {
        Job job = JobPool.getInstance().newJob();
        JobPool.getInstance().releaseJob(job);
        assertEquals(job, JobPool.getInstance().newJob());
    }
}
