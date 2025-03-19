package BD;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.Utils;
import org.apache.log4j.Logger;

import java.sql.*;

public class Funciones {
    private static final int MAX_ROWS_PER_BLOQUE = 5;
    static Logger logger = Logger.getLogger(Funciones.class);

    // Create Votante table
    public static void createVotanteTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Votante (" +
                "codigo TEXT PRIMARY KEY, " +
                "llave_privada TEXT)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.execute();
            logger.info("Created table: Votante");
        } catch (SQLException e) {
            logger.error("Error creating table Votante", e);
        }
    }

    // Insert data into Votante table
    public static String insertVotante(String codigo) {
        System.out.println("insertVotante");
        String llavePrivada = Utils.generateUniqueKey(); // Generate key
        String sql = "INSERT INTO Votante (codigo, llave_privada) VALUES (?, ?)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, codigo);
            statement.setString(2, llavePrivada);
            statement.executeUpdate();
            logger.info("Inserted data into Votante table");
            return llavePrivada; // Return the key
        } catch (SQLException e) {
            logger.error("Error inserting data into Votante table", e);
            return null;
        }
    }

    public static void createCandidatosTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Candidatos (" +
                "id TEXT PRIMARY KEY, " +
                "nombre TEXT)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.execute();
            logger.info("Created table: Candidatos");
        } catch (SQLException e) {
            logger.error("Error creating table Candidatos", e);
        }
    }

    public static void insertCandidato(String id, String nombre) {
        System.out.println("insertCandidato");
        String sql = "INSERT INTO Candidatos (id, nombre) VALUES (?, ?)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, nombre);
            statement.executeUpdate();
            logger.info("Inserted data into Candidatos table");
        } catch (SQLException e) {
            logger.error("Error inserting data into Candidatos table", e);
        }
    }


    public static void createInitialBloqueTable() {
        String nextBlock = "Bloque_0001";

        String sql = "CREATE TABLE IF NOT EXISTS " + nextBlock + " (" +
                "id TEXT PRIMARY KEY, " +
                "codigo_volante TEXT, " +
                "codigo_candidato TEXT, " +
                "hash TEXT, " +
                "ref_anterior_bloque TEXT)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.execute();
            logger.info("Created table: " + nextBlock);
        } catch (SQLException e) {
            logger.error("Error creating table " + nextBlock, e);
        }
    }

    public static void createBloqueTable() {
        String latestBlock = getLatestBlockTable();

        if (!isTableFull(latestBlock)) {
            logger.info("Current " + latestBlock +" table is not full. No new table created.");
            return;
        }

        String nextBlock = getNextBlockName(latestBlock);

        String sql = "CREATE TABLE IF NOT EXISTS " + nextBlock + " (" +
                "id TEXT PRIMARY KEY, " +
                "codigo_volante TEXT, " +
                "codigo_candidato TEXT, " +
                "hash TEXT, " +
                "ref_anterior_bloque TEXT)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.execute();
            logger.info("Created table: " + nextBlock);
        } catch (SQLException e) {
            logger.error("Error creating table " + nextBlock, e);
        }
    }

    public static void insertBloqueData(String id, String codigoVolante, String codigoCandidato) {
        String tableName = getLatestBlockTable();

        if (isTableFull(tableName)) {
            createBloqueTable();
            tableName = getLatestBlockTable(); // Update to the new table name
        }

        String hash = Utils.getSHA256(id + codigoVolante + codigoCandidato);

        String refAnteriorBloque = getPreviousBlockHash(tableName);

        String sql = "INSERT INTO " + tableName + " (id, codigo_volante, codigo_candidato, hash, ref_anterior_bloque) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, codigoVolante);
            statement.setString(3, codigoCandidato);
            statement.setString(4, hash);
            statement.setString(5, refAnteriorBloque);
            statement.executeUpdate();
            logger.info("Inserted data into " + tableName);
        } catch (SQLException e) {
            logger.error("Error inserting data into " + tableName, e);
        }
    }

    private static boolean isTableFull(String tableName) {
        String sql = "SELECT COUNT(*) AS row_count FROM " + tableName;

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                int rowCount = rs.getInt("row_count");
                return rowCount >= MAX_ROWS_PER_BLOQUE;
            }
        } catch (SQLException e) {
            logger.error("Error checking table row count: ", e);
        }

        return false;
    }

    private static String getPreviousBlockHash(String currentTableName) {
        String previousTableName = getPreviousTableName(currentTableName);
        if (previousTableName == null) {
            return "";
        }

        String query = "SELECT hash FROM " + previousTableName + " ORDER BY id DESC LIMIT 1";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getString("hash");
            }
        } catch (SQLException e) {
            logger.error("Error retrieving previous block hash: ", e);
        }

        return "";
    }

    private static String getPreviousTableName(String currentTableName) {
        try {
            int currentBlockNumber = Integer.parseInt(currentTableName.replace("Bloque_", ""));
            if (currentBlockNumber <= 1) {
                return null;
            }
            return String.format("Bloque_%04d", currentBlockNumber - 1);
        } catch (NumberFormatException e) {
            logger.error("Invalid table name format: " + currentTableName, e);
            return null;
        }
    }

    private static String getLatestBlockTable() {
        String latestBlock = "Bloque_0001"; // Default if none exist
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'Bloque_%' ORDER BY name DESC LIMIT 1";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                latestBlock = resultSet.getString("name");
            }
        } catch (SQLException e) {
            logger.error("Error retrieving latest block table", e);
        }

        return latestBlock;
    }

    private static String getNextBlockName(String latestBlock) {
        int lastNumber = Integer.parseInt(latestBlock.replace("Bloque_", ""));
        return String.format("Bloque_%04d", lastNumber + 1);
    }

    public static String getConcatenatedTableData(String tableName) {
        StringBuilder concatenatedData = new StringBuilder();
        String query = "SELECT id, codigo_volante, codigo_candidato, hash, ref_anterior_bloque FROM " + tableName;

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                concatenatedData.append(rs.getString("id"))
                        .append(rs.getString("codigo_volante"))
                        .append(rs.getString("codigo_candidato"))
                        .append(rs.getString("hash"))
                        .append(rs.getString("ref_anterior_bloque"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving table data: ", e);
        }

        return concatenatedData.toString();
    }

    public static String getLlavePrivada(String codigoVotante) {
        String sql = "SELECT llave_privada FROM Votante WHERE codigo = ?";
        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {
            statement.setString(1, codigoVotante);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("llave_privada");
            }
        } catch (SQLException e) {
            logger.error("Error retrieving llave_privada", e);
        }
        return null;
    }


    public static JsonArray getAllVotantes() {
        System.out.println("getAllVotantes");
        JsonArray votantes = new JsonArray();
        String sql = "SELECT codigo, llave_privada FROM Votante";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                JsonObject votante = new JsonObject();
                votante.addProperty("codigo", rs.getString("codigo"));
                votante.addProperty("llave_privada", rs.getString("llave_privada"));
                votantes.add(votante);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving Votantes", e);
        }

        return votantes;
    }

    public static JsonArray getAllCandidatos() {
        System.out.println("getAllCandidatos");
        JsonArray candidatos = new JsonArray();
        String sql = "SELECT id, nombre FROM Candidatos";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                JsonObject candidato = new JsonObject();
                candidato.addProperty("id", rs.getString("id"));
                candidato.addProperty("nombre", rs.getString("nombre"));
                candidatos.add(candidato);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving Candidatos", e);
        }

        return candidatos;
    }

    public static JsonArray getAllBloques() {
        System.out.println("getAllBloques");
        JsonArray bloques = new JsonArray();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'Bloque_%'";

        try (Connection con = Database.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String tableName = rs.getString("name");
                JsonObject bloque = new JsonObject();
                bloque.addProperty("tableName", tableName);

                JsonArray rows = new JsonArray();
                String rowSql = "SELECT * FROM " + tableName;
                try (PreparedStatement rowStatement = con.prepareStatement(rowSql);
                     ResultSet rowRs = rowStatement.executeQuery()) {

                    while (rowRs.next()) {
                        JsonObject row = new JsonObject();
                        row.addProperty("id", rowRs.getString("id"));
                        row.addProperty("codigo_volante", rowRs.getString("codigo_volante"));
                        row.addProperty("codigo_candidato", rowRs.getString("codigo_candidato"));
                        row.addProperty("hash", rowRs.getString("hash"));
                        row.addProperty("ref_anterior_bloque", rowRs.getString("ref_anterior_bloque"));
                        rows.add(row);
                    }
                }
                bloque.add("rows", rows);
                bloques.add(bloque);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving Bloques", e);
        }

        return bloques;
    }
}
