package classes;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TrackerServletListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        TrackerBean tracker = new TrackerBean();
        ServletContext sc = sce.getServletContext();
        sc.setAttribute("trackerBean", tracker);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}