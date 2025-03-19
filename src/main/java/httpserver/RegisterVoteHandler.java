package httpserver;

import BD.Funciones;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Utils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RegisterVoteHandler implements HttpHandler {
    static Logger logger = Logger.getLogger(RegisterVoteHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            Scanner s = new Scanner(is).useDelimiter("\\A");
            String requestBody = s.hasNext() ? s.next() : "";

            JsonObject jsonRequest = com.google.gson.JsonParser.parseString(requestBody).getAsJsonObject();
            String codigoVotante = jsonRequest.get("codigo_votante").getAsString();
            String codigoCandidato = jsonRequest.get("codigo_candidato").getAsString();
            String firma = jsonRequest.get("firma").getAsString();

            // Verify firma using HMAC
            String llavePrivada = Funciones.getLlavePrivada(codigoVotante);
            String expectedFirma = Utils.calculateHMAC(codigoVotante + codigoCandidato, llavePrivada);

            if (!firma.equals(expectedFirma)) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("status", "NOK");
                errorResponse.addProperty("message", "Invalid firma");
                exchange.sendResponseHeaders(400, errorResponse.toString().length());
                OutputStream os = exchange.getResponseBody();
                os.write(errorResponse.toString().getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            // Insert vote into Bloque_x table
            String uniqueId = Utils.generateUniqueKey();
            Funciones.insertBloqueData(uniqueId, codigoVotante, codigoCandidato);

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("status", "OK");
            jsonResponse.addProperty("unique_id", uniqueId);

            String response = jsonResponse.toString();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
}