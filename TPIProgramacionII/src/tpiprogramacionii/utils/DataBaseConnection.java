package tpiprogramacionii.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DataBaseConnection {
   /* private static Connection connection = null;

    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        }

        try (InputStream input = DataBaseConnection.class.getClassLoader().getResourceAsStream("config/db.properties")) {
            if (input == null) {
                throw new IOException("No se encontro el archivo db.properties");
            }

            Properties prop = new Properties();
            prop.load(input);

            String host = prop.getProperty("db.host");
            String port = prop.getProperty("db.port");
            String database = prop.getProperty("db.database");
            String user = prop.getProperty("db.user");
            String password = prop.getProperty("db.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";

            connection = DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            System.err.println("Error de conexion SQL: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de propiedades: " + e.getMessage());
        }

        return connection;
    }
    */
        private static final String URL = "jdbc:mariadb://localhost:3306/pruebaconjava";
        private static final String USER = "root";      // tu usuario de MariaDB/MySQL
        private static final String PASSWORD = "";      // contraseña, si no tienes deja vacío

        public static Connection getConnection() {
            try {
                // Forzar carga del driver de MariaDB
                Class.forName("org.mariadb.jdbc.Driver");

                // Conectar a la base de datos
                return DriverManager.getConnection(URL, USER, PASSWORD);

            } catch (ClassNotFoundException e) {
                System.err.println("Driver de MariaDB no encontrado: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Error al conectar a la base de datos: " + e.getMessage());
            }
            return null;
        }   
    
}
