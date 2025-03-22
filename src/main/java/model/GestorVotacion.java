package model;

import db.DatabaseManager;
import comandos.VotacionComando;
import comandos.ConfirmacionVotoComando;
import planificador.PlanificadorPresidenteMesa;
import java.util.UUID;

public class GestorVotacion {
    private final DatabaseManager dbManager;
    private final PlanificadorPresidenteMesa planificadorPresidenteMesa;
    private final ConsensoVotos consensoVotos;

    public GestorVotacion(PlanificadorPresidenteMesa planificadorPresidenteMesa) {
        this.dbManager = DatabaseManager.getInstance();
        this.planificadorPresidenteMesa = planificadorPresidenteMesa;
        this.consensoVotos = new ConsensoVotos(planificadorPresidenteMesa);
    }

    public String procesarVoto(String codigoVotante, String codigoCandidato) throws Exception {
        // Validaciones
        if (!dbManager.esVotanteValido(codigoVotante)) {
            throw new IllegalArgumentException("Votante inválido o ya ha votado");
        }
        if (!dbManager.existeCandidato(codigoCandidato)) {
            throw new IllegalArgumentException("Candidato no existe");
        }

        // Crear voto
        String idVoto = UUID.randomUUID().toString();
        String refAnteriorBloque = dbManager.obtenerUltimaReferenciaBloque();
        long timestamp = System.currentTimeMillis();

        // Crear objeto Voto
        Voto voto = new Voto(
            idVoto,
            timestamp,
            codigoVotante,
            codigoCandidato,
            refAnteriorBloque
        );

        // Crear comando con el voto y una firma temporal (deberías implementar la firma real)
        String firma = "firma_temporal"; // Aquí deberías generar una firma real
        VotacionComando comando = new VotacionComando(voto, firma);

        // Iniciar consenso
        consensoVotos.iniciarConsenso(idVoto, dbManager.obtenerTotalNodos());

        // Enviar a otros nodos
        planificadorPresidenteMesa.getPlanificadorSalida().addMessage(comando);

        return idVoto;
    }

    public void procesarConfirmacion(String idVoto, boolean confirmado, String ipNodo) {
        if (confirmado) {
            consensoVotos.registrarConfirmacion(idVoto, ipNodo);
        } else {
            consensoVotos.limpiarVoto(idVoto);
            try {
                dbManager.eliminarVoto(idVoto);
            } catch (Exception e) {
                System.err.println("Error eliminando voto rechazado: " + e.getMessage());
            }
        }
    }
} 