package comandos;

import BD.CandidatoDao;

/**
 * Comando 0002: Sincronizar candidatos.
 * Recibe un string con varios candidatos separados por ';',
 * cada candidato tiene "codigo,nombre".
 */
public class SyncCandidatosComando {
    private String datos;

    public SyncCandidatosComando(String datos) {
        this.datos = datos;
    }

    public void ejecutar() {
        // Ejemplo de datos: "00045242,Ricardo Laredo;0136464,Lucas;0000465,Alejandra"
        String[] candidatos = datos.split(";");
        for (String cand : candidatos) {
            String[] partes = cand.split(",", 2);
            if (partes.length == 2) {
                String codigo = partes[0].trim();
                String nombre = partes[1].trim();
                CandidatoDao.insertOrUpdate(codigo, nombre);
            }
        }
        System.out.println("Se han sincronizado candidatos.");
    }
}
