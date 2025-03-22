package comandos;

import model.Voto;
import java.util.regex.Pattern;

/**
 * Comando 0009: Votación
 * Formato: 0009|id_voto,timestamp,codigo_votante,codigo_candidato,firma
 */
public class Comando09 extends Comando {
    private Voto voto;
    private String firma;

    public Comando09(Voto voto, String firma) {
        this.voto = voto;
        this.firma = firma;
    }

    @Override
    public String getComando() {
        return String.format("0009|%s,%d,%s,%s,%s",
            voto.getId(),
            voto.getTimestamp(),
            voto.getCodigoVotante(),
            voto.getCodigoCandidato(),
            firma);
    }

    @Override
    public String getCodigoComando() {
        return "0009";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split(Pattern.quote("|"));
            if (!partes[0].equals("0009") || partes.length != 2) return false;
            
            String[] datos = partes[1].split(",");
            return datos.length == 5;
        } catch (Exception e) {
            return false;
        }
    }

    public static Comando09 parsear(String comando) {
        try {
            String[] partes = comando.split(Pattern.quote("|"));
            String[] datos = partes[1].split(",");
            
            Voto voto = new Voto(
                datos[0],                    // id
                Long.parseLong(datos[1]),    // timestamp
                datos[2],                    // codigoVotante
                datos[3]                     // codigoCandidato
            );
            
            return new Comando09(voto, datos[4]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de comando inválido: " + e.getMessage());
        }
    }

    public Voto getVoto() {
        return voto;
    }

    public String getFirma() {
        return firma;
    }
} 