package Event;

/**
 * Ejemplo de clase para modelar un evento de socket.
 * Puedes ampliarlo con más campos si lo requieres.
 */
public class SocketEvent {
    private String mensaje;

    public SocketEvent(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
