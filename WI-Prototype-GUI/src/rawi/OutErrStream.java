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
    StringBuilder stringBuilder = new StringBuilder(4096);
    JTextArea jta;
    int lines = 0;

    public OutErrStream(JTextArea jta)
    {
        this.jta = jta;
    }

    @Override
    public void write(int b) throws IOException
    {
        stringBuilder.append((char) b);

        if (b == '\n')
        {
            lines++;

            if (lines > 500)
            {
                stringBuilder.delete(0, stringBuilder.indexOf("\n") + 1);
                lines--;
            }

            flush();
        }
    }

    @Override
    public void flush() throws IOException
    {
        jta.setText(stringBuilder.toString());
        jta.setCaretPosition(stringBuilder.length());
    }
}
