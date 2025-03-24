package network;

import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import comandos.Comando;
import com.google.gson.Gson;
import db.DatabaseManager;
import planificador.PlanificadorEntrada;
import planificador.PlanificadorPresidenteMesa;
import model.SocketClient;

public class NodoManager {
    private static final int PUERTO_NODO = 1825;
    private static final String IP_NODO_PRINCIPAL = "192.168.137.1"; // Ajusta esta IP según corresponda
    private static final int TIEMPO_REINTENTO = 5000; // 5 segundos entre reintentos
    private static final int MAX_INTENTOS = 12; // Intentar durante 1 minuto (12 * 5 segundos)

    private final String ipLocal;
    private final PlanificadorEntrada planificadorEntrada;
    private final Map<String, SocketClient> nodosConectados;
    private final Gson gson;
    private final DatabaseManager dbManager;
    private final PlanificadorPresidenteMesa planificadorPresidenteMesa;
    private SocketClient socketPrincipal;
    private volatile boolean running;
    private volatile boolean reconectando;

    // Constructor para nodo secundario (sin planificadorPresidenteMesa)
    public NodoManager(String ipLocal, PlanificadorEntrada planificadorEntrada) {
        this(ipLocal, planificadorEntrada, null);
    }

    // Constructor principal (o secundario con planificadorPresidenteMesa)
    public NodoManager(String ipLocal, PlanificadorEntrada planificadorEntrada, PlanificadorPresidenteMesa planificadorPresidenteMesa) {
        this.ipLocal = ipLocal;
        this.planificadorEntrada = planificadorEntrada;
        this.planificadorPresidenteMesa = planificadorPresidenteMesa;
        this.nodosConectados = new ConcurrentHashMap<>();
        this.gson = new Gson();
        this.running = true;
        this.reconectando = false;
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Conecta al nodo principal (si se es nodo secundario).
     */
    public void conectarANodoPrincipal() throws IOException {
        if (reconectando) {
            return;
        }
        reconectando = true;

        new Thread(() -> {
            int intentos = 0;
            boolean conectado = false;
            while (!conectado && running && intentos < MAX_INTENTOS) {
                try {
                    intentos++;
                    System.out.println("Intento " + intentos + " de " + MAX_INTENTOS + " para conectar al nodo principal...");

                    socketPrincipal = new SocketClient();
                    socketPrincipal.connectToPrincipal(IP_NODO_PRINCIPAL, PUERTO_NODO, planificadorEntrada);
                    nodosConectados.put(IP_NODO_PRINCIPAL, socketPrincipal);
                    System.out.println("Conectado exitosamente al nodo principal: " + IP_NODO_PRINCIPAL);
                    conectado = true;
                } catch (IOException e) {
                    System.err.println("Error conectando al nodo principal (intento " + intentos + "): " + e.getMessage());
                    if (intentos < MAX_INTENTOS) {
                        try {
                            Thread.sleep(TIEMPO_REINTENTO);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            reconectando = false;
            if (!conectado) {
                System.err.println("No se pudo conectar al nodo principal después de " + MAX_INTENTOS + " intentos.");
                System.err.println("El sistema continuará funcionando en modo autónomo.");
            }
        }).start();
    }

    /**
     * Agrega nodos a partir de una cadena de IPs separadas por ";"
     * Se excluye la IP local y, si se es nodo principal, no se conectan nodos.
     */
    public void agregarNodo(String listaIps) {
        if (listaIps == null || listaIps.trim().isEmpty()) {
            return;
        }

        Set<String> ipsUnicas = new HashSet<>();
        for (String ip : listaIps.split(";")) {
            String ipLimpia = ip.trim();
            if (!ipLimpia.isEmpty()) {
                ipsUnicas.add(ipLimpia);
            }
        }
        System.out.println("Lista de IPs únicas recibidas: " + ipsUnicas);

        for (String ip : ipsUnicas) {
            // Excluir IP local
            if (ip.equalsIgnoreCase(ipLocal.trim())) {
                System.out.println("La IP " + ip + " es la IP local, no se conecta.");
                continue;
            }
            // Si este nodo es el principal, no conectamos a otros
            if (esNodoPrincipal()) {
                System.out.println("Soy el nodo principal, no conecto a " + ip);
                continue;
            }
            if (nodosConectados.containsKey(ip)) {
                System.out.println("La IP " + ip + " ya está conectada.");
                continue;
            }
            conectarANodoSecundario(ip);
        }
    }

    /**
     * Conecta a un nodo secundario dado su IP.
     */
    private void conectarANodoSecundario(String ipNodo) {
        if (ipNodo == null || ipNodo.isEmpty() || ipNodo.equalsIgnoreCase(ipLocal)) {
            return;
        }

        // Si ya existe conexión, la reemplazamos.
        if(nodosConectados.containsKey(ipNodo)){
            System.out.println("Ya existe conexión para IP " + ipNodo + ", se reemplazará la conexión.");
            SocketClient oldClient = nodosConectados.get(ipNodo);
            oldClient.close();
            nodosConectados.remove(ipNodo);
        }

        try {
            System.out.println("Intentando conectar a nodo: " + ipNodo);
            Socket socketSecundario = new Socket();
            socketSecundario.connect(new java.net.InetSocketAddress(ipNodo, PUERTO_NODO), 3000);
            SocketClient client = new SocketClient(socketSecundario);
            nodosConectados.put(ipNodo, client);
            if (planificadorPresidenteMesa != null) {
                planificadorPresidenteMesa.getPlanificadorSalida().agregarNodo(ipNodo);
            }
            new Thread(() -> manejarNodoSecundario(socketSecundario, ipNodo)).start();
            System.out.println("Conectado exitosamente a nodo: " + ipNodo);
        } catch (Exception e) {
            System.err.println("Error al conectar con nodo " + ipNodo + ": " + e.getMessage());
            programarReintento(ipNodo);
        }
    }

    /**
     * Programa un reintento para conectar a un nodo dado.
     */
    private void programarReintento(String ipNodo) {
        new Thread(() -> {
            try {
                Thread.sleep(TIEMPO_REINTENTO);
                if (running && !nodosConectados.containsKey(ipNodo)) {
                    System.out.println("Reintentando conexión con nodo: " + ipNodo);
                    conectarANodoSecundario(ipNodo);
                } else {
                    System.out.println("No se reintentará conexión con nodo " + ipNodo + " porque ya está conectado.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Maneja la comunicación con un nodo secundario.
     */
    private void manejarNodoSecundario(Socket socket, String ipNodo) {
        try (BufferedReader readerNodo = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String mensaje;
            while (running && (mensaje = readerNodo.readLine()) != null) {
                procesarMensaje(mensaje);
            }
        } catch (IOException e) {
            if (running) {
                System.out.println("Conexión perdida con nodo " + ipNodo + ". Se intentará reconectar.");
                nodosConectados.remove(ipNodo);
                if (planificadorPresidenteMesa != null) {
                    planificadorPresidenteMesa.getPlanificadorSalida().eliminarNodo(ipNodo);
                }
                programarReintento(ipNodo);
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar socket con nodo " + ipNodo + ": " + e.getMessage());
            }
        }
    }

    /**
     * Procesa un mensaje recibido y lo envía al PlanificadorEntrada.
     */
    private void procesarMensaje(String mensaje) {
        try {
            System.out.println("Procesando mensaje: " + mensaje);
            planificadorEntrada.agregarMensaje(mensaje);
            // Si es un mensaje de lista de nodos (código 0001), se procesa la lista.
            if (mensaje.startsWith("0001|")) {
                String[] partes = mensaje.split("\\|");
                if (partes.length == 2) {
                    System.out.println("Recibida lista de nodos: " + partes[1]);
                    agregarNodo(partes[1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados.
     */
    public void enviarMensaje(String mensaje) {
        synchronized (nodosConectados) {
            for (SocketClient cliente : nodosConectados.values()) {
                try {
                    cliente.send(mensaje);
                } catch (Exception e) {
                    System.err.println("Error enviando mensaje a " + cliente.getRemoteAddress() + ": " + e.getMessage());
                    cliente.close();
                }
            }
        }
    }

    /**
     * Retorna una lista con las IPs de los nodos conectados.
     */
    public List<String> getNodosConectados() {
        return new ArrayList<>(nodosConectados.keySet());
    }

    public String getIpLocal() {
        return ipLocal;
    }

    /**
     * Indica si este nodo es el principal.
     */
    public boolean esNodoPrincipal() {
        return ipLocal.equalsIgnoreCase(IP_NODO_PRINCIPAL);
    }

    /**
     * Cierra todas las conexiones y detiene el NodoManager.
     */
    public void cerrar() {
        running = false;
        if (socketPrincipal != null) {
            socketPrincipal.close();
        }
        synchronized (nodosConectados) {
            for (SocketClient cliente : nodosConectados.values()) {
                cliente.close();
            }
            nodosConectados.clear();
        }
    }

    // Métodos para enviar voto o procesar mensajes de otros tipos se pueden agregar según sea necesario.

    // Clases internas para datos de mensajes (opcional)
    private static class MessageWrapper {
        String tipo;
        String datos;
    }

    private static class VotoData {
        String codigoVotante;
        String codigoCandidato;
        String firma;
        VotoData(String codigoVotante, String codigoCandidato, String firma) {
            this.codigoVotante = codigoVotante;
            this.codigoCandidato = codigoCandidato;
            this.firma = firma;
        }
    }

    private static class VotanteData {
        String codigo;
        String nombre;
        String dni;
    }

    private static class CandidatoData {
        String codigo;
        String nombre;
        String partido;
    }

    private static class ComandoData {
        String tipo;
        String datos;
    }

    private static class ConfirmacionData {
        String idVoto;
        boolean confirmado;
    }
}
