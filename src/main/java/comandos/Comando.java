package comandos;

/**
 * Clase base para todos los comandos del sistema de votación
 */
public abstract class Comando {
    private String codigoComando;
    private String ip;

    public Comando() {
        this.ip = "";
    }

    /**
     * Obtiene la representación en string del comando
     * @return String con el formato específico del comando
     */
    public abstract String getComando();

    /**
     * Obtiene el código identificador del comando
     * @return String con el código del comando (ej: "0009", "0010", "0011")
     */
    public String getCodigoComando() {
        return codigoComando;
    }

    public void setCodigoComando(String codigoComando) {
        this.codigoComando = codigoComando;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
