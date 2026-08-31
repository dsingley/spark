package spark.embeddedserver.jetty.websocket;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * A wrapper for web socket handler classes/instances.
 */
public interface WebSocketHandlerWrapper {

    /**
     * Gets the actual handler - if necessary, instantiating an object.
     *
     * @return The handler instance.
     */
    Object getHandler();

    static void validateHandlerClass(Class<?> handlerClass) {
        // Jetty 12 removed the WebSocketListener interface entirely (see
        // https://github.com/sleberknight/spark/issues/12); the annotated style is now the
        // only supported way to write a WebSocket handler.
        if (!handlerClass.isAnnotationPresent(WebSocket.class)) {
            throw new IllegalArgumentException("WebSocket handler must be annotated as '@WebSocket'");
        }
    }

}
