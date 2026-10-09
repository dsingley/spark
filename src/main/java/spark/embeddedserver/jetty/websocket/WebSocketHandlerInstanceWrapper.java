package spark.embeddedserver.jetty.websocket;

import static java.util.Objects.requireNonNull;

/**
 * Wraps a single WebSocket handler instance, which is shared by all connections to its path, so it has
 * to be safe to use from several threads.
 */
public class WebSocketHandlerInstanceWrapper implements WebSocketHandlerWrapper {
    
    private final Object handler;
    
    /**
     * Creates a wrapper for the given handler instance.
     *
     * @param handler the handler, whose class must be annotated with {@code @WebSocket}
     * @throws NullPointerException     if the handler is null
     * @throws IllegalArgumentException if the handler's class is not annotated with {@code @WebSocket}
     */
    public WebSocketHandlerInstanceWrapper(Object handler) {
        requireNonNull(handler, "WebSocket handler cannot be null");
        WebSocketHandlerWrapper.validateHandlerClass(handler.getClass());
        this.handler = handler;
    }

    @Override
    public Object getHandler() {
        return handler;
    }

}
