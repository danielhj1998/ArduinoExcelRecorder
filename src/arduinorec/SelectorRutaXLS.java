package arduinorec;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author dolph
 */
public class SelectorRutaXLS extends JFrame{
    private final JFileChooser chooser;
    
    public SelectorRutaXLS() {
        chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos de excel", "xls");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Guardar");
        chooser.setAcceptAllFileFilterUsed(false);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        
    }
    
    public boolean abrirSelector(){
        this.setVisible(true);
        //Se posiciona al frente
        this.setExtendedState(JFrame.ICONIFIED);
        this.setExtendedState(JFrame.NORMAL);
        
        return chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION;
    }
    
    public String getRuta(){
        return chooser.getSelectedFile().toString();
    }
    
}
