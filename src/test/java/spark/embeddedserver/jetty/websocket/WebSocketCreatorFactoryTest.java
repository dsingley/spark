package spark.embeddedserver.jetty.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.jupiter.api.Test;
import spark.embeddedserver.jetty.websocket.WebSocketCreatorFactory.SparkWebSocketCreator;

class WebSocketCreatorFactoryTest {

    @Test
    void testCreateWebSocketCreator() {
        var webSocketCreator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));

        assertThat(webSocketCreator).isInstanceOf(SparkWebSocketCreator.class);
    }

    @Test
    void testCreateWebSocket_forAHandlerClass_returnsANewInstanceForEachConnection() throws Exception {
        var webSocketCreator =
                WebSocketCreatorFactory.create(new WebSocketHandlerClassWrapper(AnnotatedHandler.class));

        var first = webSocketCreator.createWebSocket(null, null);
        var second = webSocketCreator.createWebSocket(null, null);

        assertAll(
                () -> assertThat(first).isInstanceOf(AnnotatedHandler.class),
                () -> assertThat(second).isInstanceOf(AnnotatedHandler.class),
                () -> assertThat(second).isNotSameAs(first)
        );
    }

    @Test
    void testCreateWebSocket_forAHandlerInstance_returnsThatInstanceForEveryConnection() {
        var handler = new AnnotatedHandler();
        var webSocketCreator = WebSocketCreatorFactory.create(new WebSocketHandlerInstanceWrapper(handler));

        assertAll(
                () -> assertThat(webSocketCreator.createWebSocket(null, null)).isSameAs(handler),
                () -> assertThat(webSocketCreator.createWebSocket(null, null)).isSameAs(handler)
        );
    }

    @Test
    void testCannotCreateWithoutAWrapper() {
        assertThatNullPointerException()
                .isThrownBy(() -> WebSocketCreatorFactory.create(null))
                .withMessage("handlerWrapper cannot be null");
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
