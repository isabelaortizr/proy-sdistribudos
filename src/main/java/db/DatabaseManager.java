package db;

import java.sql.*;
import model.Voto;
import java.util.List;
import java.util.ArrayList;
import api.dto.CandidatoResponse;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:mydb.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(URL);
            inicializarTablas();
            System.out.println("Conexión a SQLite establecida correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void inicializarTablas() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Tabla de votantes
            stmt.execute("CREATE TABLE IF NOT EXISTS votantes (" +
                    "codigo VARCHAR(50) PRIMARY KEY," +
                    "nombre VARCHAR(100) NOT NULL," +
                    "dni VARCHAR(20) NOT NULL UNIQUE," +
                    "activo BOOLEAN DEFAULT TRUE)");

            // Tabla de candidatos
            stmt.execute("CREATE TABLE IF NOT EXISTS candidatos (" +
                    "codigo VARCHAR(50) PRIMARY KEY," +
                    "nombre VARCHAR(100) NOT NULL," +
                    "partido VARCHAR(100)," +
                    "activo BOOLEAN DEFAULT TRUE)");

            // Tabla de votos
            stmt.execute("CREATE TABLE IF NOT EXISTS votos (" +
                    "id VARCHAR(50) PRIMARY KEY," +
                    "codigo_votante VARCHAR(50) NOT NULL," +
                    "codigo_candidato VARCHAR(50) NOT NULL," +
                    "hash VARCHAR(256) NOT NULL," +
                    "ref_anterior_bloque VARCHAR(256)," +
                    "confirmado BOOLEAN DEFAULT FALSE," +
                    "timestamp BIGINT DEFAULT (UNIX_TIMESTAMP() * 1000)," +
                    "FOREIGN KEY (codigo_votante) REFERENCES votantes(codigo)," +
                    "FOREIGN KEY (codigo_candidato) REFERENCES candidatos(codigo))");
        }
    }

    public void registrarVoto(String id, String codigoVotante, String codigoCandidato, 
                              String hash, String refAnteriorBloque) throws SQLException {
        String sql = "INSERT INTO votos (id, codigo_votante, codigo_candidato, hash, ref_anterior_bloque, confirmado) " +
                    "VALUES (?, ?, ?, ?, ?, false)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, codigoVotante);
            stmt.setString(3, codigoCandidato);
            stmt.setString(4, hash);
            stmt.setString(5, refAnteriorBloque);
            stmt.executeUpdate();
        }
    }

    public void eliminarVoto(String idVoto) throws SQLException {
        String sql = "DELETE FROM votos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idVoto);
            stmt.executeUpdate();
        }
    }

    public boolean esVotanteValido(String codigoVotante) throws SQLException {
        // Verificar que el votante exista y no haya votado
        String sql = "SELECT COUNT(*) FROM votantes v " +
                    "LEFT JOIN votos vt ON v.codigo = vt.codigo_votante " +
                    "WHERE v.codigo = ? AND vt.id IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigoVotante);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void confirmarVoto(String idVoto) throws SQLException {
        String sql = "UPDATE votos SET confirmado = TRUE WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idVoto);
            stmt.executeUpdate();
        }
    }

    public boolean existeVoto(String idVoto) throws SQLException {
        String sql = "SELECT COUNT(*) FROM votos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idVoto);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public boolean estaConfirmado(String idVoto) throws SQLException {
        String sql = "SELECT confirmado FROM votos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idVoto);
            var rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean("confirmado");
        }
    }

    public void eliminarVotoExpirado(String idVoto) throws SQLException {
        String sql = "DELETE FROM votos WHERE id = ? AND confirmado = FALSE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idVoto);
            stmt.executeUpdate();
        }
    }

    // Métodos para Votantes
    public void registrarVotante(String codigo, String nombre, String dni) throws SQLException {
        String sql = "INSERT INTO votantes (codigo, nombre, dni) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setString(2, nombre);
            stmt.setString(3, dni);
            stmt.executeUpdate();
        }
    }

    public void eliminarVotante(String codigo) throws SQLException {
        String sql = "UPDATE votantes SET activo = FALSE WHERE codigo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.executeUpdate();
        }
    }

    public List<String[]> listarVotantes() throws SQLException {
        List<String[]> votantes = new ArrayList<>();
        String sql = "SELECT codigo, nombre, dni FROM votantes WHERE activo = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                votantes.add(new String[]{
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("dni")
                });
            }
        }
        return votantes;
    }

    // Métodos para Candidatos
    public void registrarCandidato(String codigo, String nombre, String partido) throws SQLException {
        String sql = "INSERT INTO candidatos (codigo, nombre, partido) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setString(2, nombre);
            stmt.setString(3, partido);
            stmt.executeUpdate();
        }
    }

    public void eliminarCandidato(String codigo) throws SQLException {
        String sql = "UPDATE candidatos SET activo = FALSE WHERE codigo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.executeUpdate();
        }
    }

    public List<String[]> listarCandidatos() throws SQLException {
        List<String[]> candidatos = new ArrayList<>();
        String sql = "SELECT codigo, nombre, partido FROM candidatos WHERE activo = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                candidatos.add(new String[]{
                    rs.getString("codigo"),
                    rs.getString("nombre"),
                    rs.getString("partido")
                });
            }
        }
        return candidatos;
    }

    public List<CandidatoResponse> obtenerCandidatos() {
        List<CandidatoResponse> candidatos = new ArrayList<>();
        String sql = "SELECT codigo, nombre FROM candidatos";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                candidatos.add(new CandidatoResponse(
                    rs.getString("codigo"),
                    rs.getString("nombre")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener candidatos: " + e.getMessage());
        }
        
        return candidatos;
    }

    public boolean existeCandidato(String codigo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM candidatos WHERE codigo = ? AND activo = TRUE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public String obtenerUltimaReferenciaBloque() {
        String sql = "SELECT hash FROM votos WHERE confirmado = TRUE ORDER BY timestamp DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("hash");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener última referencia de bloque: " + e.getMessage());
        }
        return "";
    }

    public void cerrar() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    public void actualizarEstadoVoto(String idVoto, boolean confirmado) {
        String sql = confirmado ? 
            "UPDATE votos SET estado = 'CONFIRMADO' WHERE id = ?" :
            "UPDATE votos SET estado = 'RECHAZADO' WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idVoto);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando estado del voto", e);
        }
    }

    public void sincronizarBloque(String idBloque, String refAnterior, long timestamp, String datosBloque) {
        String sql = "INSERT INTO bloques (id, ref_anterior, timestamp, datos) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, idBloque);
            stmt.setString(2, refAnterior);
            stmt.setLong(3, timestamp);
            stmt.setString(4, datosBloque);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error sincronizando bloque", e);
        }
    }

    public int obtenerTotalNodos() {
        try {
            String sql = "SELECT COUNT(*) FROM nodos WHERE activo = 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error obteniendo total de nodos: " + e.getMessage());
            return 1; // Por defecto, asumimos que solo está este nodo
        }
    }
} 