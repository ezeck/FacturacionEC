package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FilesDatabase {

    private Connection conn;
    private Connection conn2;

    public FilesDatabase(){
        conn = connect();
    }

    public Connection getConnection(){
        return conn;
    }

    public void destroy(){
        disconnect();
    }

    // Conexion a DB Ecuador Local
    Connection connect() {
        Connection conn1 = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn1 = DriverManager.getConnection("jdbc:mysql://10.59.140.160:33060/ecu01?noAccessToProcedureBodies=true","root","tolaspi943");
            conn1.setAutoCommit(true);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn1;
    }
    
    public Connection connectBSP() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn2 = DriverManager.getConnection("jdbc:mysql://10.40.59.10:33033/bsp?noAccessToProcedureBodies=true","bsp","tolaspi943");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conn2;
    }

    void disconnect(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void disconnectBSP(){
        try {
            conn2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

}
