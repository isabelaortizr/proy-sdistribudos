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

            // Tabla de bloques
            String sqlBloque = "CREATE TABLE IF NOT EXISTS bloque_0001 ("
                    + "id TEXT PRIMARY KEY, "
                    + "codigo_votante TEXT, "
                    + "codigo_candidato TEXT, "
                    + "hash TEXT, "
                    + "ref_anterior_bloque TEXT, "
                    + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + "estado TEXT DEFAULT 'pendiente', "
                    + "FOREIGN KEY (codigo_votante) REFERENCES votante(codigo), "
                    + "FOREIGN KEY (codigo_candidato) REFERENCES candidato(codigo)"
                    + ");";
            stmt.execute(sqlBloque);

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
