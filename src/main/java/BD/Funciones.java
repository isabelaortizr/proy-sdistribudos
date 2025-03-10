package BD;

import java.sql.*;

public class Funciones {

    public static void insertCandidato(String codigo, String nombre) {
        String sql = "INSERT INTO Candidatos (codigo, nombre) VALUES (?, ?)";

        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.setString(2, nombre);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCandidato(String codigo) {
        String sql = "DELETE FROM Candidatos WHERE codigo = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertVotante(String codigo, String llavePrivada) {
        String sql = "INSERT INTO Votantes (codigo, llave_privada) VALUES (?, ?)";

        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.setString(2, llavePrivada);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteVotante(String codigo) {
        String sql = "DELETE FROM Votantes WHERE codigo = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
