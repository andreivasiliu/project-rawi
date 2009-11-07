package project.ClusterComputer;

public class MainApplication
{
    //private Task[] tasks;
    //private Instance[] instances;
    
    public static void main(String[] args)
        throws Throwable
    {
        MainApplication app = new MainApplication();
        
        app.run();
    }
    
    public void run()
        throws InterruptedException
    {
        TcpListener listener = new TcpListener(1239);
        
        listener.run();
        
        listener.join();
    }
}
