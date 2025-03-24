package comandos;

import model.Voto;
import lombok.Getter;
import lombok.Setter;
import java.util.regex.Pattern;

/**
 * Comando 0009: Votación
 * Formato: 0009|id,tiempo_creacion,codigo_votante,codigo_candidato,ref_anterior_bloque|firma
 */
@Getter
@Setter
public class VotacionComando extends Comando {
    public static final String CODIGO_COMANDO = "0009";
    private Voto voto;
    private String firma;
    private int cantidadConfirmaciones;
    private boolean sincronizado = false;


    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }

    public VotacionComando(Voto voto, String firma) {
        this.setCodigoComando(CODIGO_COMANDO);
        this.voto = voto;
        this.cantidadConfirmaciones = 0;
        this.firma = firma;
    }

    public VotacionComando(String ip) {
        super();
        this.setCodigoComando(CODIGO_COMANDO);
        setIp(ip);
        this.voto = null;
        this.cantidadConfirmaciones = 0;
    }

    @Override
    public String getComando() {
        return String.format("%s|%s,%d,%s,%s|%s%n", 
            CODIGO_COMANDO,
            voto.getId(),
            voto.getTimestamp(),
            voto.getCodigoVotante(),
            voto.getCodigoCandidato(),
            firma
        );
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] tokens = comando.split(Pattern.quote("|"));
            if (!tokens[0].equals(CODIGO_COMANDO) || tokens.length != 3) return false;
            
            String[] votoArray = tokens[1].split(",");
            return votoArray.length == 4 && // id, timestamp, codigoVotante, codigoCandidato
                   !votoArray[0].trim().isEmpty() && // id
                   Long.parseLong(votoArray[1].trim()) > 0 && // timestamp
                   !votoArray[2].trim().isEmpty() && // codigoVotante
                   !votoArray[3].trim().isEmpty() && // codigoCandidato
                   !tokens[2].trim().isEmpty(); // firma
        } catch (Exception e) {
            System.err.println("Error validando formato: " + e.getMessage());
            return false;
        }
    }

    public static VotacionComando parsear(String comando) {
        try {
            String[] tokens = comando.split(Pattern.quote("|"));
            if (tokens.length >= 2) {
                String[] votoArray = tokens[1].split(",");
                if (votoArray.length == 4) {
                    Voto voto = new Voto(
                            votoArray[0], // id
                            Long.parseLong(votoArray[1]), // timestamp
                            votoArray[2], // codigoVotante
                            votoArray[3], // codigoCandidato
                            "" // refAnteriorBloque (se puede obtener después)
                    );
                    System.out.println("Parseado Voto con ID: " + voto.getId());
                    return new VotacionComando(voto, tokens[2].trim());
                }
            }
            throw new IllegalArgumentException("Formato de comando inválido");
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parseando comando: " + e.getMessage());
        }
    }

    public Voto getVoto() {
        return voto;
    }

    public void setVoto(Voto voto) {
        this.voto = voto;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public int getCantidadConfirmaciones() {
        return cantidadConfirmaciones;
    }

    public void setCantidadConfirmaciones(int cantidadConfirmaciones) {
        this.cantidadConfirmaciones = cantidadConfirmaciones;
    }

    public void incrementarConfirmaciones() {
        this.cantidadConfirmaciones++;
    }
} 