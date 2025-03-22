package comandos;

import java.util.regex.Pattern;
import db.DatabaseManager;

/**
 * Comando para dar de alta un candidato
 * Formato: 0001|codigo,nombre
 */
public class AltaCandidatoComando extends Comando {
    private String codigo;
    private String nombre;

    public AltaCandidatoComando(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    @Override
    public String getComando() {
        return String.format("0001|%s,%s", codigo, nombre);
    }

    @Override
    public String getCodigoComando() {
        return "0001";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split(Pattern.quote("|"));
            if (!partes[0].equals("0001") || partes.length != 2) return false;
            
            String[] datos = partes[1].split(",");
            return datos.length == 2 && !datos[0].isEmpty() && !datos[1].isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static AltaCandidatoComando parsear(String comando) {
        try {
            String[] partes = comando.split(Pattern.quote("|"));
            String[] datos = partes[1].split(",");
            return new AltaCandidatoComando(datos[0], datos[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de comando inválido: " + e.getMessage());
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return getComando();
    }
}
