package commserial;

import java.io.*;
import java.util.logging.*;

/**
 *
 * @author Hernández Jiménez Daniel
 */
public class SerialSender {
    OutputStream out;

    public SerialSender(OutputStream out) {
        this.out = out;
    }
    
    public void send(byte[] bytes){
        try{
            System.out.println("ENVIANDO: "+ new String(bytes,0,bytes.length));
            out.write(bytes);
            System.out.println("Envio realizado con éxito");
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
