package httpserver;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.crypto.trep.DataBase.Functions;
import edu.upb.crypto.trep.DataBase.models.Candidato;
import edu.upb.crypto.trep.bl.AltaCandidato;
import edu.upb.crypto.trep.bl.Comando;
import edu.upb.crypto.trep.modsincronizacion.PlanificadorMensajesSalida;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InsertCandidatoHandler implements HttpHandler {
    //static Logger logger = Logger.getLogger(InsertCandidatoHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             OutputStream os = exchange.getResponseBody()) {

            // Read request body
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String requestBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

            // Parse JSON
            JsonObject jsonRequest = new com.google.gson.JsonParser().parse(requestBody).getAsJsonObject();
            String id = jsonRequest.get("id").getAsString();
            String nombre = jsonRequest.get("nombre").getAsString();

            // Insert into database
            Functions.insertCandidato(id, nombre);

            // Send response
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "OK");
            jsonResponse.addProperty("message", "Candidato inserted successfully");
            String response = jsonResponse.toString();

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            os.write(response.getBytes(StandardCharsets.UTF_8));
            
            Comando c =new AltaCandidato(new Candidato(id, nombre));
            PlanificadorMensajesSalida.addMessage(c);
            
        } catch (Exception e) {
            //logger.error("Error inserting Candidato", e);
            exchange.sendResponseHeaders(500, 0); // Send error response
        } finally {
            exchange.close(); // Close the exchange explicitly
        }
    }
}