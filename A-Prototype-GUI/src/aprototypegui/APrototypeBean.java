/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package aprototypegui;

import java.beans.*;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author PIC
 */
public class APrototypeBean implements Serializable {

    public static final String PROP_SAMPLE_PROPERTY = "sampleProperty";

    private String sampleProperty;

    private PropertyChangeSupport propertySupport;

    public APrototypeBean()
    {
        propertySupport = new PropertyChangeSupport(this);
    }

    protected String consoleText;

    /**
     * Get the value of consoleText
     *
     * @return the value of consoleText
     */
    public String getConsoleText()
    {
        return consoleText;
    }

    /**
     * Set the value of consoleText
     *
     * @param consoleText new value of consoleText
     */
    public void setConsoleText(String consoleText)
    {
        this.consoleText = consoleText;
    }


    public String getSampleProperty() {
        return sampleProperty;
    }

    public void setSampleProperty(String value) {
        String oldValue = sampleProperty;
        sampleProperty = value;
        propertySupport.firePropertyChange(PROP_SAMPLE_PROPERTY, oldValue, sampleProperty);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
