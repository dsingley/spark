package spark.embeddedserver.jetty;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;
import org.kiwiproject.reflect.RuntimeReflectionException;
import spark.ssl.SslStores;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SocketConnectorFactoryTest {

    @Test
    public void testCreateSocketConnector_whenServerIsNull_thenThrowException() {

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SocketConnectorFactory.createSocketConnector(null, "host", 80, true))
                .withMessage("'server' must not be null");
    }


    @Test
    public void testCreateSocketConnector_whenHostIsNull_thenThrowException() {

        Server server = new Server();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SocketConnectorFactory.createSocketConnector(server, null, 80, true))
                .withMessage("'host' must not be null");
    }

    @Test
    public void testCreateSocketConnector() {

        final String host = "localhost";
        final int port = 8888;

        Server server = new Server();
        ServerConnector serverConnector = SocketConnectorFactory.createSocketConnector(server, "localhost", 8888, true);

        String internalHost = KiwiReflection.getTypedFieldValue(serverConnector, declaredField(ServerConnector.class, "_host"), String.class);
        int internalPort = KiwiReflection.getTypedFieldValue(serverConnector, declaredField(ServerConnector.class, "_port"), Integer.class);
        Server internalServerConnector = KiwiReflection.getTypedFieldValue(serverConnector, declaredField(ServerConnector.class, "_server"), Server.class);

        assertAll(
                () -> assertThat(internalHost).isEqualTo(host),
                () -> assertThat(internalPort).isEqualTo(port),
                () -> assertThat(server).isEqualTo(internalServerConnector)
        );
    }

    @Test
    public void testCreateSecureSocketConnector_whenServerIsNull() {

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SocketConnectorFactory.createSecureSocketConnector(null, "localhost", 80, (SslStores) null, true))
                .withMessage("'server' must not be null");
    }

    @Test
    public void testCreateSecureSocketConnector_whenHostIsNull() {

        Server server = new Server();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SocketConnectorFactory.createSecureSocketConnector(server, null, 80, (SslStores) null, true))
                .withMessage("'host' must not be null");
    }

    @Test
    public void testCreateSecureSocketConnector_whenSslStoresIsNull() {

        Server server = new Server();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> SocketConnectorFactory.createSecureSocketConnector(server, "localhost", 80, (SslStores) null, true))
                .withMessage("'sslStores' must not be null");
    }


    @Test
    public void testCreateSecureSocketConnector() {

        final String host = "localhost";
        final int port = 8888;

        // Jetty 12 validates that the keystore/truststore paths are accessible as soon as
        // they're set (Jetty 9 deferred this until the SslContextFactory actually started),
        // so this needs a real file rather than a placeholder name.
        final String keystoreFileName = "keystore.jks";
        final String keystoreFile = "src/test/resources/" + keystoreFileName;
        final String keystorePassword = "keystorePassword";
        final String truststoreFileName = keystoreFileName;
        final String truststoreFile = keystoreFile;
        final String trustStorePassword = "trustStorePassword";

        SslStores sslStores = SslStores.create(keystoreFile, keystorePassword, truststoreFile, trustStorePassword);

        Server server = new Server();

        ServerConnector serverConnector = SocketConnectorFactory.createSecureSocketConnector(server, host, port, sslStores, true);

        String internalHost = KiwiReflection.getTypedFieldValue(serverConnector, declaredField(ServerConnector.class, "_host"), String.class);
        int internalPort = KiwiReflection.getTypedFieldValue(serverConnector, declaredField(ServerConnector.class, "_port"), Integer.class);

        assertAll(
                () -> assertThat(internalHost).isEqualTo(host),
                () -> assertThat(internalPort).isEqualTo(port)
        );

        @SuppressWarnings("unchecked")
        Map<String, ConnectionFactory> factories = KiwiReflection.getTypedFieldValue(serverConnector, declaredField(ServerConnector.class, "_factories"), Map.class);

        SslConnectionFactory sslConnectionFactory = (SslConnectionFactory) factories.get("ssl");
        assertThat(sslConnectionFactory).isNotNull();

        SslContextFactory sslContextFactory = sslConnectionFactory.getSslContextFactory();

        assertAll(
                () -> assertThat(sslContextFactory.getKeyStoreResource().getFileName()).isEqualTo(keystoreFileName),
                () -> assertThat(sslContextFactory.getTrustStoreResource().getFileName()).isEqualTo(truststoreFileName)
        );

    }

    // _host/_port/_server/_factories are declared on AbstractConnector, a superclass of
    // ServerConnector, so KiwiReflection's own field lookup (which only looks at the target's
    // exact runtime class) can't find them; nonStaticFieldsInHierarchy() walks the hierarchy
    // for us, though it doesn't set accessibility, so that's still on us.
    private static Field declaredField(Class<?> type, String fieldName) {
        Field field = KiwiReflection.nonStaticFieldsInHierarchy(type).stream()
                .filter(f -> f.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new RuntimeReflectionException(
                        "Cannot find field [%s] in hierarchy of %s".formatted(fieldName, type),
                        new NoSuchFieldException(fieldName)));
        field.setAccessible(true);
        return field;
    }

}
