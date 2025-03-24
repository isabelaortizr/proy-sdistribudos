package comandos;

public class SincronizacionBloqueComando extends Comando {
    private final String idVoto;

    public SincronizacionBloqueComando(String idVoto) {
        this.idVoto = idVoto;
    }

    @Override
    public String getComando() {
        // Formato: 0011|id_voto
        return "0011|" + idVoto;
    }

    @Override
    public String getCodigoComando() {
        return "0011";
    }

    public static boolean validarFormato(String comando) {
        String[] partes = comando.split("\\|");
        return partes.length == 2 && partes[0].equals("0011") && !partes[1].trim().isEmpty();
    }

    public static SincronizacionBloqueComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        String[] partes = comando.split("\\|");
        return new SincronizacionBloqueComando(partes[1].trim());
    }

    public String getIdVoto() {
        return idVoto;
    }
}
