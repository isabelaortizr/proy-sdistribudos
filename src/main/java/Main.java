package org.example;

import model.Server;
import model.SocketClient;
import planificador.PlanificadorEntrada;
import planificador.PlanificadorSalida;

/**
 * Clase principal para iniciar el sistema.
 */
public class Main {
    public static void main(String[] args) {

        boolean esPrincipal = true;
        if (args.length > 0 && args[0].equalsIgnoreCase("principal")) {
            esPrincipal = true;
        }


        PlanificadorEntrada planificadorEntrada = new PlanificadorEntrada();

        if (esPrincipal) {

            Server server = new Server(1825, planificadorEntrada);
            server.start();
            System.out.println("Nodo principal escuchando en el puerto 1825.");
        } else {
            System.out.println("Nodo cliente iniciado (no principal).");

            SocketClient socketClient = new SocketClient();

            socketClient.connectToPrincipal("172.16.61.14", 1825, planificadorEntrada);
        }


        String[] nodos = {"172.16.61.14"};
        PlanificadorSalida planificadorSalida = new PlanificadorSalida(nodos);


    }
}
