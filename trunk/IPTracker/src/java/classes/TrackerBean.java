package classes;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrackerBean {
    private int expirationTime = 5;
    private List<IPEntry> ipList = new LinkedList<IPEntry>();

    public List<IPEntry> getIpList() {
        return ipList;
    }

    public void addIpToList(IPEntry newIp) {
        ipList.add(newIp);
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void updateList(Calendar rightNow) {
        List<IPEntry> tempIpList = new LinkedList<IPEntry>();
        for (IPEntry elt : ipList) {
            long age = rightNow.getTimeInMillis() - elt.time;
            int ageInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(age);
            if (ageInMinutes <= expirationTime)
                tempIpList.add(elt);
        }
        ipList = tempIpList;
    }

    public List<String> getStringList(String type, Calendar rightNow) {
        List<String> items = new LinkedList<String>();
        for (IPEntry elt : ipList) {
            if (elt.type.equals(type)) {
                long age = rightNow.getTimeInMillis() - elt.time;
                int ageInSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(age);
                items.add(elt.name + " (" + ageInSeconds + " seconds ago)");
            }
        }
        return items;
    }

    public List<String> getList(String type) {
        List<String> items = new LinkedList<String>();
        for (IPEntry elt : ipList)
            if (elt.type.equals(type))
                items.add(elt.name);
        return items;
    }
}
