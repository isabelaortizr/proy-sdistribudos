package model;

import planificador.PlanificadorEntrada;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {
    private final int puerto;
    private final PlanificadorEntrada planificadorEntrada;
    private ServerSocket serverSocket;
    private boolean running;
    private final ExecutorService pool;

    public Server(int puerto, PlanificadorEntrada planificadorEntrada) {
        this.puerto = puerto;
        this.planificadorEntrada = planificadorEntrada;
        this.pool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(puerto);
            running = true;
            System.out.println("Servidor iniciado en puerto " + puerto);

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    pool.execute(new ClientHandler(clientSocket, planificadorEntrada));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error aceptando conexión: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error iniciando servidor: " + e.getMessage());
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (Exception e) {
            System.err.println("Error cerrando servidor: " + e.getMessage());
            pool.shutdownNow();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final PlanificadorEntrada planificadorEntrada;

    public ClientHandler(Socket socket, PlanificadorEntrada planificadorEntrada) {
        this.clientSocket = socket;
        this.planificadorEntrada = planificadorEntrada;
    }

    @Override
    public void run() {
        try {
            SocketClient client = new SocketClient(clientSocket);
            client.startReading();
        } catch (Exception e) {
            System.err.println("Error manejando cliente: " + e.getMessage());
        }
    }
}
