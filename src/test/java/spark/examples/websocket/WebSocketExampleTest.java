package spark.examples.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

class WebSocketExampleTest {

    @WebSocket
    public static class EchoingClient {
        private final String messageToSend;
        private final BlockingQueue<String> received = new ArrayBlockingQueue<>(10);

        EchoingClient(String messageToSend) {
            this.messageToSend = messageToSend;
        }

        @OnWebSocketOpen
        public void onOpen(Session session) {
            session.sendText(messageToSend, Callback.from(() -> { }, Throwable::printStackTrace));
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            received.add(message);
        }

        String awaitMessage() throws InterruptedException {
            return received.poll(10, TimeUnit.SECONDS);
        }
    }

    @BeforeAll
    static void beforeAll() {
        WebSocketExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void echoesMessage() throws Exception {
        WebSocketClient client = new WebSocketClient();
        EchoingClient echoingClient = new EchoingClient("hello ws");
        try {
            client.start();
            client.connect(echoingClient, URI.create("ws://localhost:4567/echo"));
            assertThat(echoingClient.awaitMessage()).isEqualTo("hello ws");
        } finally {
            client.stop();
        }
    }

    @Test
    void respondsToPing() throws Exception {
        WebSocketClient client = new WebSocketClient();
        EchoingClient echoingClient = new EchoingClient("PING");
        try {
            client.start();
            client.connect(echoingClient, URI.create("ws://localhost:4567/ping"));
            assertThat(echoingClient.awaitMessage()).isEqualTo("PONG");
        } finally {
            client.stop();
        }
    }
}
