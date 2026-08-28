package spark.embeddedserver.jetty.websocket;

import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.Test;

import spark.embeddedserver.jetty.websocket.WebSocketCreatorFactory.SparkWebSocketCreator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WebSocketCreatorFactoryTest {

    @Test
    public void testCreateWebSocketHandler() {
        JettyWebSocketCreator creator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));
        assertTrue(creator instanceof SparkWebSocketCreator);
        assertTrue(SparkWebSocketCreator.class.cast(creator).getHandler() instanceof AnnotatedHandler);
    }

    @Test
    public void testCreateWebSocket_alwaysReturnsSameHandlerInstance() throws Exception {
        JettyWebSocketCreator creator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));
        Object handler = SparkWebSocketCreator.class.cast(creator).getHandler();

        assertSame(handler, creator.createWebSocket(null, null));
        assertSame(handler, creator.createWebSocket(null, null));
    }

    @Test
    public void testCannotCreateInvalidHandlers() {
        try {
            WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(InvalidHandler.class));
            fail("Handler creation should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("WebSocket handler must be annotated as '@WebSocket'", ex.getMessage());
        }
    }

    @WebSocket
    static class AnnotatedHandler {
    }

    static class InvalidHandler {
    }
}
