package planificador;

import model.SocketClient;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Planificador de Mensajes de Salida.
 * Cuando se necesita difundir un comando a otros nodos, se usa este planificador.
 */
public class PlanificadorSalida {
    private BlockingQueue<String> colaMensajes;
    private boolean running = true;
    private SocketClient socketClient;
    private String[] nodos; // Lista de IPs a las que enviar
    private int port = 1825; // Puerto al que enviar por defecto

    public PlanificadorSalida(String[] nodos) {
        this.nodos = nodos;
        this.colaMensajes = new LinkedBlockingQueue<>();
        this.socketClient = new SocketClient();
        iniciarProcesamiento();
    }

    /**
     * Encola un comando para su envío.
     */
    public void enviarComando(String comando) {
        try {
            colaMensajes.put(comando);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia el hilo que toma comandos de la cola y los envía a todos los nodos.
     */
    private void iniciarProcesamiento() {
        new Thread(() -> {
            while (running) {
                try {
                    String comando = colaMensajes.take();
                    difundirComando(comando);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Envía el comando a todos los nodos configurados.
     */
    private void difundirComando(String comando) {
        for (String nodo : nodos) {
            System.out.println("Enviando comando a " + nodo + ":" + port + " => " + comando);
            socketClient.sendMessage(nodo, port, comando);
        }
    }
}
