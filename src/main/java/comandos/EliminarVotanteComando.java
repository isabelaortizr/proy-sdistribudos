package comandos;

import BD.VotanteDao;

/**
 * Comando 0007: Eliminar votante.
 */
public class EliminarVotanteComando {
    private String codigo;

    public EliminarVotanteComando(String codigo) {
        this.codigo = codigo;
    }

    public void ejecutar() {
        VotanteDao.delete(codigo);
        System.out.println("Votante eliminado: " + codigo);
    }
}
