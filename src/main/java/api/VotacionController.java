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
import java.util.logging.Logger;
import java.util.logging.Level;

import comandos.*;
import api.dto.CandidatoRequest;
import api.dto.CandidatoResponse;
import api.dto.VotoRequest;
import db.DatabaseManager;
import planificador.PlanificadorPresidenteMesa;
import planificador.PlanificadorSalida;
import model.Voto;
import planificador.PlanificadorTransaccion;

public class VotacionController {
    private static final Logger LOGGER = Logger.getLogger(VotacionController.class.getName());
    private final HttpServer server;
    private final PlanificadorSalida planificadorSalida;
    private final DatabaseManager dbManager;
    private final Gson gson;
    private volatile boolean ejecutando = true;

    public VotacionController(int puerto, PlanificadorSalida planificadorSalida) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(puerto), 0);
        this.planificadorSalida = planificadorSalida;
        this.dbManager = DatabaseManager.getInstance();
        this.gson = new Gson();
        
        // Configurar rutas
        server.createContext("/api/candidatos", new CandidatosHandler());
        server.createContext("/api/votantes", new VotantesHandler());
        server.createContext("/api/votar", new VotacionHandler());
        
        server.setExecutor(null);
    }

    public void iniciar() {
        if (ejecutando) {
            server.start();
            LOGGER.info("Servidor HTTP iniciado en puerto " + server.getAddress().getPort());
        }
    }

    public void detener() {
        ejecutando = false;
        server.stop(0);
        LOGGER.info("Servidor HTTP detenido");
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

                    // Crear el comando 0003 para alta de candidato
                    AltaCandidatoComando comando = new AltaCandidatoComando(request.getCodigo(), request.getNombre());
                    
                    // Enviar comando a través del planificador de salida
                    planificadorSalida.addMessage(comando);
                    
                    // Preparar respuesta JSON
                    String respuesta = String.format(
                        "{\"codigo\": \"%s\", \"mensaje\": \"Candidato registrado correctamente\"}", 
                        request.getCodigo()
                    );
                    
                    enviarRespuesta(exchange, 200, respuesta);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al registrar candidato", e);
                    enviarRespuesta(exchange, 500, "Error interno del servidor");
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    List<CandidatoResponse> candidatos = dbManager.obtenerCandidatos();
                    enviarRespuesta(exchange, 200, gson.toJson(candidatos));
                } catch (Exception e) {
                    enviarRespuesta(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                enviarRespuesta(exchange, 405, "Método no permitido");
            }
        }
    }

    private class VotantesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    // Generar código aleatorio para el votante
                    String codigo = UUID.randomUUID().toString().substring(0, 8);
                    String llavePrivada = UUID.randomUUID().toString();
                    
                    // Crear comando 0006 para alta de votante
                    AltaVotanteComando comando = new AltaVotanteComando(codigo, llavePrivada);
                    
                    // Enviar comando a través del planificador de salida
                    planificadorSalida.addMessage(comando);
                    
                    // Preparar respuesta JSON
                    String respuesta = String.format(
                        "{\"codigo\": \"%s\", \"llavePrivada\": \"%s\"}", 
                        codigo, 
                        llavePrivada
                    );
                    
                    enviarRespuesta(exchange, 200, respuesta);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error al registrar votante", e);
                    enviarRespuesta(exchange, 500, "Error interno del servidor");
                }
            } else if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    List<String[]> votantes = dbManager.listarVotantes();
                    enviarRespuesta(exchange, 200, gson.toJson(votantes));
                } catch (Exception e) {
                    enviarRespuesta(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else {
                enviarRespuesta(exchange, 405, "Método no permitido");
            }
        }
    }

    private class VotacionHandler implements HttpHandler {
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


                VotoRequest request = gson.fromJson(body.toString(), VotoRequest.class);
                request.setFirma("Firma_123");
                request.validar();

                Voto voto = new Voto(
                        request.getIdVoto(),
                        System.currentTimeMillis(),
                        request.getCodigoVotante(),
                        request.getCodigoCandidato(),
                        "" // refAnteriorBloque vacío
                );

                // Se crea el comando 0009 a partir del voto y la firma del request
                VotacionComando comando = new VotacionComando(voto, request.getFirma());

                // Registrar el voto en los planificadores locales para confirmaciones y sincronización
                PlanificadorTransaccion.addVoto(comando);
                PlanificadorPresidenteMesa.addVoto(comando);

                // Enviar el comando 0009 a través del planificador de salida
                planificadorSalida.addMessage(comando);

                enviarRespuesta(exchange, 200, "Voto registrado correctamente");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al procesar voto", e);
                enviarRespuesta(exchange, 500, "Error interno del servidor");
            }
        }
    }



    private void enviarRespuesta(HttpExchange exchange, int codigo, String mensaje) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(codigo, mensaje.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(mensaje.getBytes());
        }
    }
} 