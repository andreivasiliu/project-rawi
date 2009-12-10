package rawi.mainserver;

public abstract class Task
{
    WorkSession owner;
    
    public void markAsFinished()
    {
        owner.markTaskAsFinished(this);
    }
}
