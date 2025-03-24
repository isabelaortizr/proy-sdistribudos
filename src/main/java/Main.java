import api.VotacionController;
import planificador.PlanificadorEntrada;
import planificador.PlanificadorPresidenteMesa;
import network.NodoManager;
import model.Server;
import db.DatabaseManager;

public class Main {
    private static final int PUERTO_NODO = 1825;
    private static final int PUERTO_HTTP = 8080;
    private static final String IP_NODO_PRINCIPAL = "192.168.0.10";

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

            // Definir nodosDestino: si es principal, no hay nodos a conectar; si es secundario, se conecta al principal.
            String[] nodosDestino = esPrincipal ? new String[]{} : new String[]{IP_NODO_PRINCIPAL};

            // Crear PlanificadorPresidenteMesa (única instancia) y arrancarlo en un hilo
            PlanificadorPresidenteMesa planificadorPresidenteMesa = new PlanificadorPresidenteMesa(nodosDestino);
            Thread threadPresidente = new Thread(planificadorPresidenteMesa);
            threadPresidente.start();
            System.out.println("PlanificadorPresidenteMesa iniciado");

            // Crear e iniciar el PlanificadorEntrada, usando el mismo planificadorPresidenteMesa
            PlanificadorEntrada planificadorEntrada = new PlanificadorEntrada(planificadorPresidenteMesa);
            planificadorEntrada.start();
            System.out.println("PlanificadorEntrada iniciado");

            // Inicializar el servidor de sockets
            ServerHolder.instance = new Server(PUERTO_NODO, planificadorEntrada);
            ServerHolder.instance.start();
            System.out.println("Servidor de sockets iniciado en puerto " + PUERTO_NODO);

            // Inicializar el NodoManager (usando la misma instancia de PlanificadorPresidenteMesa)
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

            // Inicializar y arrancar el controlador HTTP
            VotacionController votacionController = new VotacionController(PUERTO_HTTP, planificadorPresidenteMesa.getPlanificadorSalida());
            votacionController.iniciar();

            // Registrar shutdown hook para limpieza
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Deteniendo componentes...");
                planificadorEntrada.detener();
                planificadorPresidenteMesa.detener();
                ServerHolder.instance.stopServer();
                votacionController.detener();
                nodoManager.cerrar();
            }));

            System.out.println("Sistema de votación iniciado:");
            System.out.println("- API HTTP en puerto " + PUERTO_HTTP);
            System.out.println("- Servidor de nodos en puerto " + PUERTO_NODO);

            // Mantener el programa en ejecución
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error iniciando el sistema: " + e.getMessage());
            e.printStackTrace();
            if (ServerHolder.instance != null) {
                ServerHolder.instance.stopServer();
            }
            System.exit(1);
        }
    }
}
