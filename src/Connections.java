import java.io.IOException;

/**
 * This interface should map a unique ID for each active client connected to the server. The implementation of Connections is part of the server
 * pattern and not part of the protocol.
 *
 */

public interface Connections<T> {
    //Send a message to the client of connectionId
    boolean send(int connectionId, T msg);

    //Send a message to all active clients - including those who have not logged-in
    void broadcast(T msg);

    //Remove the client from the map
    void disconnect(int connectionId);
}
