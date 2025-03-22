package Event;

import model.SocketClient;
import comandos.Comando;

/**
 * Interfaz para eventos de socket.
 */
public interface SocketEvent {
    void onNewNodo(SocketClient client);
    void onCloseNodo(SocketClient client);
    void onMessage(Comando comando);
    void onMessage(String message);
    void onError(String error);
}
