package planificador;

import java.util.concurrent.*;
import java.util.*;
import model.SocketClient;
import comandos.Comando;

public class PlanificadorSalida extends Thread {
    private final BlockingQueue<Comando> colaMensajes;
    private final Set<SocketClient> clientes;
    private volatile boolean running;

    public PlanificadorSalida(String[] nodosDestino) {
        this.colaMensajes = new LinkedBlockingQueue<>();
        this.clientes = Collections.synchronizedSet(new HashSet<>());
        this.running = true;
        
        // Conectar con los nodos iniciales
        for (String nodo : nodosDestino) {
            agregarNodo(nodo);
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Comando comando = colaMensajes.poll(100, TimeUnit.MILLISECONDS);
                if (comando != null) {
                    distribuirMensaje(comando);
                }
            } catch (InterruptedException e) {
                if (running) {
                    System.err.println("Error en PlanificadorSalida: " + e.getMessage());
                }
            }
        }
    }

    public void addMessage(Comando comando) {
        try {
            colaMensajes.put(comando);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error agregando mensaje a la cola", e);
        }
    }

    private void distribuirMensaje(Comando comando) {
        String mensaje = comando.getComando();
        synchronized (clientes) {
            Iterator<SocketClient> iterator = clientes.iterator();
            while (iterator.hasNext()) {
                SocketClient cliente = iterator.next();
                try {
                    cliente.send(mensaje);
                } catch (Exception e) {
                    System.err.println("Error enviando mensaje a " + cliente.getRemoteAddress() + ": " + e.getMessage());
                    iterator.remove();
                    cliente.close();
                }
            }
        }
    }

    public void agregarNodo(String ip) {
        try {
            SocketClient cliente = new SocketClient();
            cliente.connectToPrincipal(ip, 1825, null);
            clientes.add(cliente);
            System.out.println("Nodo agregado: " + ip);
        } catch (Exception e) {
            System.err.println("Error conectando con nodo " + ip + ": " + e.getMessage());
        }
    }

    public void eliminarNodo(String ip) {
        synchronized (clientes) {
            Iterator<SocketClient> iterator = clientes.iterator();
            while (iterator.hasNext()) {
                SocketClient cliente = iterator.next();
                if (cliente.getRemoteAddress().equals(ip)) {
                    iterator.remove();
                    cliente.close();
                    System.out.println("Nodo eliminado: " + ip);
                    break;
                }
            }
        }
    }

    public int getCantidadNodos() {
        return clientes.size();
    }

    public void detener() {
        running = false;
        synchronized (clientes) {
            for (SocketClient cliente : clientes) {
                cliente.close();
            }
            clientes.clear();
        }
        interrupt();
    }
}
