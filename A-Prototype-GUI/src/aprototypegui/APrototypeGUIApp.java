/*
 * APrototypeGUIApp.java
 */
package aprototypegui;

import clustercomputer.ClusterComputer;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.rmi.server.ExportException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.xml.sax.InputSource;

/**
 * The main class of the application.
 */
public class APrototypeGUIApp extends SingleFrameApplication
{

    public APrototypeBean aPrototypeBean;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup()
    {
        final APrototypeGUIView view = new APrototypeGUIView(this);
        show(view);

        aPrototypeBean = view.getAPrototypeBean();

        // Redirect console output to the listbox.
        BeanStream writer = new BeanStream(view.getConsoleTextArea());
        PrintStream stream = new PrintStream(writer);
        System.setOut(stream);
        System.setErr(stream);

        System.out.println("Starting Cluster Computer...");

        URL iconImageStream = this.getClass().
                getResource("/resources/icon.gif");
        SystemTray tray = SystemTray.getSystemTray();

        Image iconImage = Toolkit.getDefaultToolkit().getImage(iconImageStream);
        TrayIcon icon = new TrayIcon(iconImage, "A-Prototype");
        icon.addMouseListener(new MouseAdapter() {
            boolean visible = true;

            @Override
            public void mouseClicked(MouseEvent e)
            {
                view.getFrame().setVisible(!visible);
                visible = !visible;
            }
        });

        try
        {
            tray.add(icon);
        }
        catch (AWTException ex)
        {
            Logger.getLogger(APrototypeGUIApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                // FIXME: Change to a MessageBox
                try
                {
                    ClusterComputer cc = new ClusterComputer();
                }
                catch (ExportException e)
                {
                    System.out.println("Unable to start Cluster Computer: "
                            + e.getLocalizedMessage());

                    System.exit(1);
                }
                catch (IOException e)
                {
                    System.out.println("Unable to start Cluster Computer: "
                            + e.getLocalizedMessage());

                    System.exit(1);
                }
            }
        };
        t.start();
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root)
    {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of APrototypeGUIApp
     */
    public static APrototypeGUIApp getApplication()
    {
        return Application.getInstance(APrototypeGUIApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args)
    {
        launch(APrototypeGUIApp.class, args);
    }
}
