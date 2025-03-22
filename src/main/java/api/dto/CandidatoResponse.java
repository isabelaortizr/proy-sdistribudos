package api.dto;

public class CandidatoResponse {
    private String codigo;
    private String nombre;

    public CandidatoResponse(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
} 