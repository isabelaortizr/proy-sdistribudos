package BD;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Maneja la conexión a la base de datos SQLite.
 */
public class Database {
    private static Connection connection;
    @Getter
    private static Database instance = new Database();
    private static final String DATABASE_URL = "jdbc:sqlite:testing.db";

    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            connection.setAutoCommit(true);
            System.out.println("Connection to SQLite established.");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
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
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

}
