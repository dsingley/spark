package spark;

import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.kiwiproject.reflect.KiwiReflection;
import org.mockito.Mockito;

import spark.embeddedserver.EmbeddedServer;
import spark.embeddedserver.EmbeddedServers;
import spark.route.Routes;
import spark.ssl.SslStores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static spark.Service.ignite;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ServiceTest {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int NOT_FOUND_STATUS_CODE = HttpServletResponse.SC_NOT_FOUND;

    private Service service;

    @BeforeEach
    public void setUp() {
        service = ignite();
    }

    @Test
    public void testEmbeddedServerIdentifier_defaultAndSet() {
        assertThat(service.embeddedServerIdentifier()).isEqualTo(EmbeddedServers.defaultIdentifier());

        Object obj = new Object();

        service.embeddedServerIdentifier(obj);

        assertThat(service.embeddedServerIdentifier()).isEqualTo(obj);
    }

    @Test
    public void testEmbeddedServerIdentifier_thenThrowIllegalStateException() {
        Object obj = new Object();

        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.embeddedServerIdentifier(obj))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testHalt_whenOutParameters_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt()).isInstanceOf(HaltException.class);
    }

    @Test
    public void testHalt_whenStatusCode_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt(NOT_FOUND_STATUS_CODE)).isInstanceOf(HaltException.class);
    }

    @Test
    public void testHalt_whenBodyContent_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt("error")).isInstanceOf(HaltException.class);
    }

    @Test
    public void testHalt_whenStatusCodeAndBodyContent_thenThrowHaltException() {
        assertThatThrownBy(() -> service.halt(NOT_FOUND_STATUS_CODE, "error")).isInstanceOf(HaltException.class);
    }

    @Test
    public void testIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = KiwiReflection.getTypedFieldValue(service, "ipAddress", String.class);
        assertThat(ipAddress).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void testIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.ipAddress(IP_ADDRESS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testSetIpAddress_whenInitializedFalse() {
        service.ipAddress(IP_ADDRESS);

        String ipAddress = KiwiReflection.getTypedFieldValue(service, "ipAddress", String.class);
        assertThat(ipAddress).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void testSetIpAddress_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.ipAddress(IP_ADDRESS))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testPort_whenInitializedFalse() {
        service.port(8080);

        int port = KiwiReflection.getTypedFieldValue(service, "port", Integer.class);
        assertThat(port).isEqualTo(8080);
    }

    @Test
    public void testPort_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.port(8080))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testSetPort_whenInitializedFalse() {
        service.port(8080);

        int port = KiwiReflection.getTypedFieldValue(service, "port", Integer.class);
        assertThat(port).isEqualTo(8080);
    }

    @Test
    public void testSetPort_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.port(8080))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testGetPort_whenInitializedFalse_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", false);

        assertThatThrownBy(() -> service.port())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done after route mapping has begun");
    }

    @Test
    public void testGetPort_whenInitializedTrue() {
        int expectedPort = 8080;
        KiwiReflection.setFieldValue(service, "initialized", true);
        KiwiReflection.setFieldValue(service, "port", expectedPort);

        int actualPort = service.port();

        assertThat(actualPort).isEqualTo(expectedPort);
    }

    @Test
    public void testGetPort_whenInitializedTrue_Default() {
        int expectedPort = Service.SPARK_DEFAULT_PORT;
        KiwiReflection.setFieldValue(service, "initialized", true);

        int actualPort = service.port();

        assertThat(actualPort).isEqualTo(expectedPort);
    }

    @Test
    public void testThreadPool_whenOnlyMaxThreads() {
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
    public void testThreadPool_whenMaxMinAndTimeoutParameters() {
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
    public void testThreadPool_whenMaxMinAndTimeoutParameters_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.threadPool(100, 50, 75))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testSecure_thenReturnNewSslStores() {
        service.secure("keyfile", "keypassword", "truststorefile", "truststorepassword");
        SslStores sslStores = KiwiReflection.getTypedFieldValue(service, "sslStores", SslStores.class);
        assertAll(
                () -> assertThat(sslStores).isNotNull(),
                () -> assertThat(sslStores.keystoreFile()).isEqualTo("keyfile"),
                () -> assertThat(sslStores.keystorePassword()).isEqualTo("keypassword"),
                () -> assertThat(sslStores.trustStoreFile()).isEqualTo("truststorefile"),
                () -> assertThat(sslStores.trustStorePassword()).isEqualTo("truststorepassword")
        );
    }

    @Test
    public void testSecure_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.secure(null, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testSecure_whenInitializedFalse_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.secure(null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Must provide a keystore file to run secured");
    }

    @Test
    public void testWebSocketIdleTimeoutMillis_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.webSocketIdleTimeoutMillis(100))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testWebSocket_whenInitializedTrue_thenThrowIllegalStateException() {
        KiwiReflection.setFieldValue(service, "initialized", true);

        assertThatThrownBy(() -> service.webSocket("/", new DummyWebSocketHandler()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This must be done before route mapping has begun");
    }

    @Test
    public void testWebSocket_whenPathNull_thenThrowNullPointerException() {
        assertThatThrownBy(() -> service.webSocket(null, new DummyWebSocketHandler()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("WebSocket path cannot be null");
    }

    @Test
    public void testWebSocket_whenHandlerNotAnnotated_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> service.webSocket("/", new DummyWebSocketListener()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("WebSocket handler must be annotated as '@WebSocket'");
    }

    @Test
    public void testWebSocket_whenHandlerNull_thenThrowNullPointerException() {
        assertThatThrownBy(() -> service.webSocket("/", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("WebSocket handler class cannot be null");
    }

    @Test
    @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
    public void stopExtinguishesServer() {
        Service service = Service.ignite();
        Routes routes = Mockito.mock(Routes.class);
        EmbeddedServer server = Mockito.mock(EmbeddedServer.class);
        service.routes = routes;
        service.server = server;
        service.initialized = true;
        service.stop();
        try {
        	// yes, this is ugly and forces to set a test timeout as a precaution :(
            while (service.initialized) {
            	Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Mockito.verify(server).extinguish();
    }

    @Test
    public void awaitStopBlocksUntilExtinguished() {
        Service service = Service.ignite();
        Routes routes = Mockito.mock(Routes.class);
        EmbeddedServer server = Mockito.mock(EmbeddedServer.class);
        service.routes = routes;
        service.server = server;
        service.initialized = true;
        service.stop();
        service.awaitStop();
        Mockito.verify(server).extinguish();
        assertThat(service.initialized).isFalse();
    }

    protected static class DummyWebSocketListener {
    }

    @WebSocket
    protected static class DummyWebSocketHandler {
    }
}
