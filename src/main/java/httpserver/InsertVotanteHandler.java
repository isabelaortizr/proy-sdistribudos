package httpserver;

import BD.Funciones;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InsertVotanteHandler implements HttpHandler {
    static Logger logger = Logger.getLogger(InsertVotanteHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             OutputStream os = exchange.getResponseBody()) {

            // Read request body
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String requestBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

            // Parse JSON
            JsonObject jsonRequest = new com.google.gson.JsonParser().parse(requestBody).getAsJsonObject();
            String codigo = jsonRequest.get("codigo").getAsString();

            // Insert into database
            String llavePrivada = Funciones.insertVotante(codigo);

            // Send response
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "OK");
            jsonResponse.addProperty("llave_privada", llavePrivada);
            String response = jsonResponse.toString();

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            os.write(response.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            logger.error("Error inserting Votante", e);
            exchange.sendResponseHeaders(500, 0); // Send error response
        } finally {
            exchange.close(); // Close the exchange explicitly
        }
    }
}