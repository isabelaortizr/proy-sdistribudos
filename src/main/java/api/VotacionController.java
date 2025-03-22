package api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import com.google.gson.Gson;
import java.util.UUID;
import java.util.List;
import com.google.gson.JsonObject;

import comandos.VotacionComando;
import comandos.ConfirmacionVotoComando;
import api.dto.VotoRequest;
import api.dto.VotanteRequest;
import api.dto.CandidatoRequest;
import db.DatabaseManager;
import api.dto.CandidatoResponse;
import planificador.PlanificadorEntrada;
import planificador.PlanificadorSalida;
import comandos.AltaCandidatoComando;
import model.Voto;

public class VotacionController {
    private static final int PUERTO_HTTP = 8080;
    private final HttpServer server;
    private final PlanificadorSalida planificadorSalida;
    private final DatabaseManager dbManager;
    private final Gson gson;

    public VotacionController(int puerto, PlanificadorSalida planificadorSalida) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(puerto), 0);
        this.planificadorSalida = planificadorSalida;
        this.dbManager = DatabaseManager.getInstance();
        this.gson = new Gson();
        configurarEndpoints();
    }

    private void configurarEndpoints() {
        // Endpoint para votar
        server.createContext("/api/votar", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    enviarRespuesta(exchange, 405, "Método no permitido");
                    return;
                }

                try {
                    // Leer el cuerpo de la petición
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    // Convertir JSON a objeto
                    VotoRequest request = gson.fromJson(body.toString(), VotoRequest.class);

                    // Validar el votante y candidato
                    if (!dbManager.esVotanteValido(request.getCodigoVotante())) {
                        throw new IllegalArgumentException("Votante inválido o ya ha votado");
                    }
                    if (!dbManager.existeCandidato(request.getCodigoCandidato())) {
                        throw new IllegalArgumentException("Candidato no existe");
                    }

                    // Crear el voto
                    String idVoto = UUID.randomUUID().toString();
                    long timestamp = System.currentTimeMillis();

                    // Crear objeto Voto
                    Voto voto = new Voto(
                        idVoto,
                        timestamp,
                        request.getCodigoVotante(),
                        request.getCodigoCandidato()
                    );

                    // Crear y procesar el comando de votación
                    VotacionComando comando = new VotacionComando(voto, request.getFirma());

                    // Enviar al planificador de salida para distribuir a otros nodos
                    planificadorSalida.addMessage(comando);

                    // Enviar respuesta exitosa
                    String respuesta = gson.toJson(new VotoResponse(idVoto));
                    enviarRespuesta(exchange, 200, respuesta);

                } catch (IllegalArgumentException e) {
                    // Error de validación
                    String respuesta = gson.toJson(new ErrorResponse("Error de validación: " + e.getMessage()));
                    enviarRespuesta(exchange, 400, respuesta);
                } catch (Exception e) {
                    // Error interno
                    String respuesta = gson.toJson(new ErrorResponse("Error interno: " + e.getMessage()));
                    enviarRespuesta(exchange, 500, respuesta);
                    e.printStackTrace();
                }
            }
        });

        // Endpoint para confirmar voto
        server.createContext("/api/confirmar", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    enviarRespuesta(exchange, 405, "Método no permitido");
                    return;
                }

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    ConfirmacionRequest confirmacion = gson.fromJson(body.toString(), ConfirmacionRequest.class);
                    String ipOrigen = exchange.getRemoteAddress().getAddress().getHostAddress();
                    
                    ConfirmacionVotoComando comando = new ConfirmacionVotoComando(
                        confirmacion.idVoto,
                        confirmacion.confirmado,
                        ipOrigen
                    );

                    // Enviar al planificador de salida para distribuir a otros nodos
                    planificadorSalida.addMessage(comando);

                    String respuesta = gson.toJson(new ConfirmacionResponse("Confirmación enviada"));
                    enviarRespuesta(exchange, 200, respuesta);

                } catch (Exception e) {
                    String respuesta = gson.toJson(new ErrorResponse("Error al procesar la confirmación: " + e.getMessage()));
                    enviarRespuesta(exchange, 400, respuesta);
                    e.printStackTrace();
                }
            }
        });

        // Endpoints para votantes
        server.createContext("/api/votantes", new VotantesHandler());
        server.createContext("/api/votantes/eliminar", new EliminarVotanteHandler());
        
        // Endpoints para candidatos
        server.createContext("/api/candidatos", new CandidatosHandler());
        server.createContext("/api/candidatos/eliminar", new EliminarCandidatoHandler());
        
        // Endpoint de estado (health check)
        server.createContext("/api/estado", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                enviarRespuesta(exchange, 405, "Método no permitido");
                return;
            }
            enviarRespuesta(exchange, 200, "{\"estado\":\"activo\"}");
        });
    }

    private void enviarRespuesta(HttpExchange exchange, int codigo, String respuesta) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(codigo, respuesta.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(respuesta.getBytes());
        }
    }

    public void iniciar() {
        server.start();
        System.out.println("Servidor HTTP iniciado en puerto " + PUERTO_HTTP);
    }

    public void detener() {
        server.stop(0);
        System.out.println("Servidor HTTP detenido");
    }

    private static class RespuestaVoto {
        private final boolean exito;
        private final String mensaje;

        public RespuestaVoto(boolean exito, String mensaje) {
            this.exito = exito;
            this.mensaje = mensaje;
        }
    }

    private class VotantesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Registrar nuevo votante
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    VotanteRequest request = gson.fromJson(body.toString(), VotanteRequest.class);
                    request.validar();

                    dbManager.registrarVotante(
                        request.getCodigo(),
                        request.getNombre(),
                        request.getDni()
                    );

                    enviarRespuesta(exchange, 200, "{\"mensaje\":\"Votante registrado correctamente\"}");
                } catch (Exception e) {
                    enviarRespuesta(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                // Listar votantes
                try {
                    List<String[]> votantes = dbManager.listarVotantes();
                    String json = gson.toJson(votantes);
                    enviarRespuesta(exchange, 200, json);
                } catch (Exception e) {
                    enviarRespuesta(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                enviarRespuesta(exchange, 405, "{\"error\":\"Método no permitido\"}");
            }
        }
    }

    private class EliminarVotanteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                enviarRespuesta(exchange, 405, "{\"error\":\"Método no permitido\"}");
                return;
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                String codigo = gson.fromJson(reader, JsonObject.class).get("codigo").getAsString();

                dbManager.eliminarVotante(codigo);
                enviarRespuesta(exchange, 200, "{\"mensaje\":\"Votante eliminado correctamente\"}");
            } catch (Exception e) {
                enviarRespuesta(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    private class CandidatosHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Leer el body del request
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    // Parsear el request
                    CandidatoRequest request = gson.fromJson(body.toString(), CandidatoRequest.class);
                    request.validar();

                    // Crear el comando
                    AltaCandidatoComando comando = new AltaCandidatoComando(request.getCodigo(), request.getNombre());
                    
                    // Enviar al planificador de salida para distribuir a otros nodos
                    planificadorSalida.addMessage(comando);
                    
                    enviarRespuesta(exchange, 200, "{\"mensaje\":\"Candidato registrado correctamente\"}");
                } catch (Exception e) {
                    enviarRespuesta(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    List<CandidatoResponse> candidatos = dbManager.obtenerCandidatos();
                    enviarRespuesta(exchange, 200, gson.toJson(candidatos));
                } catch (Exception e) {
                    enviarRespuesta(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Método no permitido
            }
        }
    }

    private class EliminarCandidatoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                enviarRespuesta(exchange, 405, "{\"error\":\"Método no permitido\"}");
                return;
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                String codigo = gson.fromJson(reader, JsonObject.class).get("codigo").getAsString();

                dbManager.eliminarCandidato(codigo);
                enviarRespuesta(exchange, 200, "{\"mensaje\":\"Candidato eliminado correctamente\"}");
            } catch (Exception e) {
                enviarRespuesta(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // Clases para manejar las peticiones y respuestas
    private static class VotoResponse {
        String idVoto;
        VotoResponse(String idVoto) {
            this.idVoto = idVoto;
        }
    }

    private static class ConfirmacionRequest {
        String idVoto;
        boolean confirmado;
    }

    private static class ConfirmacionResponse {
        String mensaje;
        ConfirmacionResponse(String mensaje) {
            this.mensaje = mensaje;
        }
    }

    private static class ErrorResponse {
        String error;
        ErrorResponse(String error) {
            this.error = error;
        }
    }
} 