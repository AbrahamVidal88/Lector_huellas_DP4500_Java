/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Formulario;

import DB.ConexionBD;
import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.*;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author ABRAHAMVIDALPAT
 */
public class CapturaHuella extends javax.swing.JFrame {

    /**
     * Creates new form CapturaHuella
     */
    public CapturaHuella() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Imposible modificar el tema visual.", "Lookandfeel invalido",
            JOptionPane.ERROR_MESSAGE);
        }
        initComponents();
    }
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    protected void Iniciar()
    {
        Lector.addDataListener(new DPFPDataAdapter(){
        @Override public void dataAcquired(final DPFPDataEvent e){
            SwingUtilities.invokeLater(new Runnable() {@Override
        public void run(){
               EnviarTexto("La huella digital ha sido capturada");
               ProcesarCaptura(e.getSample());
            }
            });}
        });
        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter(){
        @Override public void readerConnected(final DPFPReaderStatusEvent e){
        SwingUtilities.invokeLater(new Runnable() {@Override
       public void run(){
            EnviarTexto("El sensor de la huella digital esta activado");
        }});}
        @Override public void readerDisconnected(final DPFPReaderStatusEvent e){
        SwingUtilities.invokeLater(new Runnable() {@Override
       public void run(){
            EnviarTexto("El sensor de la huella digital esta desactivado");
        }});}
        });
        Lector.addSensorListener(new DPFPSensorAdapter(){
        @Override public void fingerTouched(final DPFPSensorEvent e){
        SwingUtilities.invokeLater(new Runnable() {@Override
       public void run(){
            EnviarTexto("El dedo ha sido colocado sobre el lector");
        }});}
        @Override public void fingerGone(final DPFPSensorEvent e){
        SwingUtilities.invokeLater(new Runnable() {@Override
       public void run(){
            EnviarTexto("El dedo ha sido quitado del lector");
        }});}
        });
        Lector.addErrorListener(new DPFPErrorAdapter(){
        public void errorReader(final DPFPErrorEvent e){
        SwingUtilities.invokeLater(new Runnable () {@Override
       public void run(){
            EnviarTexto("Error: "+e.getError());
        }});}
        });

    }
    public DPFPFeatureSet featuresinscripcion;
    public DPFPFeatureSet featureverificacion;
    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose){
        DPFPFeatureExtraction extractor =
        DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try{
            return extractor.createFeatureSet(sample, purpose);
        }catch(DPFPImageQualityException e){
            return null;
        }
    }
    public Image CrearImagenHuella(DPFPSample sample){
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }
    public void DibujarHuella(Image image){
       Lblimagenhuella.setIcon(new ImageIcon(
       image.getScaledInstance(Lblimagenhuella.getWidth(), 
       Lblimagenhuella.getHeight(), 
       Image.SCALE_DEFAULT)));
       repaint();
    }
    public void EstadoHuellas(){
        EnviarTexto("Muestra de huellas necesarias para guardar template" +
        Reclutador.getFeaturesNeeded());
    }
    public void EnviarTexto(String string){
        TxtArea.append(string +"\n");
    }
    public void start(){
        Lector.startCapture();
        EnviarTexto("Utilizando el lector de huellas");
    }
    public void stop(){
        Lector.stopCapture();
        EnviarTexto("No se esta utilizando el lector");
    }
    public DPFPTemplate getTemplate(){
        return template;
    }
    public void setTemplate(DPFPTemplate template){
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }
    public void ProcesarCaptura(DPFPSample sample){
        featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        featureverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
        
    if (featuresinscripcion != null)
    try{
        System.out.println("Las caracteristicas de la huella han sido creadas");
        Reclutador.addFeatures(featuresinscripcion);
        
        Image image=CrearImagenHuella(sample);
        DibujarHuella(image);
        
        BtnVerificar.setEnabled(true);
        BtnIdentificar.setEnabled(true);
    }catch(DPFPImageQualityException ex){
        System.err.println("Error: "+ex.getMessage());
    } 
    finally{
        EstadoHuellas();
        switch(Reclutador.getTemplateStatus())
        {
            case TEMPLATE_STATUS_READY:
                stop();
                setTemplate(Reclutador.getTemplate());
                EnviarTexto("La plantilla de la huella ha sido creada, ya puede identificarla o verificarla");
                BtnIdentificar.setEnabled(false);
                BtnVerificar.setEnabled(false);
                BtnGuardar.setEnabled(true);
                BtnGuardar.grabFocus();
                break;
            
            case TEMPLATE_STATUS_FAILED:
                Reclutador.clear();
                stop();
                EstadoHuellas();
                setTemplate(null);
                JOptionPane.showMessageDialog(CapturaHuella.this, "La plantilla de la huella no puede ser creada, repita el proceso");
                start();
                break;
        }
    }
    }
    ConexionBD cn= new ConexionBD();
    public void guardarHuella() throws SQLException{
        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer tamañoHuella=template.serialize().length;
        
        String nombre = JOptionPane.showInputDialog("Nombre:");
        try{
            Connection c= cn.conectar();
            PreparedStatement guardarStmt = c.prepareStatement
                    ("INSERT INTO somhue(huenombre, huehuella) values(?,?)");
             guardarStmt.setString(1, nombre);
            guardarStmt.setBinaryStream(2, datosHuella, tamañoHuella);
            guardarStmt.execute();
            guardarStmt.close();
            JOptionPane.showMessageDialog(null, "Huella guardada correctamente");
            cn.desconectar();
            BtnGuardar.setEnabled(false);
            BtnVerificar.grabFocus();
        }catch(SQLException ex){
            System.err.println("Error al guardar los datos de la huella");
        }finally{
            cn.desconectar();
        }
    }
    
    public void verificarHuella(String nom){
        try{
            Connection c = cn.conectar();
            PreparedStatement verificarStmt= c.prepareStatement
                    ("SELECT huehuella FROM somhue WHERE huenombre = ?");
            verificarStmt.setString(1, nom);
            ResultSet rs = verificarStmt.executeQuery();
            
            if(rs.next()){
                byte templateBuffer [] = rs.getBytes("huehuella");
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                DPFPVerificationResult result = Verificador.verify(featureverificacion, getTemplate());
                
                if(result.isVerified())
                    JOptionPane.showMessageDialog(null, "La huella capturada coincide con la de "+nom,"Verificacion de huella", JOptionPane.INFORMATION_MESSAGE);
                else
                    JOptionPane.showMessageDialog(null, "No corresponde la huella con "+nom, "Verificacion de huella", JOptionPane.ERROR_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null, "No existe un registro de huella para "+nom, "Verificacion de huella", JOptionPane.ERROR_MESSAGE);
            }
        }catch(SQLException e){
            System.err.println("Error al verificar los datos de la huella");
        }finally{
            cn.desconectar();
        }
    }
    
    public void identificarHuella() throws IOException{
        try{
            Connection c=cn.conectar();
            PreparedStatement identificarStmt =
                    c.prepareStatement("SELECT huenombre, huehuella FROM somhue");
            ResultSet rs = identificarStmt.executeQuery();
            while(rs.next()){
                byte templateBuffer[] = rs.getBytes("huehuella");
                String nombre=rs.getString("huenombre");
                DPFPTemplate referenceTemplate =
                        DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                DPFPVerificationResult result= Verificador.verify(featureverificacion, getTemplate());
                if(result.isVerified()){
                    JOptionPane.showMessageDialog(null, "La huella capturada es de "+ nombre,"Verificacion de Huella", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "No existe ningun registro que coincida con la huella ","Verificacion de Huella", JOptionPane.ERROR_MESSAGE);
            setTemplate(null);
        }catch(SQLException e){
           System.err.println("Error al identificar huella dactilar"+ e.getMessage());
        }
        finally{
            cn.desconectar();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PanHuella = new javax.swing.JPanel();
        Lblimagenhuella = new javax.swing.JLabel();
        PanBtns = new javax.swing.JPanel();
        BtnVerificar = new javax.swing.JButton();
        BtnGuardar = new javax.swing.JButton();
        BtnIdentificar = new javax.swing.JButton();
        BtnSalir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        TxtArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        PanHuella.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(0), "Huella Digital"));

        javax.swing.GroupLayout PanHuellaLayout = new javax.swing.GroupLayout(PanHuella);
        PanHuella.setLayout(PanHuellaLayout);
        PanHuellaLayout.setHorizontalGroup(
            PanHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanHuellaLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(Lblimagenhuella, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(45, Short.MAX_VALUE))
        );
        PanHuellaLayout.setVerticalGroup(
            PanHuellaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanHuellaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Lblimagenhuella, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        PanBtns.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(0), "Acciones"));

        BtnVerificar.setText("Verificar");
        BtnVerificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnVerificarActionPerformed(evt);
            }
        });

        BtnGuardar.setText("Guardar");
        BtnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGuardarActionPerformed(evt);
            }
        });

        BtnIdentificar.setText("Identificar");
        BtnIdentificar.setToolTipText("");
        BtnIdentificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnIdentificarActionPerformed(evt);
            }
        });

        BtnSalir.setText("Salir");
        BtnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSalirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PanBtnsLayout = new javax.swing.GroupLayout(PanBtns);
        PanBtns.setLayout(PanBtnsLayout);
        PanBtnsLayout.setHorizontalGroup(
            PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanBtnsLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(BtnVerificar)
                    .addComponent(BtnIdentificar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(BtnSalir)
                    .addComponent(BtnGuardar))
                .addContainerGap())
        );
        PanBtnsLayout.setVerticalGroup(
            PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanBtnsLayout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnVerificar)
                    .addComponent(BtnGuardar))
                .addGap(46, 46, 46)
                .addGroup(PanBtnsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnIdentificar)
                    .addComponent(BtnSalir))
                .addContainerGap(49, Short.MAX_VALUE))
        );

        TxtArea.setColumns(20);
        TxtArea.setRows(5);
        jScrollPane1.setViewportView(TxtArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(PanBtns, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PanHuella, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PanHuella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(PanBtns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSalirActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_BtnSalirActionPerformed
       
    private void BtnVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnVerificarActionPerformed
        // TODO add your handling code here:
        String nombre = JOptionPane.showInputDialog("Nombre a verificar");
        verificarHuella(nombre);
        Reclutador.clear();
    }//GEN-LAST:event_BtnVerificarActionPerformed

    private void BtnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGuardarActionPerformed
        // TODO add your handling code here:
        try{
            guardarHuella();
            Reclutador.clear();
            Lblimagenhuella.setIcon(null);
            start();
        }catch(SQLException ex){
            Logger.getLogger(CapturaHuella.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_BtnGuardarActionPerformed

    private void BtnIdentificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnIdentificarActionPerformed
        // TODO add your handling code here:
        try{
            identificarHuella();
            Reclutador.clear();
        }catch(IOException ex){
            Logger.getLogger(CapturaHuella.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_BtnIdentificarActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        Iniciar();
        start();
        EstadoHuellas();
        BtnGuardar.setEnabled(false);
        BtnIdentificar.setEnabled(false);
        BtnVerificar.setEnabled(false);
        BtnSalir.grabFocus();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt){
        stop();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CapturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CapturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CapturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CapturaHuella.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new CapturaHuella().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnGuardar;
    private javax.swing.JButton BtnIdentificar;
    private javax.swing.JButton BtnSalir;
    private javax.swing.JButton BtnVerificar;
    private javax.swing.JLabel Lblimagenhuella;
    private javax.swing.JPanel PanBtns;
    private javax.swing.JPanel PanHuella;
    private javax.swing.JTextArea TxtArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
