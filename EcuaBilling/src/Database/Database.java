package Database;

import Utils.Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private Connection conn;
    private String env;

    public Database(String environment){
        this.env = environment;
        this.conn = connect(env, "2016", false);
    }

    public String getEnv() {
    	return this.env;
    }
    public void switchTo(String year){
        destroy(true);

        this.conn = connect(env, year, true);
    }

    public Connection getConnection(){
        return conn;
    }

    public void destroy(boolean switching){
        disconnect(switching);
    }

    public void reconnect() { conn = connect(env, "2016", false); }

    public void rollback(){
        if(conn != null){
            try {
                conn.rollback();
                System.out.println("<"+ Utils.getNow()+"> [DATABASE] FAIL - Rollback");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public void commit(){
        if(conn != null){
            try {
                conn.commit();
                System.out.println("<"+ Utils.getNow()+"> [DATABASE] SUCCESS - Commit");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    Connection connect(String environment, String year, boolean switching) {
        Connection conn1 = null;
        String conQuery, logDesc;
        if(environment.equals("PROD")){
            if(year.compareTo("2015") == 0)
                conQuery = "jdbc:jtds:sqlserver://10.93.140.3;databaseName=COMP01_04;user=Quick;autoReconnect=true";
            else
                conQuery = "jdbc:jtds:sqlserver://10.93.140.3;databaseName=COMP01;user=Quick;autoReconnect=true";

            logDesc = "PRODUCTION";
        } else {
            conQuery = "jdbc:jtds:sqlserver://10.93.140.3;databaseName=COMP00;user=Quick;autoReconnect=true";
            logDesc = "TEST";
        }
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn1 = DriverManager.getConnection(conQuery);
            conn1.setAutoCommit(false);

            if(!switching)
                System.out.println("<"+ Utils.getNow()+"> [DATABASE] Conectado a Quick - "+logDesc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn1;
    }

    void disconnect(boolean switching){
        try {
            conn.close();

            if(!switching)
                System.out.println("\n<"+ Utils.getNow()+"> [DATABASE] Desconectado de Quick");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}