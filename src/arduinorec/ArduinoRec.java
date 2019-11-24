package arduinorec;

import commserial.SerialReceiver;
import commserial.SerialSender;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import commserial.InputBufferAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cori
 */
public class ArduinoRec {
    private static final int CONNECTION_TIME_OUT = 2000; 
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int DEFAULT_DATABITS = SerialPort.DATABITS_8;
    private static final int DEFAULT_STOP_BITS = SerialPort.STOPBITS_1;
    private static final int DEFAULT_PARITY = SerialPort.PARITY_NONE;
    
    private static SerialSender sSender;
    private static SerialReceiver sReceiver;
    private static SerialPort sPort;
    
    public void ejecutar(){
        System.out.println("************************************************************************");
        System.out.println("Arduino Recorder by Cori");
        System.out.println("************************************************************************");
        
        do{
            String portName = obtenerCOM();
            System.out.println("Conectando a "+portName+"...\n");

            sPort = conectarArduino(portName);
        }while(sPort == null);

        System.out.println("\nConexión exitosa");
        System.out.println("\n-Para visualizar el menu, inserte el comando <menu>");
        
        String op;
        do{
            Scanner sc = new Scanner(System.in);
            op = sc.nextLine();
            
            switch(op){
                case "menu":
                    System.out.println("\n\nMENU DE OPCIONES\n"
                            + "<excel>\tGuardar datos en archivo .xlsx\n"
                            + "<print>\tMostrar datos en pantalla\n\n"
                            + "<menu>\tmuestra el menu de opciones\n"
                            + "<end>\ttermina la ejecución\n"
                            + "\n");
                    break;
                case "excel":
                    guardarXLSX();
                    break;
                case "print":
                    imprimirEnPantalla();
                    break;
                case "end":
                    if(sPort != null){
                        sPort.removeEventListener();
			sPort.close();
                    }
                    break;
                default:
                    System.out.println("Esa opción no es válida");
                    break;
            }
            
        }while(!op.equals("end"));
    
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        
        ArduinoRec app = new ArduinoRec();
        app.ejecutar();
    }
    
    /**Este método obtiene el puerto COM donde está el ARDUINO conectado
     * 
     * @return nombre del puerto COM donde está conectado el ARDUINO
     */
    public static String obtenerCOM(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Elija el puerto COM donde el ARDUINO esta conectado");
        
        String portName = "COM"; 
        int n=sc.nextInt();
        return portName + n;
    }
    
