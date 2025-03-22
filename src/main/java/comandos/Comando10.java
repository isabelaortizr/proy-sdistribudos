package comandos;

import java.util.regex.Pattern;

/**
 * Comando 0010: Confirmación de voto
 * Formato: 0010|id_voto,confirmado,ip_origen
 */
public class Comando10 extends Comando {
    private final String idVoto;
    private final boolean confirmado;
    private final String ipOrigen;

    public Comando10(String idVoto, boolean confirmado, String ipOrigen) {
        this.idVoto = idVoto;
        this.confirmado = confirmado;
        this.ipOrigen = ipOrigen;
    }

    @Override
    public String getComando() {
        return String.format("0010|%s,%s,%s", idVoto, confirmado ? "1" : "0", ipOrigen);
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
                   (datos[1].trim().equals("1") || datos[1].trim().equals("0")) &&
                   !datos[2].trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static Comando10 parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        
        String[] partes = comando.split("\\|");
        String[] datos = partes[1].split(",");
        
        return new Comando10(
            datos[0].trim(),
            datos[1].trim().equals("1"),
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