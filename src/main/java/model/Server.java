import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PUERTO = 1825;
    private static final List<String> listaNodos = new ArrayList<>();
    private static final boolean isNodoPrincipal = true; // Flag que define que este es el nodo principal

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("✅ Nodo Principal activo en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                String ipCliente = cliente.getInetAddress().getHostAddress();

                System.out.println("🔗 Nuevo nodo conectado: " + ipCliente);
                listaNodos.add(ipCliente); // Agregar la IP del nodo nuevo a la lista

                new Thread(new ManejadorNodo(cliente, ipCliente)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ManejadorNodo implements Runnable {
        private Socket cliente;
        private String ipNodo;

        public ManejadorNodo(Socket cliente, String ipNodo) {
            this.cliente = cliente;
            this.ipNodo = ipNodo;
        }

        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                 PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true)) {

                String mensaje = entrada.readLine(); // Leer el comando enviado por el nodo nuevo
                if (isNodoPrincipal && "0001".equals(mensaje)) { // Solo el Nodo Principal puede interpretar este comando
                    String respuesta = generarCodigoConexion();
                    System.out.println("📤 Enviando lista de nodos a " + ipNodo + ": " + respuesta);
                    salida.println(respuesta);
                } else {
                    System.out.println("❌ Comando no válido de " + ipNodo);
                    salida.println("ERROR: Comando no autorizado");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String generarCodigoConexion() {
            return "0001|" + String.join(";", listaNodos);
        }
    }
}
