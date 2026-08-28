package spark.embeddedserver.jetty;

import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import spark.ssl.SslStores;

import java.util.Map;

import static org.junit.Assert.*;

public class SocketConnectorFactoryTest {

    @Test
    public void testCreateSocketConnector_whenServerIsNull_thenThrowException() {

        try {
            SocketConnectorFactory.createSocketConnector(null, "host", 80, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch(IllegalArgumentException ex) {
            assertEquals("'server' must not be null", ex.getMessage());
        }
    }


    @Test
    public void testCreateSocketConnector_whenHostIsNull_thenThrowException() {

        Server server = new Server();

        try {
            SocketConnectorFactory.createSocketConnector(server, null, 80, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch(IllegalArgumentException ex) {
            assertEquals("'host' must not be null", ex.getMessage());
        }
    }

    @Test
    public void testCreateSocketConnector() {

        final String host = "localhost";
        final int port = 8888;

        Server server = new Server();
        ServerConnector serverConnector = SocketConnectorFactory.createSocketConnector(server, "localhost", 8888, true);

        String internalHost = Whitebox.getInternalState(serverConnector, "_host");
        int internalPort = Whitebox.getInternalState(serverConnector, "_port");
        Server internalServerConnector = Whitebox.getInternalState(serverConnector, "_server");

        assertEquals("Server Connector Host should be set to the specified server", host, internalHost);
        assertEquals("Server Connector Port should be set to the specified port", port, internalPort);
        assertEquals("Server Connector Server should be set to the specified server", internalServerConnector, server);
    }

    @Test
    public void testCreateSecureSocketConnector_whenServerIsNull() {

        try {
            SocketConnectorFactory.createSecureSocketConnector(null, "localhost", 80, (SslStores) null, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch(IllegalArgumentException ex) {
            assertEquals("'server' must not be null", ex.getMessage());
        }
    }

    @Test
    public void testCreateSecureSocketConnector_whenHostIsNull() {

        Server server = new Server();

        try {
            SocketConnectorFactory.createSecureSocketConnector(server, null, 80, (SslStores) null, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch(IllegalArgumentException ex) {
            assertEquals("'host' must not be null", ex.getMessage());
        }
    }

    @Test
    public void testCreateSecureSocketConnector_whenSslStoresIsNull() {

        Server server = new Server();

        try {
            SocketConnectorFactory.createSecureSocketConnector(server, "localhost", 80, (SslStores) null, true);
            fail("SocketConnector creation should have thrown an IllegalArgumentException");
        } catch(IllegalArgumentException ex) {
            assertEquals("'sslStores' must not be null", ex.getMessage());
        }
    }


    @Test
    @PrepareForTest({ServerConnector.class})
    public void testCreateSecureSocketConnector() throws  Exception {

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

        String internalHost = Whitebox.getInternalState(serverConnector, "_host");
        int internalPort = Whitebox.getInternalState(serverConnector, "_port");

        assertEquals("Server Connector Host should be set to the specified server", host, internalHost);
        assertEquals("Server Connector Port should be set to the specified port", port, internalPort);

        Map<String, ConnectionFactory> factories = Whitebox.getInternalState(serverConnector, "_factories");

        assertTrue("Should return true because factory for SSL should have been set",
                factories.containsKey("ssl") && factories.get("ssl") != null);

        SslConnectionFactory sslConnectionFactory = (SslConnectionFactory) factories.get("ssl");
        SslContextFactory sslContextFactory = sslConnectionFactory.getSslContextFactory();

        assertEquals("Should return the Keystore file specified", keystoreFileName,
                sslContextFactory.getKeyStoreResource().getFileName());

        assertEquals("Should return the Truststore file specified", truststoreFileName,
                sslContextFactory.getTrustStoreResource().getFileName());

    }

}