    public static SerialPort conectarArduino(String portName){
        try {
            //Se realiza la conexión con arduino
            CommPortIdentifier cpIdentifier= CommPortIdentifier.getPortIdentifier(portName);
            sPort = (SerialPort) cpIdentifier.open("ArduinoRecorder",CONNECTION_TIME_OUT);
            
            int[] parametros = obtenerParametros();
            sPort.setSerialPortParams(parametros[0], parametros[1], parametros[2], parametros[3]);
            
            sSender= new SerialSender(sPort.getOutputStream());
            sReceiver= new SerialReceiver(sPort.getInputStream());
            sPort.addEventListener(sReceiver);
            
            return sPort;
        } 
        catch (NoSuchPortException ex) {
            System.out.println("*****El puerto no existe o no hay nada conectado");
        }
        catch (PortInUseException ex) {System.out.println("*****El puerto esta en uso");}
        catch (UnsupportedCommOperationException ex) {System.out.println("*****El puerto no admite los parámetros ingresados");} catch (TooManyListenersException ex) {
            Logger.getLogger(ArduinoRec.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArduinoRec.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static int[] obtenerParametros(){
        System.out.println("Configuración de parametros de puerto:");
        Scanner sc = new Scanner(System.in);
        System.out.println("Desea utilizar la configuración automática (BaudRate = "+DEFAULT_BAUD_RATE+", DataBits = "+DEFAULT_DATABITS+", Stop Bits= "+DEFAULT_STOP_BITS+", paridad= No)? [s/n]");
        String op = "";
        do{
            op = sc.next().toLowerCase();
        }while(!op.equals("s") && !op.equals("n"));

        int[] parametros = {DEFAULT_BAUD_RATE, DEFAULT_DATABITS, DEFAULT_STOP_BITS, DEFAULT_PARITY};
        if(op.equals("s"))
            return parametros;
        else{
            System.out.println("Lamentablemente aun no está disponible esa opción :x");
            return parametros;
        }
    }
    
    public void guardarXLSX(){
        //Se leen y guardan los datos
        Scanner sc = new Scanner(System.in);
        System.out.println("\nIngrese el número de columnas de datos: ");
        int nCol = sc.nextInt();
        String[] nombresCol = new String[nCol];
        int[] tiposCol = new int[nCol];
        for(int i=0; i<nCol; i++){
            System.out.println("\nNombre de la columna "+(i+1)+": ");
            sc.nextLine();
            nombresCol[i] = sc.nextLine();
            System.out.println("Tipo de dato de "+nombresCol[i]+" [numérico[1], String[2], booleano[3] ");
            tiposCol[i] = sc.nextInt();
        }
        
        //Se crea la acción
        List<List> rowsData = new ArrayList();
        
        InputBufferAction guardarImprimir= new InputBufferAction(){
            private int contador = 0;
            @Override
            public void receivingActionPerformed(String buffer){
 
                if(contador == 0){
                    List<Object> row = new ArrayList();
                    rowsData.add(row);
                    if(nCol == 1)
                        System.out.println(buffer);
                    else
                        System.out.print(buffer);
                }
                else
                    System.out.println("\t"+buffer);
                
                //Se guarda e imprime
                //Se hace cast al buffer
                switch(tiposCol[contador]){
                    case 1://Double
                        double numBuffer = Double.parseDouble(buffer);
                        rowsData.get(rowsData.size()-1).add(numBuffer);
                        break;
                    case 3://Boolean
                        boolean boolBuffer = Boolean.parseBoolean(buffer);
                        rowsData.get(rowsData.size()-1).add(boolBuffer);
                    default://String
                        rowsData.get(rowsData.size()-1).add(buffer);
                        break;
                }
                    
                if(contador+1 >= nCol)
                    contador = 0;
                else
                    contador++;
            }

            @Override
            public void notReceivingActionPerformed(){

            }

        };
        
        //Se inicia el proceso
        System.out.println("\nPara comenzar presione ENTER para comenzar (se envía un String \"S\" al ARDUINO y se comienza la lectura de datos) y de nuevo ENTER para terminar");
        String header = "";
        for(int i=0; i<nCol; i++){
            header += nombresCol[i]+"\t"; 
        }
        System.out.println(header);
        sc.nextLine();
        sc.nextLine();
        sSender.send("s\n".getBytes());
        sReceiver.setInputBufferAction(guardarImprimir);
        sPort.notifyOnDataAvailable(true);
        sc.nextLine();
        sPort.notifyOnDataAvailable(false);
        System.out.println("\nRecopilación de datos terminada");
        System.out.println(rowsData.size() + ", " + rowsData.get(0).size());
        System.out.println("Abriendo selector de archivos...");
        //Se abre una ventana para seleccionar la ruta del archivo
        boolean continuar = true;
        do{
            SelectorRutaXLS selector = new SelectorRutaXLS();
            /*JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de excel", "xls");
            chooser.setFileFilter(filter);
            chooser.setDialogTitle("Guardar");
            chooser.setAcceptAllFileFilterUsed(false);
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            frame.setVisible(true);
            //Se posiciona al frente
            frame.setExtendedState(JFrame.ICONIFIED);
            frame.setExtendedState(JFrame.NORMAL);*/
            if (selector.abrirSelector()) {
                String ruta = selector.getRuta();
                if(!ruta.endsWith(".xls"))
                    ruta = ruta + ".xls";
                try {
                        File archivoXLS = new File(ruta);
                        boolean registrar = true;
                        if (archivoXLS.exists()) {
                            registrar = false;
                            System.out.println("El archivo ya existe, desea sobreescribirlo? [s/n]");
                            String opcion = sc.nextLine().toLowerCase();
                            if(opcion.equals("s")){
                                registrar = true;
                                archivoXLS.delete();
                            }                        

                        }
                        if(registrar){
                            archivoXLS.createNewFile();
                            Workbook libro = new HSSFWorkbook();
                            FileOutputStream archivo = new FileOutputStream(archivoXLS);
                            Sheet hoja = libro.createSheet("Hoja 1");
                            hoja.setDisplayGridlines(true);

                            //Se crean las celdas con los datos
                            int nRow = rowsData.size();
                            for(int i=0; i<nRow; i++){
                                Row row = hoja.createRow(i);
                                for(int j=0; j<rowsData.get(i).size(); j++){
                                    Cell celda = row.createCell(j);
                                    switch(tiposCol[j]){
                                        case 1://Double
                                            celda.setCellValue((Double)rowsData.get(i).get(j));
                                            break;
                                        case 3://Boolean
                                            celda.setCellValue((Boolean)rowsData.get(i).get(j));
                                            break;
                                        default://String
                                            celda.setCellValue((String)rowsData.get(i).get(j));
                                            break;
                                    }                      
                                }
                            }

                            //Se escribe el archivo
                            libro.write(archivo);
                            archivo.close();
                            System.out.println("Abriendo Excel...");
                            Desktop.getDesktop().open(archivoXLS);
                            System.out.println("\nRegistro Exitoso");
                            System.out.println();
                        }
                        else{
                            System.out.println("Desea seleccionar otra ruta? [s/n]");
                            String opcion = sc.nextLine().toLowerCase();
                            if(!opcion.equals("n")){
                                continuar = false;
                            }
                        }

                    }catch (IOException ex) {
                            Logger.getLogger(ArduinoRec.class.getName()).log(Level.SEVERE, null, ex);
                    }

            }
        }while(!continuar);
             
    }
    
    public void imprimirEnPantalla(){
        InputBufferAction imprimir= new InputBufferAction(){
                @Override
                public void receivingActionPerformed(String buffer){
                    System.out.println(buffer);
                }
                
                @Override
                public void notReceivingActionPerformed(){
                    
                }

        };
        
        Scanner sc = new Scanner(System.in);
       
        System.out.println("Para comenzar presione ENTER para comenzar (se envía un String \"S\" al ARDUINO y se comienza la lectura de datos) y de nuevo ENTER para terminar");
        sc.nextLine();
        sSender.send("s\n".getBytes());
        sReceiver.setInputBufferAction(imprimir);
        sPort.notifyOnDataAvailable(true);
        sc.nextLine();
        sPort.notifyOnDataAvailable(false);
        System.out.println("Impresion terminada");
    }
}
