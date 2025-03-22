package planificador;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import model.SocketClient;
import comandos.*;

/**
 * Planificador de Mensajes de Entrada.
 * Recibe los comandos que llegan por socket y los procesa en un hilo.
 */
public class PlanificadorEntrada extends Thread {
    private static final Logger log = Logger.getLogger(PlanificadorEntrada.class.getName());
    private final BlockingQueue<String> colaComandos;
    private final PlanificadorPresidenteMesa planificador;
    private volatile boolean running;

    public PlanificadorEntrada(PlanificadorPresidenteMesa planificador) {
        this.colaComandos = new LinkedBlockingQueue<>();
        this.planificador = planificador;
        this.running = true;
    }

    @Override
    public void run() {
        log.info("PlanificadorEntrada iniciado");
        while (running) {
            try {
                String comando = colaComandos.poll(100, TimeUnit.MILLISECONDS);
                if (comando != null) {
                    procesarComando(comando);
                }
            } catch (InterruptedException e) {
                if (running) {
                    log.severe("Error en PlanificadorEntrada: " + e.getMessage());
                }
            }
        }
        log.info("PlanificadorEntrada detenido");
    }

    /**
     * Método para recibir un comando desde el socket y encolarlo.
     */
    public void recibirComando(String mensaje) {
        try {
            colaComandos.put(mensaje);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.severe("Error encolando comando: " + e.getMessage());
        }
    }

    /**
     * Procesa el comando identificando su tipo y ejecutando la acción correspondiente.
     */
    private void procesarComando(String mensaje) {
        try {
            log.info("Procesando comando: " + mensaje);
            String[] partes = mensaje.split("\\|");
            if (partes.length < 2) {
                log.warning("Formato de comando inválido: " + mensaje);
                return;
            }

            String tipoComando = partes[0];
            switch (tipoComando) {
                case "0001":
                    procesarComandoListaNodos(partes[1]);
                    break;
                case "0009":
                    procesarComandoVotacion(mensaje);
                    break;
                case "0010":
                    procesarComandoConfirmacion(mensaje);
                    break;
                case "0011":
                    procesarComandoSincronizacion(mensaje);
                    break;
                default:
                    log.warning("Tipo de comando desconocido: " + tipoComando);
            }
        } catch (Exception e) {
            log.severe("Error procesando comando: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void procesarComandoListaNodos(String listaIps) {
        try {
            log.info("Procesando lista de nodos: " + listaIps);
            String[] ips = listaIps.split(";");
            
            for (String ip : ips) {
                ip = ip.trim();
                log.info("IP A CONECTAR: " + ip);
                
                if (ip.equals("127.0.0.1") || ip.equals("localhost") || isMyIP(ip)) {
                    continue;
                }

                try {
                    Socket socket = new Socket(ip, 1825);
                    SocketClient client = new SocketClient(socket);
                    client.startReading();
                    planificador.getPlanificadorSalida().agregarNodo(ip);
                    log.info("Conectado al nodo: " + ip);
                } catch (Exception e) {
                    log.warning("Error conectando a nodo " + ip + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.severe("Error procesando lista de nodos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void procesarComandoVotacion(String mensaje) {
        try {
            Comando09 comando = Comando09.parsear(mensaje);
            if (comando != null) {
                PlanificadorPresidenteMesa.addVoto(new VotacionComando(comando.getVoto(), comando.getFirma()));
                log.info("Voto procesado: " + comando.getVoto().getId());
            }
        } catch (Exception e) {
            log.severe("Error procesando comando de votación: " + e.getMessage());
        }
    }

    private void procesarComandoConfirmacion(String mensaje) {
        try {
            Comando10 comando = Comando10.parsear(mensaje);
            if (comando != null) {
                PlanificadorPresidenteMesa.confirmarVoto(
                    new ConfirmacionVotoComando(comando.getIdVoto(), comando.isConfirmado(), comando.getIpOrigen())
                );
                log.info("Confirmación procesada para voto: " + comando.getIdVoto());
            }
        } catch (Exception e) {
            log.severe("Error procesando comando de confirmación: " + e.getMessage());
        }
    }

    private void procesarComandoSincronizacion(String mensaje) {
        try {
            Comando11 comando = Comando11.parsear(mensaje);
            if (comando != null) {
                planificador.getPlanificadorSalida().addMessage(comando);
                log.info("Sincronización procesada para bloque: " + comando.getIdBloque());
            }
        } catch (Exception e) {
            log.severe("Error procesando comando de sincronización: " + e.getMessage());
        }
    }

    private boolean isMyIP(String ip) {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.getHostAddress().equals(ip)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Error verificando IP: " + e.getMessage());
        }
        return false;
    }

    public void detener() {
        running = false;
    }
}
