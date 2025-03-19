package httpserver;

import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.crypto.trep.DataBase.Functions;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class GetAllCandidatosHandler implements HttpHandler {
    static Logger logger = Logger.getLogger(GetAllCandidatosHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonArray candidatos = Functions.getAllCandidatos();

            String response = candidatos.toString();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}