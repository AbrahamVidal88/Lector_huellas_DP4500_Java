/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author ABRAHAMVIDALPAT
 */
public class ConexionBD {
    
    public String puerto="3306";
    public String nomservidor="localhost";
    public String db="huellasd";
    public String user="root";
    public String pass="";
    Connection conn=null;
    
 public Connection conectar()
 {
     try{
         String ruta="jdbc:mysql://";
         String servidor=nomservidor+":"+puerto+"/";
         Class.forName("com.mysql.jdbc.Driver");
         conn = DriverManager.getConnection(ruta+servidor+db,user,pass);
         if(conn!=null){
             System.out.println("Conexion a la base de datos lista.");
         }
         else if(conn==null){
             throw new SQLException();
         }
         }
         catch(SQLException e){
         JOptionPane.showMessageDialog(null, e.getMessage());
         }
         catch(ClassNotFoundException e){
         JOptionPane.showMessageDialog(null, "Se produjo el siguiente error: "+ e.getMessage());
         }
         catch(NullPointerException e){
             JOptionPane.showMessageDialog(null, "Se produjo el siguiente error: "+e.getMessage());
         }
     finally{
         return conn;
     }
 }
 public void desconectar(){
     conn=null;
     System.out.println("Desconexion a la base de datos lista.");
 }
}
