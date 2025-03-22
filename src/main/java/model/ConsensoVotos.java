package model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import planificador.PlanificadorPresidenteMesa;
import comandos.ConfirmacionVotoComando;

public class ConsensoVotos {
    // Map: idVoto -> Set de IPs que han confirmado
    private static final ConcurrentHashMap<String, Set<String>> confirmacionesVotos = new ConcurrentHashMap<>();
    // Map: idVoto -> número total de confirmaciones necesarias
    private static final ConcurrentHashMap<String, Integer> confirmacionesNecesarias = new ConcurrentHashMap<>();
    private final PlanificadorPresidenteMesa planificadorPresidenteMesa;

    public ConsensoVotos(PlanificadorPresidenteMesa planificadorPresidenteMesa) {
        this.planificadorPresidenteMesa = planificadorPresidenteMesa;
    }

    public void iniciarConsenso(String idVoto, int totalNodos) {
        confirmacionesVotos.put(idVoto, new HashSet<>());
        confirmacionesNecesarias.put(idVoto, totalNodos);
    }

    public void registrarConfirmacion(String idVoto, String ipNodo) {
        Set<String> confirmaciones = confirmacionesVotos.get(idVoto);
        if (confirmaciones != null) {
            synchronized (confirmaciones) {
                confirmaciones.add(ipNodo);
                
                // Verificar si tenemos todas las confirmaciones necesarias
                if (confirmaciones.size() >= confirmacionesNecesarias.get(idVoto)) {
                    // Enviar comando de confirmación final
                    ConfirmacionVotoComando confirmacion = new ConfirmacionVotoComando(idVoto, true, ipNodo);
                    planificadorPresidenteMesa.confirmarVoto(confirmacion);
                    
                    // Limpiar las estructuras de datos
                    confirmacionesVotos.remove(idVoto);
                    confirmacionesNecesarias.remove(idVoto);
                }
            }
        }
    }

    public boolean tieneTodasLasConfirmaciones(String idVoto) {
        Set<String> confirmaciones = confirmacionesVotos.get(idVoto);
        Integer necesarias = confirmacionesNecesarias.get(idVoto);
        return confirmaciones != null && necesarias != null && 
               confirmaciones.size() >= necesarias;
    }

    public void limpiarVoto(String idVoto) {
        confirmacionesVotos.remove(idVoto);
        confirmacionesNecesarias.remove(idVoto);
    }
} 