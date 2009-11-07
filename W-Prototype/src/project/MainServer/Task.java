package project.MainServer;

public abstract class Task
{
    WorkSession owner;
    
    public void markAsFinished()
    {
        owner.markTaskAsFinished(this);
    }
}
