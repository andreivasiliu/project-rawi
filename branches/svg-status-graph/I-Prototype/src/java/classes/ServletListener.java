
package classes;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class ServletListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        try {
            RMIWebServer server = new RMIWebServer();
            sc.setAttribute("server", server);

            // initialize bean
            MainBean theBean = new MainBean();
            sc.setAttribute("mainBean", theBean);

           
            
        } catch (RemoteException ex) {
            Logger.getLogger(ServletListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        RMIWebServer server = (RMIWebServer)sc.getAttribute("server");
        sc.removeAttribute("server");
        sc.removeAttribute("mainBean");

        // TODO delete temporary files (session files)
        
        try {
            UnicastRemoteObject.unexportObject(server.getRegistry(), true);
        } catch (NoSuchObjectException ex) {
            Logger.getLogger(ServletListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}