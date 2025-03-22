package model;

public class Voto {
    private String id;
    private long timestamp;
    private String codigoVotante;
    private String codigoCandidato;
    private String refAnteriorBloque;

    public Voto(String id, long timestamp, String codigoVotante, String codigoCandidato) {
        this.id = id;
        this.timestamp = timestamp;
        this.codigoVotante = codigoVotante;
        this.codigoCandidato = codigoCandidato;
        this.refAnteriorBloque = "";
    }

    public Voto(String id, long timestamp, String codigoVotante, String codigoCandidato, String refAnteriorBloque) {
        this.id = id;
        this.timestamp = timestamp;
        this.codigoVotante = codigoVotante;
        this.codigoCandidato = codigoCandidato;
        this.refAnteriorBloque = refAnteriorBloque;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCodigoVotante() {
        return codigoVotante;
    }

    public void setCodigoVotante(String codigoVotante) {
        this.codigoVotante = codigoVotante;
    }

    public String getCodigoCandidato() {
        return codigoCandidato;
    }

    public void setCodigoCandidato(String codigoCandidato) {
        this.codigoCandidato = codigoCandidato;
    }

    public String getRefAnteriorBloque() {
        return refAnteriorBloque;
    }

    public void setRefAnteriorBloque(String refAnteriorBloque) {
        this.refAnteriorBloque = refAnteriorBloque;
    }

    public boolean haExpirado(long tiempoMaximo) {
        return System.currentTimeMillis() - timestamp > tiempoMaximo;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%s,%s,%s",
            id,
            timestamp,
            codigoVotante,
            codigoCandidato,
            refAnteriorBloque
        );
    }
} 