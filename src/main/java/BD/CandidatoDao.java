package BD;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para operaciones sobre la tabla "candidato".
 */
public class CandidatoDao {

    /**
     * Inserta o actualiza un candidato en la base de datos.
     */
    public static void insertOrUpdate(String codigo, String nombre) {
        String sql = "INSERT OR REPLACE INTO candidato (codigo, nombre) VALUES (?,?)";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            pstmt.setString(2, nombre);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un candidato de la base de datos por su código.
     */
    public static void delete(String codigo) {
        String sql = "DELETE FROM candidato WHERE codigo = ?";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
