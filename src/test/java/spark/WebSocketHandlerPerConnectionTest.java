package spark;

import static java.util.Collections.newSetFromMap;
import static java.util.Collections.synchronizedSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.embeddedserver.jetty.websocket.WebSocketTestClient;
import spark.testing.SparkServerExtension;

import java.net.URI;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Checks, with a real server and real clients, which handler serves a connection: a new instance of a
 * handler class for each one, but the one instance that was given for all of them.
 */
class WebSocketHandlerPerConnectionTest {

    private static final int PORT = 6710;

    private static final Set<Object> CLASS_HANDLERS = synchronizedSet(newSetFromMap(new IdentityHashMap<>()));
    private static final Set<Object> INSTANCE_HANDLERS = synchronizedSet(newSetFromMap(new IdentityHashMap<>()));
    private static final AtomicInteger CLASS_CONNECTIONS = new AtomicInteger();
    private static final AtomicInteger INSTANCE_CONNECTIONS = new AtomicInteger();

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(PORT);
        // WebSocket handlers have to be mapped before the first route, which starts the server
        http.webSocket("/class", ClassHandler.class);
        http.webSocket("/instance", new InstanceHandler());
        http.get("/ping", (request, response) -> "pong");
    });

    @WebSocket
    public static class ClassHandler {
        @OnWebSocketOpen
        public void opened(Session session) {
            CLASS_HANDLERS.add(this);
            CLASS_CONNECTIONS.incrementAndGet();
        }
    }

    @WebSocket
    public static class InstanceHandler {
        @OnWebSocketOpen
        public void opened(Session session) {
            INSTANCE_HANDLERS.add(this);
            INSTANCE_CONNECTIONS.incrementAndGet();
        }
    }

    @BeforeEach
    void resetRecordedConnections() {
        CLASS_HANDLERS.clear();
        INSTANCE_HANDLERS.clear();
        CLASS_CONNECTIONS.set(0);
        INSTANCE_CONNECTIONS.set(0);
    }

    @Test
    void aHandlerClass_getsANewInstanceForEachConnection() throws Exception {
        connectAndClose("/class");
        connectAndClose("/class");

        assertAll(
                () -> assertThat(CLASS_CONNECTIONS).hasValue(2),
                () -> assertThat(CLASS_HANDLERS).hasSize(2)
        );
    }

    @Test
    void aHandlerInstance_isSharedByAllConnections() throws Exception {
        connectAndClose("/instance");
        connectAndClose("/instance");

        assertAll(
                () -> assertThat(INSTANCE_CONNECTIONS).hasValue(2),
                () -> assertThat(INSTANCE_HANDLERS).hasSize(1)
        );
    }

    // WebSocketTestClient sends a message as soon as it connects and then closes the connection itself,
    // so waiting for the close waits for the whole exchange to finish
    private static void connectAndClose(String path) throws Exception {
        try (var client = new WebSocketClient()) {
            var websocket = new WebSocketTestClient();
            client.start();
            client.connect(websocket, URI.create("ws://127.0.0.1:" + PORT + path)).get(10, TimeUnit.SECONDS);
            // The server has opened the connection by the time the close handshake finishes
            if (!websocket.awaitClose(10, TimeUnit.SECONDS)) {
                throw new TimeoutException("Connection to " + path + " was not closed");
            }
        }
    }
}
