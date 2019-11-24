package commserial;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.*;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hernández Jiménez Daniel
 */
public class SerialReceiver implements SerialPortEventListener{
    private BufferedReader in;
    private InputBufferAction bufferListener;
    private final int SLEEP_TIME=100;
    
    public SerialReceiver(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));
    }
    
    public SerialReceiver(InputStream in, InputBufferAction bufferListener) {
        this(in);
        this.bufferListener=bufferListener;

    }
    
    public void setInputBufferAction(InputBufferAction bufferListener){
        this.bufferListener=bufferListener;
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        if (spe.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {                    
                    bufferListener.receivingActionPerformed(in.readLine());
            } catch (Exception e) {
                    System.err.println(e.toString());
            }
        }
    }

    
}
