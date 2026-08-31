package spark.embeddedserver.jetty.websocket;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class WebSocketHandlerWrapperTest {

    @Test
    public void testValidateHandlerClass_whenAnnotated_thenDoesNotThrow() {
        WebSocketHandlerWrapper.validateHandlerClass(AnnotatedHandler.class);
    }

    @Test
    public void testValidateHandlerClass_whenNotAnnotated_thenThrowIllegalArgumentException() {
        try {
            WebSocketHandlerWrapper.validateHandlerClass(PlainHandler.class);
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("WebSocket handler must be annotated as '@WebSocket'", ex.getMessage());
        }
    }

    @WebSocket
    static class AnnotatedHandler {
    }

    static class PlainHandler {
    }
}
