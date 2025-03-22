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
        this.idVoto = idVoto;
        this.confirmado = confirmado;
        this.ipOrigen = ipOrigen;
    }

    @Override
    public String getComando() {
        return String.format("0010|%s,%b,%s", idVoto, confirmado, ipOrigen);
    }

    @Override
    public String getCodigoComando() {
        return "0010";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            if (!partes[0].equals("0010") || partes.length != 2) return false;
            
            String[] datos = partes[1].split(",");
            return datos.length == 3 && 
                   !datos[0].trim().isEmpty() && 
                   (datos[1].trim().equalsIgnoreCase("true") || datos[1].trim().equalsIgnoreCase("false")) &&
                   !datos[2].trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static ConfirmacionVotoComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        
        String[] partes = comando.split("\\|");
        String[] datos = partes[1].split(",");
        
        return new ConfirmacionVotoComando(
            datos[0].trim(),
            Boolean.parseBoolean(datos[1].trim()),
            datos[2].trim()
        );
    }

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