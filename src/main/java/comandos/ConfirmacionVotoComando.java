package comandos;

import java.util.regex.Pattern;
import planificador.PlanificadorPresidenteMesa;

/**
 * Comando 0010: Confirmación de Voto
 * Formato: 0010|id_voto,confirmado,ip_origen
 */
public class ConfirmacionVotoComando extends Comando {
    private final String idVoto;
    private final boolean confirmado;
    private final String ipOrigen;

    public ConfirmacionVotoComando(String idVoto, boolean confirmado, String ipOrigen) {
        setCodigoComando("0010");
        this.idVoto = idVoto;
        this.confirmado = confirmado;
        this.ipOrigen = ipOrigen;
    }

    @Override
    public String getComando() {
        // Formato: 0010|idVoto,confirmado,ipOrigen
        return String.format("%s|%s,%b,%s", getCodigoComando(), idVoto, confirmado, ipOrigen);
    }

    @Override
    public String getCodigoComando() {
        return "0010";
    }

    /**
     * Valida que el comando tenga el formato correcto:
     * 0010|idVoto,confirmado,ipOrigen
     */
    public static boolean validarFormato(String comando) {
        String[] partes = comando.split("\\|");
        if (partes.length != 2) return false;
        if (!partes[0].equals("0010")) return false;
        String[] datos = partes[1].split(",");
        if (datos.length != 3) return false;
        if (datos[0].trim().isEmpty() || datos[2].trim().isEmpty()) return false;
        try {
            Boolean.parseBoolean(datos[1].trim());
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Parsea la cadena de comando y retorna un objeto ConfirmacionVotoComando.
     * Se espera el formato: 0010|idVoto,confirmado,ipOrigen
     */
    public static ConfirmacionVotoComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando 0010 inválido");
        }
        String[] partes = comando.split("\\|");
        String[] datos = partes[1].split(",");
        String idVoto = datos[0].trim();
        boolean confirmado = Boolean.parseBoolean(datos[1].trim());
        String ipOrigen = datos[2].trim();
        return new ConfirmacionVotoComando(idVoto, confirmado, ipOrigen);
    }

    // Getters
    public String getIdVoto() {
        return idVoto;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }
}
