package model;
import BD.Funciones;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;


import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class Utils {
    static Logger logger = Logger.getLogger(Utils.class);

    public static String getSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            // Convert byte array to hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String getTableSha256(String tableName) {
        try {
            // Obtener datos concatenados de la tabla usando Functions
            String tableData = Funciones.getConcatenatedTableData(tableName);

            if (tableData == null || tableData.isEmpty()) {
                throw new IllegalArgumentException("Table " + tableName + " is empty or does not exist.");
            }
            return getSHA256(tableData);
        } catch (Exception e) {
            logger.error("Error processing table data: ", e);
            throw new RuntimeException("Error processing table data: " + e.getMessage(), e);
        }
    }

    public static JsonObject parseJson(String jsonString) {
        try {
            return JsonParser.parseString(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON format: " + jsonString, e);
        }
    }
    public static String generateUniqueKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // Explicit 256-bit initialization
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error generating unique key: ", e);
            return "";
        }
    }

    public static String calculateHMAC(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKey);
            byte[] hmacBytes = sha256_HMAC.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error calculating HMAC", e);
        }
    }



}
