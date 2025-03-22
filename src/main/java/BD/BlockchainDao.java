package BD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones sobre la tabla "bloque_0001" (blockchain).
 */
public class BlockchainDao {

    /**
     * Inserta un nuevo bloque en la cadena.
     */
    public static boolean insertarBloque(String id, String codigoVotante, String codigoCandidato, 
                                       String hash, String refAnteriorBloque) {
        String sql = "INSERT INTO bloque_0001 (id, codigo_votante, codigo_candidato, hash, ref_anterior_bloque) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, codigoVotante);
            pstmt.setString(3, codigoCandidato);
            pstmt.setString(4, hash);
            pstmt.setString(5, refAnteriorBloque);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el estado de un bloque.
     */
    public static boolean actualizarEstado(String id, String estado) {
        String sql = "UPDATE bloque_0001 SET estado = ? WHERE id = ?";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setString(2, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el último bloque de la cadena.
     */
    public static String obtenerUltimoBloque() {
        String sql = "SELECT id, hash FROM bloque_0001 WHERE estado = 'confirmado' ORDER BY timestamp DESC LIMIT 1";
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("hash");
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si un votante ya ha votado.
     */
    public static boolean haVotado(String codigoVotante) {
        String sql = "SELECT COUNT(*) as count FROM bloque_0001 WHERE codigo_votante = ? AND estado = 'confirmado'";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, codigoVotante);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los bloques pendientes.
     */
    public static List<String[]> obtenerBloquesPendientes() {
        String sql = "SELECT id, codigo_votante, codigo_candidato, hash, ref_anterior_bloque " +
                    "FROM bloque_0001 WHERE estado = 'pendiente' ORDER BY timestamp ASC";
        List<String[]> bloques = new ArrayList<>();
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String[] bloque = new String[5];
                bloque[0] = rs.getString("id");
                bloque[1] = rs.getString("codigo_votante");
                bloque[2] = rs.getString("codigo_candidato");
                bloque[3] = rs.getString("hash");
                bloque[4] = rs.getString("ref_anterior_bloque");
                bloques.add(bloque);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bloques;
    }

    /**
     * Verifica la integridad de la cadena de bloques.
     */
    public static boolean verificarIntegridad() {
        String sql = "SELECT id, hash, ref_anterior_bloque FROM bloque_0001 WHERE estado = 'confirmado' ORDER BY timestamp ASC";
        try (Statement stmt = Database.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            String hashAnterior = null;
            while (rs.next()) {
                String refAnterior = rs.getString("ref_anterior_bloque");
                if (hashAnterior != null && !hashAnterior.equals(refAnterior)) {
                    return false;
                }
                hashAnterior = rs.getString("hash");
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 