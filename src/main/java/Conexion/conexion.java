package Conexion;
import java.sql.*;

public class conexion {
    Connection con;
    
    public conexion(){
        try{
           Class.forName("com.mysql.cj.jdbc.Driver");
           con = DriverManager.getConnection("jdbc:mysql://localhost:3306/crudpulperia", "root", "");
           System.out.println("Conexion exitosa");
            
        } catch(Exception e){
            System.out.println("No se puede conectar a la BD");
            e.printStackTrace();
        }
    }
   
    public Connection getConnection(){
        return con;  
    }
}
