package spark.embeddedserver.jetty.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.jupiter.api.Test;

class WebSocketHandlerClassWrapperTest {

    @Test
    void getHandler_shouldCreateANewInstanceEachTime() {
        var wrapper = new WebSocketHandlerClassWrapper(Handler.class);

        var first = wrapper.getHandler();
        var second = wrapper.getHandler();

        assertAll(
                () -> assertThat(first).isInstanceOf(Handler.class),
                () -> assertThat(second).isInstanceOf(Handler.class),
                () -> assertThat(second).isNotSameAs(first)
        );
    }

    @Test
    void shouldRequireAHandlerClass() {
        assertThatNullPointerException()
                .isThrownBy(() -> new WebSocketHandlerClassWrapper(null))
                .withMessage("WebSocket handler class cannot be null");
    }

    @Test
    void shouldRejectAClassWithoutANoArgumentConstructor_whenItIsWrapped() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new WebSocketHandlerClassWrapper(NoDefaultConstructorHandler.class))
                .withMessageStartingWith("WebSocket handler class must have a no-argument constructor: ")
                .withMessageContaining("NoDefaultConstructorHandler");
    }

    @Test
    void shouldRejectAClassWithAnInaccessibleConstructor_whenItIsWrapped() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new WebSocketHandlerClassWrapper(PrivateConstructorHandler.class))
                .withMessageStartingWith("WebSocket handler class must have an accessible no-argument constructor: ")
                .withMessageContaining("PrivateConstructorHandler");
    }

    @WebSocket
    static class Handler {
    }

    @WebSocket
    static class NoDefaultConstructorHandler {
        NoDefaultConstructorHandler(String name) {
        }
    }

    @WebSocket
    static class PrivateConstructorHandler {
        private PrivateConstructorHandler() {
        }
    }
}
