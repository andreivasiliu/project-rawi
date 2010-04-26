/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

/**
 *
 * @author PIC
 */
public class OutErrStream extends OutputStream
{
    JTextArea jta;

    public OutErrStream(JTextArea jta)
    {
        this.jta = jta;
    }

    @Override
    public void write(int b) throws IOException
    {
        jta.setText(jta.getText() + (char)b);
    }
}
