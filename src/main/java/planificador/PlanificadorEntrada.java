package planificador;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import model.SocketClient;
import comandos.*;
import network.NodoManager;

/**
 * Planificador de Mensajes de Entrada.
 * Recibe los comandos que llegan por socket y los procesa en un hilo.
 */
public class PlanificadorEntrada extends Thread {
    private static final Logger LOGGER = Logger.getLogger(PlanificadorEntrada.class.getName());
    private final BlockingQueue<String> colaMensajes;
    private final AtomicBoolean ejecutando;
    private final PlanificadorPresidenteMesa planificadorPresidenteMesa;
    private final NodoManager nodoManager;

    public PlanificadorEntrada(PlanificadorPresidenteMesa planificadorPresidenteMesa) {
        this.colaMensajes = new LinkedBlockingQueue<>();
        this.ejecutando = new AtomicBoolean(true);
        this.planificadorPresidenteMesa = planificadorPresidenteMesa;
        this.nodoManager = new NodoManager("127.0.0.1", this, planificadorPresidenteMesa);
    }

    @Override
    public void run() {
        LOGGER.info("PlanificadorEntrada iniciado");
        while (ejecutando.get()) {
            try {
                String mensaje = colaMensajes.poll(1, TimeUnit.SECONDS);
                if (mensaje != null) {
                    procesarComando(mensaje);
                }
            } catch (InterruptedException e) {
                if (ejecutando.get()) {
                    LOGGER.log(Level.SEVERE, "Error al procesar mensaje", e);
                }
            }
        }
    }

    public void agregarMensaje(String mensaje) {
        try {
            colaMensajes.put(mensaje);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error al agregar mensaje a la cola", e);
        }
    }

    public void detener() {
        ejecutando.set(false);
        interrupt();
        LOGGER.info("PlanificadorEntrada detenido");
    }

    private void procesarComando(String mensaje) {
        LOGGER.info("Procesando comando: " + mensaje);
        String[] partes = mensaje.split("\\|");
        if (partes.length < 1) {
            LOGGER.warning("Mensaje inválido: " + mensaje);
            return;
        }

        String codigoComando = partes[0];
        switch (codigoComando) {
            case "0001":
                procesarComandoListaNodos(partes);
                break;
            case "0002":
                procesarComandoListaCandidatos(partes);
                break;
            case "0003":
                procesarComandoAltaCandidato(partes);
                break;
            case "0004":
                procesarComandoVotacion(partes);
                break;
            case "0005":
                procesarComandoConfirmacionVoto(partes);
                break;
            case "0006":
                procesarComandoAltaVotante(partes);
                break;
            case "0009":
                procesarComandoVotacionDistribuida(partes);
                break;
            case "0010":
                procesarComandoConfirmacionVotoDistribuido(partes);
                break;
            case "0011":
                procesarComandoSincronizacionVoto(partes);
                break;
            default:
                LOGGER.warning("Comando desconocido: " + codigoComando);
        }
    }

    private void procesarComandoListaNodos(String[] partes) {
        if (partes.length < 2) {
            LOGGER.warning("Formato inválido para lista de nodos");
            return;
        }

        String ipOrigen = partes[1];
        LOGGER.info("Procesando lista de nodos: " + ipOrigen);

        // Si es un nodo secundario, agregar la IP a la lista de nodos
        if (!ipOrigen.equals("127.0.0.1")) {
            LOGGER.info("IP A CONECTAR: " + ipOrigen);
            nodoManager.agregarNodo(ipOrigen);
        }
    }

    private void procesarComandoListaCandidatos(String[] partes) {
        if (partes.length < 2) {
            LOGGER.warning("Formato inválido para lista de candidatos");
            return;
        }

        String candidatosJson = partes[1];
        LOGGER.info("Procesando lista de candidatos: " + candidatosJson);
        // TODO: Implementar lógica para actualizar la lista de candidatos en la base de datos
    }

