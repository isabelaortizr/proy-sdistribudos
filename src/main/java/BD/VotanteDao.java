package BD;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para operaciones sobre la tabla "votante".
 */
public class VotanteDao {

    /**
     * Inserta o actualiza un votante en la base de datos.
     */
    public static void insertOrUpdate(String codigo, String llavePrivada) {
        String sql = "INSERT OR REPLACE INTO votante (codigo, llave_privada) VALUES (?,?)";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            pstmt.setString(2, llavePrivada);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un votante de la base de datos por su código.
     */
    public static void delete(String codigo) {
        String sql = "DELETE FROM votante WHERE codigo = ?";
        try (PreparedStatement pstmt = Database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, codigo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
