package spark.embeddedserver.jetty.websocket;

import static java.util.Collections.synchronizedList;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records the events of each WebSocket connection separately, by the port the client connected from.
 * <p>
 * A new handler instance is created for each connection but they all record into one static map, so
 * a test looks only at the events of its own connection: see {@link #eventsFor(int)}. That also keeps
 * a stray client, such as one that happens to connect to the test server's port, from changing what a
 * test sees.
 */
@WebSocket
public class WebSocketTestHandler {

    private static final Map<Integer, List<String>> EVENTS_BY_CLIENT_PORT = new ConcurrentHashMap<>();

    /**
     * @param clientPort the port the client connected from, see {@link WebSocketTestClient#getLocalPort()}
     * @return the events of that client's connection so far, in order
     */
    public static List<String> eventsFor(int clientPort) {
        return List.copyOf(EVENTS_BY_CLIENT_PORT.getOrDefault(clientPort, List.of()));
    }

    @OnWebSocketOpen
    public void connected(Session session) {
        record(session, "onConnect");
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        record(session, String.format("onClose: %s %s", statusCode, reason));
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        record(session, "onMessage: " + message);
    }

    private static void record(Session session, String event) {
        var clientPort = ((InetSocketAddress) session.getRemoteSocketAddress()).getPort();
        EVENTS_BY_CLIENT_PORT.computeIfAbsent(clientPort, port -> synchronizedList(new ArrayList<>())).add(event);
    }
}
