/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import classes.IPEntry;
import classes.TrackerBean;
import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ioana
 */
public class GetIPServletTest {

    TrackerBean tb;

    public GetIPServletTest() {
    }

    @Before
    public void setUp() throws Exception {
        tb = new TrackerBean();
        tb.addIpToList(new IPEntry("name1", "t1", 123));
        tb.addIpToList(new IPEntry("name2", "t2", 124));
        tb.addIpToList(new IPEntry("name3", "t3", 125));
        tb.setExpirationTime(3);
    }

    @Test
    public void testSetExpirationTime() {
        tb.setExpirationTime(3);
        assertEquals(3, tb.getExpirationTime());
    }

    @Test
    public void testUpdateList() throws Exception {
        assertEquals(3, tb.getIpList().size());
        tb.setExpirationTime(0);
        tb.updateList(Calendar.getInstance());
        assertEquals(0, tb.getIpList().size());
    }

}