package comandos;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando 0001: Recibir lista de nodos (IPs).
 * Ejemplo de datos: "192.168.0.2,192.168.0.3,192.168.0.4"
 */
public class ListaNodosComando {
    private String datos;

    public ListaNodosComando(String datos) {
        this.datos = datos;
    }

    public void ejecutar() {
        // Parsear la lista de IPs
        String[] ips = datos.split(",");
        List<String> listaIps = new ArrayList<>();
        for (String ip : ips) {
            listaIps.add(ip.trim());
        }
        System.out.println("Se ha recibido la lista de nodos: " + listaIps);
        // Aquí podrías guardarlos en una variable estática,
        // en una clase Singleton o donde prefieras.
    }
}
