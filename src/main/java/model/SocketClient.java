package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    private static final String IP_NODO_PRINCIPAL = "172.16.40.233"; // IP del Nodo Principal
    private static final int PUERTO = 1825;

    public static void main(String[] args) {
        try (Socket socket = new Socket(IP_NODO_PRINCIPAL, PUERTO);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            salida.println("0001"); // Enviar el comando para obtener la lista de nodos

            String respuesta = entrada.readLine(); // Recibir la respuesta
            System.out.println("📥 Lista de Nodos Recibida: " + respuesta);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
