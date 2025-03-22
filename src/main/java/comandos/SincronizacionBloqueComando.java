package comandos;

/**
 * Comando 0011: Sincronización de Bloque
 * Formato: 0011|id_bloque,ref_anterior,timestamp,datos_bloque
 */
public class SincronizacionBloqueComando extends Comando {
    private final String idBloque;
    private final String refAnterior;
    private final long timestamp;
    private final String datosBloque;

    public SincronizacionBloqueComando(String idBloque, String refAnterior, long timestamp, String datosBloque) {
        this.idBloque = idBloque;
        this.refAnterior = refAnterior;
        this.timestamp = timestamp;
        this.datosBloque = datosBloque;
    }

    @Override
    public String getComando() {
        return String.format("0011|%s,%s,%d,%s", 
            idBloque,
            refAnterior,
            timestamp,
            datosBloque
        );
    }

    @Override
    public String getCodigoComando() {
        return "0011";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            if (!partes[0].equals("0011") || partes.length != 2) return false;
            
            String[] datos = partes[1].split(",");
            return datos.length == 4 && 
                   !datos[0].trim().isEmpty() && // idBloque
                   !datos[1].trim().isEmpty() && // refAnterior
                   datos[2].trim().matches("\\d+") && // timestamp
                   !datos[3].trim().isEmpty(); // datosBloque
        } catch (Exception e) {
            return false;
        }
    }

    public static SincronizacionBloqueComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        
        String[] partes = comando.split("\\|");
        String[] datos = partes[1].split(",");
        
        return new SincronizacionBloqueComando(
            datos[0].trim(),
            datos[1].trim(),
            Long.parseLong(datos[2].trim()),
            datos[3].trim()
        );
    }

    // Getters
    public String getIdBloque() {
        return idBloque;
    }

    public String getRefAnterior() {
        return refAnterior;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDatosBloque() {
        return datosBloque;
    }
} 