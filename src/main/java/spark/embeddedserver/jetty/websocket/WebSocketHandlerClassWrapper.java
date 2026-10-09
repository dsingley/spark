package spark.embeddedserver.jetty.websocket;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Modifier;

/**
 * Wraps a WebSocket handler class. A new handler instance is created each time
 * {@link #getHandler()} is called, which Spark does for each new connection, so each connection gets its
 * own handler.
 */
public class WebSocketHandlerClassWrapper implements WebSocketHandlerWrapper {
    
    private final Class<?> handlerClass;

    /**
     * Creates a wrapper for the given handler class.
     *
     * @param handlerClass the handler class, which must be annotated with {@code @WebSocket} and have
     *                     an accessible no-argument constructor, and cannot be abstract
     * @throws NullPointerException     if the class is null
     * @throws IllegalArgumentException if the class is not annotated with {@code @WebSocket}, is abstract,
     *                                  or has no accessible no-argument constructor
     */
    public WebSocketHandlerClassWrapper(Class<?> handlerClass) {
        requireNonNull(handlerClass, "WebSocket handler class cannot be null");
        WebSocketHandlerWrapper.validateHandlerClass(handlerClass);
        validateHasAccessibleNoArgConstructor(handlerClass);
        this.handlerClass = handlerClass;
    }

    // Handlers are created when connections arrive, so check now, when the handler is registered, that
    // they can be, instead of failing the first time a client connects
    private static void validateHasAccessibleNoArgConstructor(Class<?> handlerClass) {
        if (Modifier.isAbstract(handlerClass.getModifiers())) {
            throw new IllegalArgumentException(
                    "WebSocket handler class cannot be abstract: " + handlerClass.getName());
        }
        try {
            var constructor = handlerClass.getDeclaredConstructor();
            if (!constructor.canAccess(null)) {
                throw new IllegalArgumentException(
                        "WebSocket handler class must have an accessible no-argument constructor: " + handlerClass.getName());
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    "WebSocket handler class must have a no-argument constructor: " + handlerClass.getName(), e);
        }
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
