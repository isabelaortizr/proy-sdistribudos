package planificador;

import comandos.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Planificador de Mensajes de Entrada.
 * Recibe los comandos que llegan por socket y los procesa en un hilo.
 */
public class PlanificadorEntrada {
    private BlockingQueue<String> colaMensajes;
    private boolean running = true;

    public PlanificadorEntrada() {
        this.colaMensajes = new LinkedBlockingQueue<>();
        iniciarProcesamiento();
    }

    /**
     * Método para recibir un comando desde el socket y encolarlo.
     */
    public void recibirComando(String comando) {
        try {
            colaMensajes.put(comando);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inicia el hilo que va tomando comandos de la cola y los procesa.
     */
    private void iniciarProcesamiento() {
        new Thread(() -> {
            while (running) {
                try {
                    String comando = colaMensajes.take();
                    procesarComando(comando);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Procesa el comando identificando el código (0001..0007) y
     * ejecuta la clase correspondiente.
     */
    private void procesarComando(String comando) {
        // Ej: "0003|1234,Juan Perez"
        String[] partes = comando.split("\\|", 2);
        if (partes.length < 2) {
            System.out.println("Comando inválido: " + comando);
            return;
        }

        String codigo = partes[0];
        String datos = partes[1];

        switch (codigo) {
            case "0001":
                // Lista de nodos
                new ListaNodosComando(datos).ejecutar();
                break;
            case "0002":
                // Sincronizar candidatos
                new SyncCandidatosComando(datos).ejecutar();
                break;
            case "0003":
                // Alta candidato
                String[] partsC = datos.split(",", 2);
                if (partsC.length == 2) {
                    new AltaCandidatoComando(partsC[0], partsC[1]).ejecutar();
                }
                break;
            case "0004":
                // Eliminar candidato
                new EliminarCandidatoComando(datos).ejecutar();
                break;
            case "0005":
                // Sincronizar votantes
                new SyncVotantesComando(datos).ejecutar();
                break;
            case "0006":
                // Alta votante
                String[] partsV = datos.split(",", 2);
                if (partsV.length == 2) {
                    new AltaVotanteComando(partsV[0], partsV[1]).ejecutar();
                }
                break;
            case "0007":
                // Eliminar votante
                new EliminarVotanteComando(datos).ejecutar();
                break;
            default:
                System.out.println("Código de comando desconocido: " + codigo);
                break;
        }
    }
}
