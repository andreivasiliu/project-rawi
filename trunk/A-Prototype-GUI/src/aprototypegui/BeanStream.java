/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package aprototypegui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.swing.JTextArea;

/**
 *
 * @author PIC
 */
class BeanStream extends OutputStream
{
    JTextArea textArea;
    //StringWriter stringWriter;

    public BeanStream(JTextArea textArea)
    {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException
    {
        //stringWriter.append((char) b);

        //stringWriter.getBuffer().
        textArea.setText(textArea.getText() + (char)b);
    }
}
