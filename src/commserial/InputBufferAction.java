package commserial;

/**
 *
 * @author Cori
 */
public interface InputBufferAction{
    /**Este método deber ser llamado cuando un buffer es leído del serial
     * 
     * @param buffer 
     */
    public void receivingActionPerformed(String buffer);
    
    /**Este método deber ser llamado cuando no se recibe nada del serial
     * 
     */
    public void notReceivingActionPerformed();
}
