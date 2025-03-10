package comandos;

import BD.CandidatoDao;

/**
 * Comando 0003: Alta de candidato.
 */
public class AltaCandidatoComando {
    private String codigo;
    private String nombre;

    public AltaCandidatoComando(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public void ejecutar() {
        CandidatoDao.insertOrUpdate(codigo, nombre);
        System.out.println("Alta de candidato: " + codigo + " - " + nombre);
    }
}
