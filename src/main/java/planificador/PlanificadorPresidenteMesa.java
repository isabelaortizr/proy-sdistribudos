package planificador;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import comandos.*;
import db.DatabaseManager;

public class PlanificadorPresidenteMesa implements Runnable {
    // Se usa un mapa para almacenar los votos, usando el ID del voto como clave
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
                    // Se itera sobre los votos registrados (se crea una copia para evitar ConcurrentModification)
                    for (Comando comando : new ArrayList<>(messages.values())) {
                        if (comando instanceof VotacionComando) {
                            VotacionComando vc = (VotacionComando) comando;
                            System.out.println("Procesando voto: " + vc.getVoto().getId());

                            // Determinar el umbral: si hay nodos conectados, se usa ese valor; de lo contrario, se fuerza 1 para pruebas.
                            int umbral = planificadorSalida.getCantidadNodos() > 0
                                    ? planificadorSalida.getCantidadNodos()
                                    : 1;
                            System.out.println("Confirmaciones actuales: " + vc.getCantidadConfirmaciones()
                                    + " | Umbral de nodos: " + umbral);

                            // Si se alcanzó o superó el umbral y el voto aún no ha sido sincronizado...
                            if (vc.getCantidadConfirmaciones() >= umbral && !vc.isSincronizado()) {
                                System.out.println("Confirmaciones suficientes para el voto "
                                        + vc.getVoto().getId() + ". Se envía comando 0011 de sincronización.");
                                vc.setSincronizado(true);
                                removeItem(vc.getVoto().getId());

                                // Crear el comando 0011
                                SincronizacionBloqueComando sincronizacion = new SincronizacionBloqueComando(
                                        vc.getVoto().getId()
                                );

                                // Enviar el comando 0011 a través del planificador de salida
                                planificadorSalida.addMessage(sincronizacion);

                                // Ejecutar el commit localmente para insertar el voto en la base de datos
                                PlanificadorTransaccion.commitVoto(sincronizacion);
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

    // Método para agregar un voto, evitando duplicados
    public static void addVoto(VotacionComando comando) {
        synchronized (messages) {
            if (!messages.containsKey(comando.getVoto().getId())) {
                messages.put(comando.getVoto().getId(), comando);
                System.out.println("Voto agregado en PlanificadorPresidenteMesa con ID: "
                        + comando.getVoto().getId());
                messages.notify();
            } else {
                System.out.println("Presi: Voto ya existe: " + comando.getVoto().getId());
            }
        }
    }

    // Método para procesar confirmaciones duplicadas
    public static void confirmarVoto(ConfirmacionVotoComando confirmacion) {
        System.out.println("Cantidad votos: " + messages.size());
        System.out.println("Buscando voto con ID: " + confirmacion.getIdVoto());
        synchronized (messages) {
            VotacionComando comando = (VotacionComando) messages.get(confirmacion.getIdVoto());
            if (comando != null) {
                // Si el voto ya fue sincronizado, se ignora la confirmación duplicada
                if (!comando.isSincronizado()) {
                    comando.incrementarConfirmaciones();
                    System.out.println("Incrementando confirmaciones para voto "
                            + confirmacion.getIdVoto() + ": " + comando.getCantidadConfirmaciones());
                } else {
                    System.out.println("El voto " + confirmacion.getIdVoto()
                            + " ya ha sido sincronizado, se ignora confirmación duplicada.");
                }
                messages.notify();
            } else {
                System.out.println("No se encontró voto con ID: " + confirmacion.getIdVoto());
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
