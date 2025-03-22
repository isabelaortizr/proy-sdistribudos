package planificador;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import comandos.*;
import db.DatabaseManager;

public class PlanificadorPresidenteMesa implements Runnable {
    private static final Map<String, Comando> messages = new ConcurrentHashMap<>();
    private final DatabaseManager dbManager;
    private final PlanificadorSalida planificadorSalida;
    private volatile boolean running = true;

    public PlanificadorPresidenteMesa(String[] nodosDestino) {
        this.dbManager = DatabaseManager.getInstance();
        this.planificadorSalida = new PlanificadorSalida(nodosDestino);
        this.planificadorSalida.start();
    }

    @Override
    public void run() {
        System.out.println("PlanificadorPresidenteMesa iniciado");
        while (running) {
            try {
                synchronized (messages) {
                    for (Comando comando : messages.values()) {
                        if (comando instanceof VotacionComando) {
                            VotacionComando vc = (VotacionComando) comando;
                            System.out.println("Comando ID: " + vc.getVoto().getId());
                            
                            // Verificar si tenemos todas las confirmaciones necesarias
                            if (vc.getCantidadConfirmaciones() == planificadorSalida.getCantidadNodos()) {
                                System.out.println("Ya está con las confirmaciones necesarias. Se envía mensaje a todos los nodos");
                                removeItem(vc.getVoto().getId());
                                // Crear y enviar comando de sincronización
                                SincronizacionBloqueComando sincronizacion = new SincronizacionBloqueComando(
                                    vc.getVoto().getId(),
                                    vc.getVoto().getRefAnteriorBloque(),
                                    System.currentTimeMillis(),
                                    vc.getVoto().toString()
                                );
                                planificadorSalida.addMessage(sincronizacion);
                                continue;
                            }

                            // Verificar timeout
                            if (System.currentTimeMillis() - vc.getVoto().getTimestamp() > 10000) {
                                System.out.println("Tiempo de espera finalizado para voto: " + vc.getVoto().getId());
                                removeItem(vc.getVoto().getId());
                            }
                        }
                    }
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                if (running) {
                    System.err.println("Error en PlanificadorPresidenteMesa: " + e.getMessage());
                }
            }
        }
        System.out.println("PlanificadorPresidenteMesa detenido");
    }

    private void removeItem(String key) {
        synchronized (messages) {
            messages.remove(key);
            messages.notify();
        }
    }

    public static void addVoto(VotacionComando comando) {
        synchronized (messages) {
            if (!messages.containsKey(comando.getVoto().getId())) {
                messages.put(comando.getVoto().getId(), comando);
                messages.notify();
            } else {
                System.out.println("Presi: Voto ya existe: " + comando.getVoto().getId());
            }
        }
    }

    public static void confirmarVoto(ConfirmacionVotoComando confirmacion) {
        System.out.println("Cantidad votos: " + messages.size());
        System.out.println("Buscando voto con ID: " + confirmacion.getIdVoto());
        synchronized (messages) {
            VotacionComando comando = (VotacionComando) messages.get(confirmacion.getIdVoto());
            if (comando != null) {
                comando.incrementarConfirmaciones();
                messages.notify();
            }
        }
    }

    public PlanificadorSalida getPlanificadorSalida() {
        return planificadorSalida;
    }

    public void detener() {
        running = false;
        planificadorSalida.detener();
    }
} 