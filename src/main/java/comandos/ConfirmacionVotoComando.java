package comandos;

import java.util.regex.Pattern;
import planificador.PlanificadorPresidenteMesa;

/**
 * Comando 0010: Confirmación de Voto
 * Formato: 0010|id_voto,confirmado,ip_origen
 */
public class ConfirmacionVotoComando extends Comando {
    private final String idVoto;
    private final boolean confirmado;

    public ConfirmacionVotoComando(String idVoto, boolean confirmado) {
        super();
        this.setCodigoComando("0010");
        this.idVoto = idVoto;
        this.confirmado = confirmado;
    }

    public ConfirmacionVotoComando(String idVoto, boolean confirmado, String ipNodo) {
        super();
        this.setCodigoComando("0010");
        this.idVoto = idVoto;
        this.confirmado = confirmado;
        this.setIp(ipNodo);
    }

    @Override
    public String getComando() {
        return String.format("%s|%s,%b,%s",
                this.getCodigoComando(),
                this.idVoto,
                this.confirmado,
                this.getIp()  // Se agrega el ip_origen
        );
    }

    public String getIdVoto() {
        return idVoto;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public static boolean validarFormato(String mensaje) {
        String[] partes = mensaje.split("\\|");
        if (partes.length != 2) return false;

        String[] datos = partes[1].split(",");
        return datos.length == 3;
    }

    public static ConfirmacionVotoComando parsear(String mensaje) {
        String[] partes = mensaje.split("\\|");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        String[] datos = partes[1].split(",");
        if (datos.length == 2) {
            // Si solo se reciben dos campos, se puede asignar un valor predeterminado para ip.
            return new ConfirmacionVotoComando(
                    datos[0],
                    Boolean.parseBoolean(datos[1]),
                    "0.0.0.0"  // Valor por defecto o puedes manejarlo de otra forma.
            );
        } else if (datos.length == 3) {
            return new ConfirmacionVotoComando(
                    datos[0],
                    Boolean.parseBoolean(datos[1]),
                    datos[2]
            );
        } else {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
    }
} 