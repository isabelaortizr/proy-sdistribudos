package BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Maneja la conexión a la base de datos SQLite.
 */
public class Database {
    private static Connection connection;

    static {
        try {
            // Carga el driver de SQLite
            Class.forName("org.sqlite.JDBC");
            // Crea o abre la base de datos (archivo "mydb.db")
            connection = DriverManager.getConnection("jdbc:sqlite:mydb.db");
            // Crea las tablas si no existen
            crearTablas();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static void crearTablas() {
        try (Statement stmt = connection.createStatement()) {
            // Tabla de candidatos
            String sqlCandidato = "CREATE TABLE IF NOT EXISTS candidato ("
                    + "codigo TEXT PRIMARY KEY, "
                    + "nombre TEXT"
                    + ");";
            stmt.execute(sqlCandidato);

            // Tabla de votantes
            String sqlVotante = "CREATE TABLE IF NOT EXISTS votante ("
                    + "codigo TEXT PRIMARY KEY, "
                    + "llave_privada TEXT"
                    + ");";
            stmt.execute(sqlVotante);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Devuelve la conexión única a la base de datos.
     */
    public static Connection getConnection() {
        return connection;
    }
}
