
package tpiprogramacionii.main;

import java.sql.Connection;
import tpiprogramacionii.utils.DataBaseConnection;


public class TestConnection {
         public static void main(String[] args) {
        System.out.println("Probando conexión a la base de datos...");

        try (Connection conn = DataBaseConnection.getConnection()) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión exitosa a la BD.");
            } else {
                System.out.println(" La conexión se obtuvo pero está cerrada.");
            }

        } catch (Exception e) {
            System.out.println("Error al conectar a la BD:");
                 }
    }
}
