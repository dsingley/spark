package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static spark.Service.ignite;

import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.kiwiproject.reflect.KiwiReflection;
import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServers;
import spark.route.Routes;
import spark.ssl.SslStores;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class ServiceTest {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int NOT_FOUND_STATUS_CODE = HttpServletResponse.SC_NOT_FOUND;

    private Service service;

    @BeforeEach
    void setUp() {
        service = ignite();
    }

    @Test
    void testEmbeddedServerIdentifier_defaultAndSet() {
        assertThat(service.embeddedServerIdentifier()).isEqualTo(EmbeddedServers.defaultIdentifier());

        Object obj = new Object();

        service.embeddedServerIdentifier(obj);

        assertThat(service.embeddedServerIdentifier()).isEqualTo(obj);
    }

    @Test
    void testEmbeddedServerIdentifier_thenThrowIllegalStateException() {
        Object obj = new Object();

        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.embeddedServerIdentifier(obj))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testHalt_whenOutParameters_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt()).isInstanceOf(HaltException.class);
    }

    @Test
    void testHalt_whenStatusCode_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt(NOT_FOUND_STATUS_CODE)).isInstanceOf(HaltException.class);
    }

    @Test
    void testHalt_whenBodyContent_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt("error")).isInstanceOf(HaltException.class);
    }

    @Test
    void testHalt_whenStatusCodeAndBodyContent_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt(NOT_FOUND_STATUS_CODE, "error")).isInstanceOf(HaltException.class);
    }

    @Test
    void testIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = KiwiReflection.getTypedFieldValue(service, "ipAddress", String.class);
        assertThat(ipAddress).isEqualTo(IP_ADDRESS);
    }

    @Test
    void testIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.ipAddress(IP_ADDRESS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testPort_whenInitializedFalse() {
        service.port(8080);

        int port = KiwiReflection.getTypedFieldValue(service, "port", Integer.class);
        assertThat(port).isEqualTo(8080);
    }

    @Test
    void testPort_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.port(8080))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testGetPort_whenInitializedFalse_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", false);

        assertThatThrownBy(() -> service.port())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done after route mapping has begun");
    }

    @Test
    void testGetPort_whenInitializedTrue() {
        int expectedPort = 8080;
        KiwiReflection.setFieldValue(service, "initialized", true);
        KiwiReflection.setFieldValue(service, "port", expectedPort);

        int actualPort = service.port();

        assertThat(actualPort).isEqualTo(expectedPort);
    }

    @Test
    void testGetPort_whenInitializedTrue_Default() {
        int expectedPort = Service.SPARK_DEFAULT_PORT;
        KiwiReflection.setFieldValue(service, "initialized", true);

        int actualPort = service.port();

        assertThat(actualPort).isEqualTo(expectedPort);
    }

    @Test
    void testThreadPool_whenOnlyMaxThreads() {
        service.threadPool(100);
        int maxThreads = KiwiReflection.getTypedFieldValue(service, "maxThreads", Integer.class);
        int minThreads = KiwiReflection.getTypedFieldValue(service, "minThreads", Integer.class);
        int threadIdleTimeoutMillis = KiwiReflection.getTypedFieldValue(service, "threadIdleTimeoutMillis", Integer.class);
        assertAll(
                () -> assertThat(maxThreads).isEqualTo(100),
                () -> assertThat(minThreads).isEqualTo(-1),
                () -> assertThat(threadIdleTimeoutMillis).isEqualTo(-1)
        );
    }

    @Test
    void testThreadPool_whenMaxMinAndTimeoutParameters() {
        service.threadPool(100, 50, 75);
        int maxThreads = KiwiReflection.getTypedFieldValue(service, "maxThreads", Integer.class);
        int minThreads = KiwiReflection.getTypedFieldValue(service, "minThreads", Integer.class);
        int threadIdleTimeoutMillis = KiwiReflection.getTypedFieldValue(service, "threadIdleTimeoutMillis", Integer.class);
        assertAll(
                () -> assertThat(maxThreads).isEqualTo(100),
                () -> assertThat(minThreads).isEqualTo(50),
                () -> assertThat(threadIdleTimeoutMillis).isEqualTo(75)
        );
    }

    @Test
    void testThreadPool_whenMaxMinAndTimeoutParameters_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.threadPool(100, 50, 75))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testSecure_thenReturnNewSslStores() {
        service.secure("keyfile", "keypassword", "truststorefile", "truststorepassword");
        SslStores sslStores = KiwiReflection.getTypedFieldValue(service, "sslStores", SslStores.class);

        assertThat(sslStores).isNotNull();
        assertAll(
                () -> assertThat(sslStores.keystoreFile()).isEqualTo("keyfile"),
                () -> assertThat(sslStores.keystorePassword()).isEqualTo("keypassword"),
                () -> assertThat(sslStores.trustStoreFile()).isEqualTo("truststorefile"),
                () -> assertThat(sslStores.trustStorePassword()).isEqualTo("truststorepassword")
        );
    }

    @Test
    void testSecure_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.secure(null, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testSecure_whenInitializedFalse_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.secure(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must provide a keystore file to run secured");
    }

    @Test
    void testWebSocketIdleTimeoutMillis_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.webSocketIdleTimeoutMillis(100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testWebSocket_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        var handler = new DummyWebSocketHandler();
        assertThatThrownBy(() -> service.webSocket("/", handler))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    void testWebSocket_whenPathNull_thenThrowNullPointerException() {
        var handler = new DummyWebSocketHandler();
        assertThatThrownBy(() -> service.webSocket(null, handler))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("WebSocket path cannot be null");
    }

    @Test
    void testWebSocket_whenHandlerNotAnnotated_thenThrowIllegalArgumentException() {
        var handler = new DummyWebSocketListener();
        assertThatThrownBy(() -> service.webSocket("/", handler))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("WebSocket handler must be annotated as '@WebSocket'");
    }

    @Test
    void testWebSocket_whenHandlerNull_thenThrowNullPointerException() {
        assertThatThrownBy(() -> service.webSocket("/", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("WebSocket handler class cannot be null");
    }

    @Test
    void stopExtinguishesServer() {
        Service theService = Service.ignite();
        Routes routes = mock(Routes.class);
        EmbeddedServer server = mock(EmbeddedServer.class);
        theService.routes = routes;
        theService.server = server;
        theService.initialized = true;
        theService.stop();

        // polls "initialized" directly instead of calling awaitStop(), so this test verifies
        // stop() completes independently of awaitStop()'s own correctness (see the dedicated
        // awaitStop() test below)
        await().atMost(1, TimeUnit.SECONDS).until(() -> !theService.initialized);

        verify(server).extinguish();
    }

    @Test
    // 1 second was too tight in CPU-constrained environments (e.g. GitHub Codespaces sharing
    // cores with the IDE/language server): the actual work here is trivial (mocks), but stop()
    // spawns a real background thread, and awaitStop() blocks until it's scheduled and runs -
    // OS thread-scheduling latency under contention, not this test's logic, was tripping it.
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void awaitStopBlocksUntilExtinguished() {
        Service theService = Service.ignite();
        Routes routes = mock(Routes.class);
        EmbeddedServer server = mock(EmbeddedServer.class);
        theService.routes = routes;
        theService.server = server;
        theService.initialized = true;
        theService.stop();
        theService.awaitStop();
        verify(server).extinguish();
        assertThat(theService.initialized).isFalse();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void awaitStopWithTimeout_whenServerStopsInTime_returnsTrue() {
        var theService = Service.ignite();
        var routes = mock(Routes.class);
        var server = mock(EmbeddedServer.class);
        theService.routes = routes;
        theService.server = server;
        theService.initialized = true;
        theService.stop();

        var stopped = theService.awaitStop(Duration.ofSeconds(5));

        verify(server).extinguish();
        assertAll(
                () -> assertThat(stopped).isTrue(),
                () -> assertThat(theService.initialized).isFalse()
        );
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void awaitStopWithTimeout_whenServerDoesNotStopInTime_returnsFalse() {
        var theService = Service.ignite();
        var routes = mock(Routes.class);
        var server = mock(EmbeddedServer.class);
        // simulates a slow/hung shutdown, so the timeout genuinely elapses first
        doAnswer(invocation -> {
            Thread.sleep(500);
            return null;
        }).when(server).extinguish();
        theService.routes = routes;
        theService.server = server;
        theService.initialized = true;
        theService.stop();

        var stopped = theService.awaitStop(Duration.ofMillis(50));

        assertThat(stopped).isFalse();
    }

    protected static class DummyWebSocketListener {
    }

    @WebSocket
    protected static class DummyWebSocketHandler {
    }
}
