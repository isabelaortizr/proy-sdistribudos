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

    public long getTimestamp() {
        return timestamp;
    }

    public String getCodigoVotante() {
        return codigoVotante;
    }

    public String getCodigoCandidato() {
        return codigoCandidato;
    }

    public String getRefAnteriorBloque() {
        return refAnteriorBloque;
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
