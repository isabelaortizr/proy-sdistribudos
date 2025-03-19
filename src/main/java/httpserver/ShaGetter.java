package httpserver;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.crypto.trep.Utils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ShaGetter implements HttpHandler {
    static Logger logger = Logger.getLogger(ShaGetter.class);

    @Override
    public void handle(HttpExchange he) throws IOException {
        String response;
        Headers responseHeaders = he.getResponseHeaders();
        responseHeaders.add("Access-Control-Allow-Origin", "*");
        responseHeaders.add("Access-Control-Allow-Methods", "GET, OPTIONS");
        responseHeaders.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de preflight para CORS
        if ("OPTIONS".equalsIgnoreCase(he.getRequestMethod())) {
            he.sendResponseHeaders(204, -1);
            return;
        }

        if (he.getRequestMethod().equalsIgnoreCase("GET")) {
            try {
                // Obtener el nombre de la tabla desde la consulta GET
                String query = he.getRequestURI().getQuery();
                if (query == null || !query.contains("=")) {
                    throw new IllegalArgumentException("Invalid query format. Expected ?table=Bloque_XXXX");
                }

                String tableName = query.substring(query.indexOf('=') + 1);

                // Use the new Utils function to get the SHA-256 hash
                String sha256Hash = Utils.getTableSha256(tableName);

                // Respuesta JSON
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "OK");
                jsonResponse.addProperty("table", tableName);
                jsonResponse.addProperty("hash", sha256Hash);

                response = jsonResponse.toString();
                he.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            } catch (Exception e) {
                // Manejo de errores
                logger.error("Error processing request: ", e);
                JsonObject errorObject = new JsonObject();
                errorObject.addProperty("status", "NOK");
                errorObject.addProperty("message", "Error: " + e.getMessage());
                response = errorObject.toString();
                he.sendResponseHeaders(500, response.length());
            }
        } else {
            // Método no soportado
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("status", "NOK");
            errorObject.addProperty("message", "Method not supported");
            response = errorObject.toString();
            he.sendResponseHeaders(405, response.length());
        }

        OutputStream os = he.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}