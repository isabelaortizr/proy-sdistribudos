package comandos;

import BD.VotanteDao;

/**
 * Comando 0006: Alta de votante.
 */
public class AltaVotanteComando extends Comando {
    private String codigo;
    private String llavePrivada;

    public AltaVotanteComando(String codigo, String llavePrivada) {
        super();
        this.codigo = codigo;
        this.llavePrivada = llavePrivada;
        this.setCodigoComando("0006");
    }

    @Override
    public String getComando() {
        return String.format("%s|%s,%s", getCodigoComando(), codigo, llavePrivada);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getLlavePrivada() {
        return llavePrivada;
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            if (!partes[0].equals("0006") || partes.length != 2) return false;
            
            String[] datos = partes[1].split(",");
            return datos.length == 2 && !datos[0].isEmpty() && !datos[1].isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static AltaVotanteComando parsear(String comando) {
        try {
            String[] partes = comando.split("\\|");
            String[] datos = partes[1].split(",");
            return new AltaVotanteComando(datos[0], datos[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de comando inválido: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return getComando();
    }

    public void ejecutar() {
        VotanteDao.insertOrUpdate(codigo, llavePrivada);
        System.out.println("Alta de votante: " + codigo);
    }
}
