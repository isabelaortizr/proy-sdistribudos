package model;

import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import planificador.PlanificadorEntrada;

public class SocketClient {
    private static final int TIMEOUT = 3000; // 3 segundos de timeout
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private PlanificadorEntrada eventListener;
    private volatile boolean running;
    private Thread readThread;
    private String remoteAddress;

    public SocketClient() {
    }

    public SocketClient(Socket socket) throws IOException {
        this.socket = socket;
        this.remoteAddress = socket.getInetAddress().getHostAddress();
        initializeStreams();
    }

    public void connectToPrincipal(String host, int port, PlanificadorEntrada planificadorEntrada) throws IOException {
        this.socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), TIMEOUT);
        this.remoteAddress = socket.getInetAddress().getHostAddress();
        this.eventListener = planificadorEntrada;
        initializeStreams();
        startReading();
    }

    private void initializeStreams() throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void startReading() {
        running = true;
        readThread = new Thread(() -> {
            try {
                String message;
                while (running && (message = in.readLine()) != null) {
                    if (eventListener != null) {
                        eventListener.agregarMensaje(message);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error leyendo del socket: " + e.getMessage());
                }
            } finally {
                close();
            }
        });
        readThread.start();
    }

    public void send(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        }
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void close() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (readThread != null) readThread.interrupt();
        } catch (IOException e) {
            System.err.println("Error cerrando socket: " + e.getMessage());
        }
    }

    /*public static void main(String[] args) {
        String host = "localhost";
        int port = 1825;

        for (int i = 0; i < 6; i++) {
            try {
                new Socket(host, port);
                System.out.println("Cliente " + i + " conectado al servidor.");
            } catch (IOException e) {
                System.err.println("Cliente " + i + " no pudo conectar: " + e.getMessage());
            }
        }
    }*/
}
