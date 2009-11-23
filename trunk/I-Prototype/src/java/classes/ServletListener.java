
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
            RmiServer server = new RmiServer();
            sc.setAttribute("server", server);
        } catch (RemoteException ex) {
            Logger.getLogger(ServletListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        RmiServer server = (RmiServer)sc.getAttribute("server");
        sc.removeAttribute("server");
        try {
            UnicastRemoteObject.unexportObject(server.registry, true);
        } catch (NoSuchObjectException ex) {
            Logger.getLogger(ServletListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}