    private void procesarComandoAltaCandidato(String[] partes) {
        if (partes.length < 3) {
            LOGGER.warning("Formato inválido para alta de candidato");
            return;
        }

        String codigo = partes[1];
        String nombre = partes[2];
        LOGGER.info("Procesando alta de candidato: " + codigo + " - " + nombre);
        // TODO: Implementar lógica para agregar candidato a la base de datos
    }

    private void procesarComandoVotacion(String[] partes) {
        if (partes.length < 4) {
            LOGGER.warning("Formato inválido para votación");
            return;
        }

        String idVoto = partes[1];
        String codigoVotante = partes[2];
        String codigoCandidato = partes[3];
        LOGGER.info("Procesando votación: " + idVoto + " - " + codigoVotante + " - " + codigoCandidato);
        // TODO: Implementar lógica para procesar el voto
    }

    private void procesarComandoConfirmacionVoto(String[] partes) {
        if (partes.length < 4) {
            LOGGER.warning("Formato inválido para confirmación de voto");
            return;
        }

        String idVoto = partes[1];
        boolean confirmado = Boolean.parseBoolean(partes[2]);
        String ipOrigen = partes[3];
        LOGGER.info("Procesando confirmación de voto: " + idVoto + " - " + confirmado + " - " + ipOrigen);
        // TODO: Implementar lógica para procesar la confirmación
    }

    private void procesarComandoAltaVotante(String[] partes) {
        if (partes.length < 3) {
            LOGGER.warning("Formato inválido para alta de votante");
            return;
        }

        String codigo = partes[1];
        String llavePrivada = partes[2];
        LOGGER.info("Procesando alta de votante: " + codigo);
        // TODO: Implementar lógica para agregar votante a la base de datos
    }

    private void procesarComandoVotacionDistribuida(String[] partes) {
        if (partes.length < 2) {
            LOGGER.warning("Formato inválido para votación distribuida");
            return;
        }

        try {
            // Parsear el comando y loguear el ID obtenido
            VotacionComando comando = VotacionComando.parsear(String.join("|", partes));
            LOGGER.info("Voto recibido con ID: " + comando.getVoto().getId());

            // Agregar el voto a ambos planificadores
            PlanificadorTransaccion.addVoto(comando);
            PlanificadorPresidenteMesa.addVoto(comando);

            // Generar confirmación y enviarla al nodo origen
            ConfirmacionVotoComando confirmacion = new ConfirmacionVotoComando(
                    comando.getVoto().getId(),
                    true,
                    comando.getIp() != null ? comando.getIp() : "0.0.0.0" // asigna un valor por defecto si la IP es nula
            );
            planificadorPresidenteMesa.getPlanificadorSalida().addMessage(confirmacion);

            LOGGER.info("Voto procesado y confirmación enviada: " + comando.getVoto().getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error procesando voto distribuido", e);
        }
    }


    private void procesarComandoConfirmacionVotoDistribuido(String[] partes) {
        if (partes.length < 2) {
            LOGGER.warning("Formato inválido para confirmación de voto distribuido");
            return;
        }
        try {
            // Se utiliza el método parsear para construir el objeto ConfirmacionVotoComando
            ConfirmacionVotoComando comando = ConfirmacionVotoComando.parsear(String.join("|", partes));
            // Por ejemplo, pasamos este comando a PlanificadorPresidenteMesa para incrementar confirmaciones
            planificadorPresidenteMesa.confirmarVoto(comando);
            LOGGER.info("Confirmación de voto procesada: " + comando.getIdVoto());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error procesando confirmación de voto", e);
        }
    }


    private void procesarComandoSincronizacionVoto(String[] partes) {
        if (partes.length < 2) {
            LOGGER.warning("Formato inválido para sincronización de voto");
            return;
        }

        try {
            SincronizacionBloqueComando comando = SincronizacionBloqueComando.parsear(String.join("|", partes));
            // Aquí se llama al planificador de transacción para hacer el commit
            PlanificadorTransaccion.commitVoto(comando);
            LOGGER.info("Voto sincronizado y commiteado: " + comando.getIdVoto());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error procesando sincronización de voto", e);
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
            LOGGER.severe("Error verificando IP: " + e.getMessage());
        }
        return false;
    }
}
