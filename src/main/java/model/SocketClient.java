package model;

import planificador.PlanificadorEntrada;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Clase para enviar mensajes a otros nodos y conectarse al nodo principal.
 */
public class SocketClient {

    /**
     * Envía un mensaje (comando) a la IP y puerto especificados.
     */
    public void sendMessage(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Conecta al nodo principal y escucha sus mensajes.
     * Los mensajes recibidos se encolan en el planificador de entrada.
     */
    public void connectToPrincipal(String host, int port, PlanificadorEntrada planificadorEntrada) {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    System.out.println("Mensaje recibido del principal: " + mensaje);
                    planificadorEntrada.recibirComando(mensaje);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
