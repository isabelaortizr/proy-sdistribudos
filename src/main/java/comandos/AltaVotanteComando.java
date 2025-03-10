package comandos;

import BD.VotanteDao;

/**
 * Comando 0006: Alta de votante.
 */
public class AltaVotanteComando {
    private String codigo;
    private String llavePrivada;

    public AltaVotanteComando(String codigo, String llavePrivada) {
        this.codigo = codigo;
        this.llavePrivada = llavePrivada;
    }

    public void ejecutar() {
        VotanteDao.insertOrUpdate(codigo, llavePrivada);
        System.out.println("Alta de votante: " + codigo);
    }
}
