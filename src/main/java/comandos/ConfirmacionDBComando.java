package comandos;

import java.util.regex.Pattern;
import planificador.PlanificadorTransaccion;

/**
 * Comando 0012: Confirmación de Base de Datos
 * Formato: 0012|id_voto
 */
public class ConfirmacionDBComando extends Comando {
    private final String idVoto;

    public ConfirmacionDBComando(String idVoto) {
        this.idVoto = idVoto;
    }

    @Override
    public String getComando() {
        return String.format("0012|%s", idVoto);
    }

    @Override
    public String getCodigoComando() {
        return "0012";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            return partes[0].equals("0012") && partes.length == 2 && !partes[1].trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static ConfirmacionDBComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        String[] partes = comando.split("\\|");
        return new ConfirmacionDBComando(partes[1].trim());
    }

    public String getIdVoto() {
        return idVoto;
    }
} 