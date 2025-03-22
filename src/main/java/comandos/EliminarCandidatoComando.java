package comandos;

import BD.CandidatoDao;

/**
 * Comando 0003: Eliminar Candidato
 * Formato: 0003|codigo_candidato
 */
public class EliminarCandidatoComando extends Comando {
    private final String codigoCandidato;

    public EliminarCandidatoComando(String codigoCandidato) {
        this.codigoCandidato = codigoCandidato;
    }

    @Override
    public String getComando() {
        return String.format("0003|%s", codigoCandidato);
    }

    @Override
    public String getCodigoComando() {
        return "0003";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            return partes[0].equals("0003") && partes.length == 2 && !partes[1].trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static EliminarCandidatoComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        String[] partes = comando.split("\\|");
        return new EliminarCandidatoComando(partes[1].trim());
    }

    public String getCodigoCandidato() {
        return codigoCandidato;
    }

    public void ejecutar() {
        CandidatoDao.delete(codigoCandidato);
        System.out.println("Candidato eliminado: " + codigoCandidato);
    }
}
