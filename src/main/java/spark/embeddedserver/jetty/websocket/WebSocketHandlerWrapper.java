package spark.embeddedserver.jetty.websocket;

/**
 * A wrapper for web socket handler classes/instances.
 *
 * <p><b>Temporarily stubbed during the Jetty 9 → 12 migration.</b> Jetty 12 removed the
 * websocket API this validation used to check against ({@code org.eclipse.jetty.websocket.api});
 * the EE11 replacement is being ported separately (see the Phase 3 tracking issue). Until
 * that lands, any attempt to register a WebSocket handler fails fast here rather than
 * silently doing nothing.
 */
public interface WebSocketHandlerWrapper {

    /**
     * Gets the actual handler - if necessary, instantiating an object.
     *
     * @return The handler instance.
     */
    Object getHandler();

    static void validateHandlerClass(Class<?> handlerClass) {
        throw new UnsupportedOperationException(
                "WebSocket support is being migrated to Jetty 12's EE11 websocket API and is "
                        + "temporarily unavailable. See the Phase 3 tracking issue in the Jetty 12 migration.");
    }

}
