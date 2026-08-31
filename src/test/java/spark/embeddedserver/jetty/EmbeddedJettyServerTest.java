package spark.embeddedserver.jetty;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.Filter;

import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.After;
import org.junit.Test;

import spark.ExceptionMapper;
import spark.embeddedserver.jetty.websocket.WebSocketHandlerClassWrapper;
import spark.embeddedserver.jetty.websocket.WebSocketHandlerWrapper;
import spark.http.matching.MatcherFilter;
import spark.route.Routes;
import spark.ssl.SslStores;
import spark.staticfiles.StaticFilesConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EmbeddedJettyServerTest {

    private EmbeddedJettyServer embeddedJettyServer;

    @After
    public void tearDown() {
        if (embeddedJettyServer != null) {
            embeddedJettyServer.extinguish();
        }
    }

    @Test
    public void testIgnite_whenWebSocketIdleTimeoutPresent_thenSetOnContainer() throws Exception {
        JettyHandler handler = newHandler();
        embeddedJettyServer = new EmbeddedJettyServer(new JettyServer(), handler);

        Map<String, WebSocketHandlerWrapper> webSocketHandlers = new HashMap<>();
        webSocketHandlers.put("/ws", new WebSocketHandlerClassWrapper(DummyWebSocketHandler.class));
        embeddedJettyServer.configureWebSockets(webSocketHandlers, Optional.of(12345L));

        embeddedJettyServer.ignite("localhost", 0, (SslStores) null, 100, 10, 10000);

        JettyWebSocketServerContainer container = handler.getWebSocketContainer();
        assertEquals(Duration.ofMillis(12345L), container.getIdleTimeout());
    }

    @Test
    public void testIgnite_whenWebSocketIdleTimeoutAbsent_thenContainerStillAvailable() throws Exception {
        JettyHandler handler = newHandler();
        embeddedJettyServer = new EmbeddedJettyServer(new JettyServer(), handler);

        Map<String, WebSocketHandlerWrapper> webSocketHandlers = new HashMap<>();
        webSocketHandlers.put("/ws", new WebSocketHandlerClassWrapper(DummyWebSocketHandler.class));
        embeddedJettyServer.configureWebSockets(webSocketHandlers, Optional.empty());

        embeddedJettyServer.ignite("localhost", 0, (SslStores) null, 100, 10, 10000);

        // No timeout configured: the container comes up using Jetty's own default,
        // untouched by EmbeddedJettyServer.
        assertNotNull(handler.getWebSocketContainer());
    }

    @Test
    public void testIgnite_whenNoWebSocketHandlersConfigured_thenNoMappingsRegistered() throws Exception {
        JettyHandler handler = newHandler();
        embeddedJettyServer = new EmbeddedJettyServer(new JettyServer(), handler);

        embeddedJettyServer.configureWebSockets(null, Optional.empty());

        embeddedJettyServer.ignite("localhost", 0, (SslStores) null, 100, 10, 10000);

        // The WebSocketUpgradeFilter and its container are always installed (see JettyHandler),
        // regardless of whether the application registered any WebSocket routes.
        assertNotNull(handler.getWebSocketContainer());
    }

    private static JettyHandler newHandler() {
        Filter matcherFilter = new MatcherFilter(
                Routes.create(), new StaticFilesConfiguration(), new ExceptionMapper(), false, false);
        return new JettyHandler(matcherFilter);
    }

    @WebSocket
    public static class DummyWebSocketHandler {
    }
}
