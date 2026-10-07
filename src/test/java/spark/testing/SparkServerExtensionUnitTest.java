package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import spark.Service;
import spark.testing.SparkServerExtension.SparkStarter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

class SparkServerExtensionUnitTest {

    @Test
    void shouldRequireAnInitializer() {
        assertThatNullPointerException()
                .isThrownBy(() -> new SparkServerExtension(null))
                .withMessage("initializer must not be null");
    }

    @Test
    void shouldNotStartAServer_WhenCreatedWithoutAnInitializer() {
        var extension = new SparkServerExtension();
        var context = mock(ExtensionContext.class);

        assertThatCode(() -> {
            extension.beforeAll(context);
            extension.beforeEach(context);
        }).doesNotThrowAnyException();

        verifyNoInteractions(context);
        assertThatIllegalStateException().isThrownBy(extension::service);
    }

    @Test
    void shouldNotHaveServiceOrPort_BeforeServerIsStarted() {
        var extension = new SparkServerExtension(http -> { });

        assertThatIllegalStateException()
                .isThrownBy(extension::service)
                .withMessage("Spark is not running");
        assertThatIllegalStateException().isThrownBy(extension::port);
    }

    @Test
    void shouldNotHaveServiceOrPort_BeforeStarterIsRun() {
        var starter = new SparkStarter();

        assertThatIllegalStateException().isThrownBy(starter::service);
        assertThatIllegalStateException().isThrownBy(starter::port);
    }

    @Test
    void shouldExposeServiceAndPort_AndStopThemWhenClosed() throws Exception {
        var starter = new SparkStarter().runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.port(6551);
            http.get("/ping", (request, response) -> "pong");
        });
        var service = starter.service();

        assertThat(starter.port()).isEqualTo(6551);
        assertThat(service.port()).isEqualTo(6551);

        starter.close();

        assertThatIllegalStateException().isThrownBy(starter::service);
        assertThat(service.awaitStop(Duration.ofSeconds(5))).isTrue();
    }

    @Test
    void shouldRethrowCheckedExceptionFromInitializer_AndStillStopServerWhenClosed() {
        var starter = new SparkStarter();
        var ignited = new AtomicReference<Service>();

        assertThatThrownBy(() -> starter.runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.port(6556);
            http.get("/ping", (request, response) -> "pong");
            http.awaitInitialization();
            ignited.set(http);
            throw new IOException("could not load keystore");
        }))
                .isExactlyInstanceOf(IOException.class)
                .hasMessage("could not load keystore");

        starter.close();

        assertThat(ignited.get().awaitStop(Duration.ofSeconds(5))).isTrue();
    }
}
