package comandos;

import BD.CandidatoDao;

/**
 * Comando 0004: Eliminar candidato.
 */
public class EliminarCandidatoComando {
    private String codigo;

    public EliminarCandidatoComando(String codigo) {
        this.codigo = codigo;
    }

    public void ejecutar() {
        CandidatoDao.delete(codigo);
        System.out.println("Candidato eliminado: " + codigo);
    }
}
