package api.dto;

import lombok.Setter;

/**
 * Clase que representa una solicitud de voto
 */
@Setter
public class VotoRequest {
    private String idVoto;
    private String codigoVotante;
    private String codigoCandidato;
    private String firma; // Nuevo campo para la firma

    // Constructor por defecto
    public VotoRequest() {
    }

    // Constructor con todos los campos
    public VotoRequest(String idVoto, String codigoVotante, String codigoCandidato, String firma) {
        this.idVoto = idVoto;
        this.codigoVotante = codigoVotante;
        this.codigoCandidato = codigoCandidato;
        this.firma = firma;
    }

    // Getters y Setters
    public String getIdVoto() {
        return idVoto;
    }

    public void setIdVoto(String idVoto) {
        this.idVoto = idVoto;
    }

    public String getCodigoVotante() {
        return codigoVotante;
    }

    public void setCodigoVotante(String codigoVotante) {
        if (codigoVotante == null || codigoVotante.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del votante es requerido");
        }
        this.codigoVotante = codigoVotante;
    }

    public String getCodigoCandidato() {
        return codigoCandidato;
    }

    public void setCodigoCandidato(String codigoCandidato) {
        if (codigoCandidato == null || codigoCandidato.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del candidato es requerido");
        }
        this.codigoCandidato = codigoCandidato;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        if(firma == null || firma.trim().isEmpty()){
            throw new IllegalArgumentException("La firma es requerida");
        }
        this.firma = firma;
    }

    /**
     * Valida que todos los campos requeridos estén presentes
     * @throws IllegalArgumentException si algún campo requerido está vacío
     */
    public void validar() {
        if (idVoto == null || idVoto.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del voto es requerido");
        }
        if (codigoVotante == null || codigoVotante.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del votante es requerido");
        }
        if (codigoCandidato == null || codigoCandidato.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del candidato es requerido");
        }
        if (firma == null || firma.trim().isEmpty()) {
            throw new IllegalArgumentException("La firma es requerida");
        }
    }

    @Override
    public String toString() {
        return "VotoRequest{" +
                "idVoto='" + idVoto + '\'' +
                ", codigoVotante='" + codigoVotante + '\'' +
                ", codigoCandidato='" + codigoCandidato + '\'' +
                ", firma='" + firma + '\'' +
                '}';
    }
}
