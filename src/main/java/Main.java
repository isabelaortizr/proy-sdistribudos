import api.VotacionController;
import planificador.PlanificadorTransaccion;
import planificador.PlanificadorSalida;
import planificador.PlanificadorEntrada;
import planificador.PlanificadorPresidenteMesa;
import network.NodoManager;
import model.Server;
import model.SocketClient;
import comandos.ListaNodosComando;
import db.DatabaseManager;

public class Main {
    private static final int PUERTO_NODO = 1825;
    private static final int PUERTO_HTTP = 8080;
    private static final String IP_NODO_PRINCIPAL = "172.16.40.30";

    private static class ServerHolder {
        private static Server instance;
    }

    public static void main(String[] args) {
        try {
            // Configuración inicial
            boolean esPrincipal = args.length > 0 && args[0].equalsIgnoreCase("principal");
            String ipNodo = esPrincipal ? IP_NODO_PRINCIPAL : "127.0.0.1";
            System.out.println("Iniciando sistema de votación...");
            System.out.println("Modo: " + (esPrincipal ? "PRINCIPAL" : "SECUNDARIO"));
            System.out.println("IP Local: " + ipNodo);

            // Inicializar la base de datos
            DatabaseManager dbManager = DatabaseManager.getInstance();
            System.out.println("Base de datos inicializada");

            // Iniciar planificadores
            System.out.println("Iniciando planificadores...");
            
            // Crear PlanificadorPresidenteMesa
            String[] nodosDestino = esPrincipal ? new String[]{} : new String[]{IP_NODO_PRINCIPAL};
            PlanificadorPresidenteMesa planificadorPresidenteMesa = new PlanificadorPresidenteMesa(nodosDestino);
            Thread threadPresidenteMesa = new Thread(planificadorPresidenteMesa);
            threadPresidenteMesa.start();
            System.out.println("PlanificadorPresidenteMesa iniciado");

            // Configurar ListaNodosComando con el PlanificadorSalida
            ListaNodosComando.setPlanificadorSalida(planificadorPresidenteMesa.getPlanificadorSalida());

            // Crear e iniciar el PlanificadorTransaccion
            PlanificadorTransaccion planificadorTransaccion = new PlanificadorTransaccion();
            Thread threadPlanificadorTransaccion = new Thread(planificadorTransaccion);
            threadPlanificadorTransaccion.start();
            System.out.println("PlanificadorTransaccion iniciado");

            // Crear e iniciar el PlanificadorEntrada
            PlanificadorEntrada planificadorEntrada = new PlanificadorEntrada(planificadorPresidenteMesa);
            planificadorEntrada.start();
            System.out.println("PlanificadorEntrada iniciado");

            // Inicializar el servidor de sockets
            ServerHolder.instance = new Server(PUERTO_NODO, planificadorEntrada);
            ServerHolder.instance.start();
            System.out.println("Servidor de sockets iniciado en puerto " + PUERTO_NODO);

            // Inicializar el NodoManager
            NodoManager nodoManager = new NodoManager(ipNodo, planificadorEntrada, planificadorPresidenteMesa);
            if (!esPrincipal) {
                System.out.println("Intentando conectar al nodo principal...");
                try {
                    nodoManager.conectarANodoPrincipal();
                } catch (Exception e) {
                    System.err.println("No se pudo establecer la conexión inicial con el nodo principal. " +
                                     "El sistema continuará intentando conectarse en segundo plano.");
                }
            }
            System.out.println("NodoManager inicializado");

            // Iniciar el servidor HTTP
            VotacionController votacionController = new VotacionController(PUERTO_HTTP, planificadorPresidenteMesa.getPlanificadorSalida());
            votacionController.iniciar();
            System.out.println("Servidor HTTP iniciado en puerto " + PUERTO_HTTP);

            // Configurar el shutdown hook para limpieza de recursos
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Deteniendo servicios...");
                planificadorPresidenteMesa.detener();
                planificadorTransaccion.detener();
                planificadorEntrada.detener();
                if (ServerHolder.instance != null) {
                    ServerHolder.instance.stopServer();
                }
                votacionController.detener();
                nodoManager.cerrar();
                System.out.println("Servicios detenidos");
            }));

            System.out.println("Sistema de votación iniciado completamente");

            // Mantener el programa en ejecución
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error crítico iniciando el sistema: " + e.getMessage());
            e.printStackTrace();
            if (ServerHolder.instance != null) {
                ServerHolder.instance.stopServer();
            }
            System.exit(1);
        }
    }
} 