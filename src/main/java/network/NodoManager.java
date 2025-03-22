package network;

import java.net.Socket;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import comandos.Comando;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import db.DatabaseManager;
import planificador.PlanificadorEntrada;
import planificador.PlanificadorPresidenteMesa;
import model.SocketClient;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NodoManager {
    private static final int PUERTO_NODO = 1825;
    private static final String IP_NODO_PRINCIPAL = "172.16.61.17";
    private static final int TIEMPO_REINTENTO = 5000; // 5 segundos entre reintentos
    private static final int MAX_INTENTOS = 12; // Intentar durante 1 minuto (12 * 5 segundos)
    
    private final String ipLocal;
    private final PlanificadorEntrada planificadorEntrada;
    private final Set<String> nodosConectados;
    private final Gson gson;
    private final DatabaseManager dbManager;
    private final PlanificadorPresidenteMesa planificadorPresidenteMesa;
    private SocketClient socketPrincipal;
    private PrintWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private volatile boolean running;
    private volatile boolean reconectando;

    public NodoManager(String ipLocal, PlanificadorEntrada planificadorEntrada) {
        this.ipLocal = ipLocal;
        this.planificadorEntrada = planificadorEntrada;
        this.nodosConectados = Collections.synchronizedSet(new HashSet<>());
        this.gson = new Gson();
        this.running = true;
        this.reconectando = false;
        this.dbManager = DatabaseManager.getInstance();
        this.planificadorPresidenteMesa = null;
    }

    public NodoManager(String ipLocal, PlanificadorEntrada planificadorEntrada, PlanificadorPresidenteMesa planificadorPresidenteMesa) {
        this.ipLocal = ipLocal;
        this.planificadorEntrada = planificadorEntrada;
        this.planificadorPresidenteMesa = planificadorPresidenteMesa;
        this.nodosConectados = Collections.synchronizedSet(new HashSet<>());
        this.gson = new Gson();
        this.running = true;
        this.reconectando = false;
        this.dbManager = DatabaseManager.getInstance();
    }

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
                    nodosConectados.add(IP_NODO_PRINCIPAL);
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

    public void agregarNodo(String listaIps) {
        if (listaIps == null || listaIps.trim().isEmpty()) {
            return;
        }

        // Dividir la lista de IPs y limpiar cada una
        Set<String> ipsUnicas = new HashSet<>();
        for (String ip : listaIps.split(";")) {
            String ipLimpia = ip.trim();
            if (!ipLimpia.isEmpty()) {
                ipsUnicas.add(ipLimpia);
            }
        }

        System.out.println("Lista de IPs únicas recibidas: " + ipsUnicas);

        // Conectar a cada IP única que no sea la local ni esté ya conectada
        for (String ip : ipsUnicas) {
            if (!ip.equals(ipLocal) && !nodosConectados.contains(ip)) {
                conectarANodoSecundario(ip);
            }
        }
    }

    public void eliminarNodo(String ip) {
        if (nodosConectados.remove(ip)) {
            if (planificadorPresidenteMesa != null) {
                planificadorPresidenteMesa.getPlanificadorSalida().eliminarNodo(ip);
            }
            System.out.println("Nodo eliminado: " + ip);
        }
    }

    public List<String> getNodosConectados() {
        return new ArrayList<>(nodosConectados);
    }

    public String getIpLocal() {
        return ipLocal;
    }

    public boolean esNodoPrincipal() {
        return ipLocal.equals(IP_NODO_PRINCIPAL);
    }

    public void cerrar() {
        running = false;
        if (socketPrincipal != null) {
            socketPrincipal.close();
        }
        for (String ip : new ArrayList<>(nodosConectados)) {
            eliminarNodo(ip);
        }
        nodosConectados.clear();
    }

    private void procesarMensaje(String mensaje) {
        try {
            System.out.println("Procesando mensaje: " + mensaje);
            
            // Primero enviamos el mensaje al planificador
            planificadorEntrada.recibirComando(mensaje);
            
            // Si es un mensaje de lista de nodos, intentamos conectarnos a los nuevos nodos
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

    private void conectarANodoSecundario(String ipNodo) {
        if (ipNodo == null || ipNodo.isEmpty() || ipNodo.equals(ipLocal)) {
            return;
        }

        try {
            System.out.println("Intentando conectar a nodo: " + ipNodo);
            Socket socketSecundario = new Socket();
            socketSecundario.connect(new java.net.InetSocketAddress(ipNodo, PUERTO_NODO), 3000);
            nodosConectados.add(ipNodo);
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

    private void programarReintento(String ipNodo) {
        new Thread(() -> {
            try {
                Thread.sleep(TIEMPO_REINTENTO);
                if (running && !nodosConectados.contains(ipNodo)) {
                    System.out.println("Reintentando conexión con nodo: " + ipNodo);
                    conectarANodoSecundario(ipNodo);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void manejarNodoSecundario(Socket socket, String ipNodo) {
        try {
            BufferedReader readerNodo = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    public void enviarMensaje(String mensaje) {
        if (writer != null) {
            writer.println(mensaje);
        }
    }

    public void enviarVoto(String codigoVotante, String codigoCandidato, String firma) {
        try {
            MessageWrapper mensaje = new MessageWrapper();
            mensaje.tipo = "VOTO";
            mensaje.datos = gson.toJson(new VotoData(codigoVotante, codigoCandidato, firma));
            writer.println(gson.toJson(mensaje));
        } catch (Exception e) {
            System.err.println("Error al enviar voto: " + e.getMessage());
        }
    }

    private void procesarVotoRecibido(String datosVoto) {
        try {
            VotoData voto = gson.fromJson(datosVoto, VotoData.class);
            // Aquí implementarías la lógica para procesar el voto recibido
            // Por ejemplo, crear un Comando09 y ejecutarlo
        } catch (Exception e) {
            System.err.println("Error al procesar voto recibido: " + e.getMessage());
        }
    }

    private void procesarConfirmacionRecibida(String datosConfirmacion) {
        try {
            ConfirmacionData confirmacion = gson.fromJson(datosConfirmacion, ConfirmacionData.class);
            // Aquí implementarías la lógica para procesar la confirmación
            // Por ejemplo, actualizar el estado del voto en la base de datos
        } catch (Exception e) {
            System.err.println("Error al procesar confirmación: " + e.getMessage());
        }
    }

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