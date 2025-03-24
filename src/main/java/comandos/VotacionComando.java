package comandos;

import model.Voto;

/**
 * Comando 0009: Votación
 * Formato: 0009|id,tiempo_creacion,codigo_votante,codigo_candidato,ref_anterior_bloque|firma
 */
public class VotacionComando extends Comando {
    public static final String CODIGO_COMANDO = "0009";

    // Contiene los datos del voto
    private Voto voto;
    // Firma del voto (puede ser cadena vacía si no se maneja firma real)
    private String firma;

    // Campos para el conteo de confirmaciones y la marca de sincronizado
    private int cantidadConfirmaciones;
    private boolean sincronizado;

    /**
     * Constructor principal para crear un comando 0009 desde un objeto Voto y una firma.
     */
    public VotacionComando(Voto voto, String firma) {
        this.setCodigoComando(CODIGO_COMANDO);
        this.voto = voto;
        this.firma = firma;
        this.cantidadConfirmaciones = 0;
        this.sincronizado = false;
    }

    /**
     * Constructor alternativo si necesitas crear un VotacionComando con una IP definida,
     * por ejemplo, si se parsea de la red. No siempre es necesario.
     */
    public VotacionComando(String ip) {
        super();
        this.setCodigoComando(CODIGO_COMANDO);
        setIp(ip);
        this.cantidadConfirmaciones = 0;
        this.sincronizado = false;
    }

    /**
     * Genera la cadena del comando en el formato:
     * 0009|id,timestamp,codigoVotante,codigoCandidato,refAnteriorBloque|firma
     */
    @Override
    public String getComando() {
        // Nota: Si 'voto.getRefAnteriorBloque()' es null, conviene manejarlo.
        // Aquí asumimos que es "" o un valor no nulo.
        return String.format("%s|%s,%d,%s,%s,%s|%s",
                CODIGO_COMANDO,
                voto.getId(),
                voto.getTimestamp(),
                voto.getCodigoVotante(),
                voto.getCodigoCandidato(),
                voto.getRefAnteriorBloque(),
                firma
        );
    }

    @Override
    public String getCodigoComando() {
        return CODIGO_COMANDO;
    }

    /**
     * Valida si la cadena cumple con el formato mínimo:
     * 0009|...|...
     */
    public static boolean validarFormato(String comando) {
        try {
            String[] tokens = comando.split("\\|");
            // Esperamos algo como: [0] = "0009", [1] = "id,timestamp,codVotante,codCandidato,ref", [2] = firma
            if (tokens.length != 3) {
                return false;
            }
            if (!tokens[0].equals(CODIGO_COMANDO)) {
                return false;
            }

            // Partimos la parte [1] por comas, debe tener 5 elementos: id, timestamp, codVotante, codCandidato, refAnterior
            String[] votoArray = tokens[1].split(",");
            if (votoArray.length != 5) {
                return false;
            }

            // Validamos campos básicos
            String id = votoArray[0].trim();
            long timestamp = Long.parseLong(votoArray[1].trim()); // Si falla parse, salta excepción
            String codVotante = votoArray[2].trim();
            String codCandidato = votoArray[3].trim();
            String ref = votoArray[4].trim();
            String firma = tokens[2].trim();

            // Validar no vacíos (excepto refAnterior si la aceptas vacía)
            if (id.isEmpty() || codVotante.isEmpty() || codCandidato.isEmpty() || firma.isEmpty()) {
                return false;
            }
            if (timestamp <= 0) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Crea un VotacionComando a partir de la cadena, asumiendo el formato correcto.
     */
    public static VotacionComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido para 0009");
        }
        String[] tokens = comando.split("\\|");
        // tokens[1] = "id,timestamp,codVotante,codCandidato,refAnterior"
        // tokens[2] = "firma"
        String[] votoArray = tokens[1].split(",");

        String id = votoArray[0].trim();
        long timestamp = Long.parseLong(votoArray[1].trim());
        String codVotante = votoArray[2].trim();
        String codCandidato = votoArray[3].trim();
        String ref = votoArray[4].trim();
        String firma = tokens[2].trim();

        Voto voto = new Voto(
                id,
                timestamp,
                codVotante,
                codCandidato,
                ref
        );
        return new VotacionComando(voto, firma);
    }

    // Getters / Setters y lógica de confirmaciones

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

    public boolean isSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(boolean sincronizado) {
        this.sincronizado = sincronizado;
    }
}
