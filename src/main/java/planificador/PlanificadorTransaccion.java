package planificador;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import comandos.*;
import db.DatabaseManager;
import model.Voto;

public class PlanificadorTransaccion implements Runnable {
    private static final Map<String, Comando> messages = new ConcurrentHashMap<>();
    private final DatabaseManager dbManager;
    private volatile boolean running = true;

    public PlanificadorTransaccion() {
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public void run() {
        System.out.println("PlanificadorTransaccion iniciado");
        while (running) {
            try {
                synchronized (messages) {
                    for (Comando comando : messages.values()) {
                        if (comando instanceof VotacionComando) {
                            VotacionComando vc = (VotacionComando) comando;
                            System.out.println("Comando ID: " + vc.getVoto().getId());
                            
                            if (System.currentTimeMillis() - vc.getVoto().getTimestamp() > 10000) {
                                System.out.println("Tiempo de espera finalizado");
                                removeItem(vc.getVoto().getId());
                            }
                        }
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                if (running) {
                    System.err.println("Error en PlanificadorTransaccion: " + e.getMessage());
                }
            }
        }
        System.out.println("PlanificadorTransaccion detenido");
    }

    private void removeItem(String key) {
        synchronized (messages) {
            messages.remove(key);
            messages.notify();
        }
    }

    public static void addVoto(VotacionComando comando) {
        synchronized (messages) {
            messages.put(comando.getVoto().getId(), comando);
            messages.notify();
        }
    }

    public static void commitVoto(SincronizacionBloqueComando confirmacion) {
        synchronized (messages) {
            VotacionComando comando = (VotacionComando) messages.remove(confirmacion.getIdVoto());
            if (comando != null) {
                System.out.println(String.format("Voto: %s listo para registrar en base de datos", confirmacion.getIdVoto()));
                try {
                    // Obtener el objeto Voto
                    Voto voto = comando.getVoto();
                    DatabaseManager dbManager = DatabaseManager.getInstance();

                    // Registrar el voto en la base de datos
                    dbManager.registrarVoto(
                            voto.getId(),
                            voto.getCodigoVotante(),
                            voto.getCodigoCandidato(),
                            "SIN_HASH", // si no usas hash, puedes enviar un valor fijo o vacío
                            voto.getRefAnteriorBloque()
                    );
                    System.out.println("Voto insertado en la base de datos correctamente.");

                    // Verificación adicional: consultamos si el voto existe
                    if (dbManager.existeVoto(voto.getId())) {
                        System.out.println("Verificación: El voto " + voto.getId() + " se guardó correctamente.");
                    } else {
                        System.err.println("Error: No se encontró el voto " + voto.getId() + " tras la inserción.");
                    }
                } catch (SQLException ex) {
                    System.err.println("Error al insertar el voto en la base de datos: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                System.out.println("No se encontró voto con ID: " + confirmacion.getIdVoto());
            }
        }
    }









    public void detener() {
        running = false;
    }
} 