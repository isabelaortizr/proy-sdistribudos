package comandos;

import java.util.HashSet;
import java.util.Set;
import planificador.PlanificadorSalida;

/**
 * Comando 0001: Lista de Nodos
 * Formato: 0001|ip1,ip2,ip3,...
 */
public class ListaNodosComando extends Comando {
    private final String[] ips;
    private static PlanificadorSalida planificadorSalida;

    public ListaNodosComando(String[] ips) {
        this.ips = ips;
    }

    public static void setPlanificadorSalida(PlanificadorSalida ps) {
        planificadorSalida = ps;
    }

    @Override
    public String getComando() {
        return String.format("0001|%s", String.join(",", ips));
    }

    @Override
    public String getCodigoComando() {
        return "0001";
    }

    public static boolean validarFormato(String comando) {
        try {
            String[] partes = comando.split("\\|");
            return partes[0].equals("0001") && partes.length == 2;
        } catch (Exception e) {
            return false;
        }
    }

    public static ListaNodosComando parsear(String comando) {
        if (!validarFormato(comando)) {
            throw new IllegalArgumentException("Formato de comando inválido");
        }
        String[] partes = comando.split("\\|");
        String[] ips = partes[1].isEmpty() ? new String[0] : partes[1].split(",");
        return new ListaNodosComando(ips);
    }

    public String[] getIps() {
        return ips;
    }

    public Set<String> obtenerIpsUnicas() {
        Set<String> ipsUnicas = new HashSet<>();
        
        for (String ip : ips) {
            ip = ip.trim();
            if (!ip.isEmpty()) {
                ipsUnicas.add(ip);
            }
        }
        return ipsUnicas;
    }

    public void actualizarPlanificador() {
        if (planificadorSalida != null) {
            Set<String> ips = obtenerIpsUnicas();
            System.out.println("Lista de IPs únicas recibidas: " + ips);
            for (String ip : ips) {
                planificadorSalida.agregarNodo(ip);
            }
        }
    }
}
