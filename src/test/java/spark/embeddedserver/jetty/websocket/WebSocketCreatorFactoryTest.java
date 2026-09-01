package spark.embeddedserver.jetty.websocket;

import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import spark.embeddedserver.jetty.websocket.WebSocketCreatorFactory.SparkWebSocketCreator;
import static org.junit.jupiter.api.Assertions.assertAll;

public class WebSocketCreatorFactoryTest {

    @Test
    public void testCreateWebSocketHandler() {
        JettyWebSocketCreator creator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));
        assertAll(
                () -> assertThat(creator).isInstanceOf(SparkWebSocketCreator.class),
                () -> assertThat(SparkWebSocketCreator.class.cast(creator).getHandler()).isInstanceOf(AnnotatedHandler.class)
        );
    }

    @Test
    public void testCreateWebSocket_alwaysReturnsSameHandlerInstance() throws Exception {
        JettyWebSocketCreator creator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));
        Object handler = SparkWebSocketCreator.class.cast(creator).getHandler();

        assertAll(
                () -> assertThat(creator.createWebSocket(null, null)).isSameAs(handler),
                () -> assertThat(creator.createWebSocket(null, null)).isSameAs(handler)
        );
    }

    @Test
    public void testCannotCreateInvalidHandlers() {
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
