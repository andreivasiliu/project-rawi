package project.ClusterComputer;

public class TcpListener extends Listener
{
    TcpInstance[] connections;
    int port;
    
    public TcpListener(int port)
    {
        this.port = port;
    }
    
    /** Start listening on the given port, and create a TcpInstance
     *  class for each accepted connection.
     */
    
    @Override
    public void run()
    {
        // TODO
        
    }
}
