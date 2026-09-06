package spark.embeddedserver.jetty.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.jupiter.api.Test;
import spark.embeddedserver.jetty.websocket.WebSocketCreatorFactory.SparkWebSocketCreator;

class WebSocketCreatorFactoryTest {

    @Test
    void testCreateWebSocketHandler() {
        JettyWebSocketCreator creator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));
        assertAll(
                () -> assertThat(creator).isInstanceOf(SparkWebSocketCreator.class),
                () -> assertThat(SparkWebSocketCreator.class.cast(creator).getHandler()).isInstanceOf(AnnotatedHandler.class)
        );
    }

    @Test
    void testCreateWebSocket_alwaysReturnsSameHandlerInstance() {
        JettyWebSocketCreator creator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));
        Object handler = SparkWebSocketCreator.class.cast(creator).getHandler();

        assertAll(
                () -> assertThat(creator.createWebSocket(null, null)).isSameAs(handler),
                () -> assertThat(creator.createWebSocket(null, null)).isSameAs(handler)
        );
    }

    @Test
    void testCannotCreateInvalidHandlers() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(InvalidHandler.class)))
                .withMessage("WebSocket handler must be annotated as '@WebSocket'");
    }

    @WebSocket
    static class AnnotatedHandler {
    }

    static class InvalidHandler {
    }
}
