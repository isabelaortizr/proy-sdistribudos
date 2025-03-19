package httpserver;

import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.crypto.trep.DataBase.Functions;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

public class GetAllVotantesHandler implements HttpHandler {
    static Logger logger = Logger.getLogger(GetAllVotantesHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            JsonArray votantes = Functions.getAllVotantes();

            String response = votantes.toString();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}