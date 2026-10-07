package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
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

        assertAll(
                () -> verifyNoInteractions(context),
                () -> assertThatIllegalStateException().isThrownBy(extension::service)
        );
    }

    @Test
    void shouldNotHaveServiceOrPort_BeforeServerIsStarted() {
        var extension = new SparkServerExtension(http -> { });

        assertAll(
                () -> assertThatIllegalStateException()
                        .isThrownBy(extension::service)
                        .withMessage("Spark is not running"),
                () -> assertThatIllegalStateException().isThrownBy(extension::port)
        );
    }

    @Test
    void shouldNotHaveServiceOrPort_BeforeStarterIsRun() {
        var starter = new SparkStarter();

        assertAll(
                () -> assertThatIllegalStateException().isThrownBy(starter::service),
                () -> assertThatIllegalStateException().isThrownBy(starter::port)
        );
    }

    @Test
    void shouldExposeServiceAndPort_AndStopThemWhenClosed() throws Exception {
        var starter = new SparkStarter().runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.port(6551);
            http.get("/ping", (request, response) -> "pong");
        });
        var service = starter.service();

        assertAll(
                () -> assertThat(starter.port()).isEqualTo(6551),
                () -> assertThat(service.port()).isEqualTo(6551)
        );

        starter.close();

        assertAll(
                () -> assertThatIllegalStateException().isThrownBy(starter::service),
                () -> assertThat(service.awaitStop(Duration.ZERO))
                        .describedAs("close() should not return until the service has stopped")
                        .isTrue()
        );
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

    @Test
    void shouldNotAllowRunSparkToBeCalledTwice() throws Exception {
        var starter = new SparkStarter();
        starter.runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.port(6557);
            http.get("/ping", (request, response) -> "pong");
        });

        try {
            assertThatIllegalStateException()
                    .isThrownBy(() -> starter.runSpark(http -> { }))
                    .withMessage("This SparkStarter has already started Spark");
        } finally {
            starter.close();
        }
    }

    @Test
    void shouldFail_WhenInitializerDoesNotDefineAnyRoutes() {
        var starter = new SparkStarter();

        try {
            assertThatIllegalStateException()
                    .isThrownBy(() -> starter.runSpark(http -> {
                        http.ipAddress("127.0.0.1");
                        http.port(6558);
                    }))
                    .withMessage("Server has not been properly initialized");
        } finally {
            starter.close();
        }
    }

    @Test
    void shouldInjectSparkStarter_OnlyWhenCreatedWithoutAnInitializer() throws Exception {
        var method = SparkServerExtensionUnitTest.class.getDeclaredMethod("methodWithStarter", SparkStarter.class);
        var parameterContext = mock(ParameterContext.class);
        when(parameterContext.getParameter()).thenReturn(method.getParameters()[0]);
        var extensionContext = mock(ExtensionContext.class);

        assertAll(
                () -> assertThat(new SparkServerExtension().supportsParameter(parameterContext, extensionContext))
                        .isTrue(),
                () -> assertThat(new SparkServerExtension(http -> { }).supportsParameter(parameterContext, extensionContext))
                        .isFalse()
        );
    }

    @SuppressWarnings("unused")
    private void methodWithStarter(SparkStarter starter) {
        // only used to get a SparkStarter parameter by reflection
    }
}
