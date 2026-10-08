package spark.embeddedserver.jetty.websocket;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a WebSocket handler class. A new handler instance is created each time
 * {@link #getHandler()} is called, so each connection gets its own handler.
 */
public class WebSocketHandlerClassWrapper implements WebSocketHandlerWrapper {
    
    private final Class<?> handlerClass;

    /**
     * Creates a wrapper for the given handler class.
     *
     * @param handlerClass the handler class, which must be annotated with {@code @WebSocket} and have
     *                     a no-argument constructor
     * @throws NullPointerException     if the class is null
     * @throws IllegalArgumentException if the class is not annotated with {@code @WebSocket}
     */
    public WebSocketHandlerClassWrapper(Class<?> handlerClass) {
        requireNonNull(handlerClass, "WebSocket handler class cannot be null");
        WebSocketHandlerWrapper.validateHandlerClass(handlerClass);
        this.handlerClass = handlerClass;
    }
    @Override
    public Object getHandler() {
        try {
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Could not instantiate websocket handler", ex);
        }
    }

}
