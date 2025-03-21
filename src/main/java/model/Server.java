package model;

import planificador.PlanificadorEntrada;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase que actúa como servidor (nodo principal),
 * recibiendo conexiones entrantes de otros nodos.
 */
public class Server {
    private int port;
    private PlanificadorEntrada planificadorEntrada;
    private boolean running = true;

    public static final int MAX_CONNECTIONS = 5;
    private int currentConnections = 0; // Contador de conexiones actuales


    // Conjunto para almacenar las IPs de los clientes conectados
    private Set<String> connectedIPs = ConcurrentHashMap.newKeySet();

    public Server(int port, PlanificadorEntrada planificadorEntrada) {
        this.port = port;
        this.planificadorEntrada = planificadorEntrada;
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Servidor escuchando en puerto " + port);
                while (running) {
                    System.out.println("Entra al bloque");
                    Socket clientSocket = serverSocket.accept();

                    if (currentConnections >= MAX_CONNECTIONS) {
                        System.out.println("entra al if");
                        System.out.println("Máximo de conexiones alcanzado. Rechazando nueva conexión.");
                        clientSocket.close();
                        continue;
                    }

                    // Incrementa el contador de conexiones
                    currentConnections++;

                    // Muestra el número de conexiones tras la actualización
                    System.out.println("Conexiones activas después de la aceptación: " + currentConnections);

                    // Se crea un hilo para manejar cada conexión entrante.
                    new Thread(() -> handleClient(clientSocket)).start();

                    if (currentConnections >= MAX_CONNECTIONS) {
                        System.out.println("Se alcanzó el máximo de " + MAX_CONNECTIONS + " conexiones.");
                        stopServer();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void handleClient(Socket clientSocket) {
        String clientIP = clientSocket.getInetAddress().getHostAddress();
        // Agregar la IP del cliente a la lista de conectados
        connectedIPs.add(clientIP);
        System.out.println("Cliente conectado: " + clientIP);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Genera la lista real de IPs conectadas
            String listaIPs = String.join(",", connectedIPs);
            String comando = "0001|" + listaIPs;
            out.println(comando);
            System.out.println("Enviado comando 0001: " + comando);

            // Se queda escuchando mensajes del cliente.
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Mensaje recibido del cliente " + clientIP + ": " + line);
                planificadorEntrada.recibirComando(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Al finalizar la conexión, remueve la IP del cliente
            connectedIPs.remove(clientIP);
            System.out.println("Cliente desconectado: " + clientIP);
        }
    }

    public void stopServer() {
        running = false;
        System.out.println("Servidor detenido.");
    }
}
