package comandos;

import BD.VotanteDao;

/**
 * Comando 0005: Sincronizar votantes.
 * Recibe un string con varios votantes separados por ';',
 * cada votante tiene "codigo,llavePrivada".
 */
public class SyncVotantesComando {
    private String datos;

    public SyncVotantesComando(String datos) {
        this.datos = datos;
    }

    public void ejecutar() {
        // Ej: "00045242,abcd1234;0136464,xyz9876;0000465,fooBar"
        String[] votantes = datos.split(";");
        for (String vot : votantes) {
            String[] partes = vot.split(",", 2);
            if (partes.length == 2) {
                String codigo = partes[0].trim();
                String llavePrivada = partes[1].trim();
                VotanteDao.insertOrUpdate(codigo, llavePrivada);
            }
        }
        System.out.println("Se han sincronizado votantes.");
    }
}
