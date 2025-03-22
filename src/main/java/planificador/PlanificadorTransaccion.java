package planificador;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import comandos.*;
import db.DatabaseManager;

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
            VotacionComando comando = (VotacionComando) messages.remove(confirmacion.getIdBloque());
            if (comando != null) {
                System.out.println(String.format("Voto: %s listo para registrar en base de datos", confirmacion.getIdBloque()));
                // Aquí iría la lógica para insertar en la base de datos
            }
        }
    }

    public void detener() {
        running = false;
    }
} 