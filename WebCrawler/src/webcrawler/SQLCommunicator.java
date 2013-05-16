/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webcrawler;

import com.sun.corba.se.impl.util.Version;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.apache.log4j.Logger;

/**
 *
 * @author adeesha
 */
public class SQLCommunicator {
    
    
    
    public static void communicate(String author, String date, String topic, String content){
     Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        String url = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8";
        String user = "root";
        String password = "";

        try {
            con = DriverManager.getConnection(url, user, password);
            st =  con.createStatement();
         
           st = con.createStatement();
st.executeUpdate("INSERT INTO Lankadeepa (Author,Date , Topic,Content)\n" +
"VALUES ('"+author+"','"+date+"','"+ topic+"','"+content+"')");
           // if (rs.next()) {
               // System.out.println(rs.getString(1));
           // }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Version.class.getName());
            //lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(Version.class.getName());
              //  lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
    
}
