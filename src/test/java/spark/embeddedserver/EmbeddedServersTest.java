package spark.embeddedserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.JettyServerFactory;
import spark.ssl.SslStores;
import spark.testing.SparkServerExtension;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

import java.io.File;

@ExtendWith(SparkServerExtension.class)
class EmbeddedServersTest {

    @TempDir
    File temporaryFolder;

    private EmbeddedServer embeddedServer;

    @AfterEach
    void tearDown() {
        if (embeddedServer != null) {
            embeddedServer.extinguish();
        }

        // The factories are registered JVM-wide and cannot be removed, so put back the default one that
        // a test replaced; otherwise every server created later in this JVM would use the test's factory
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJettyFactory());
    }

    @Test
    void testAddAndCreate_whenCreate_createsCustomServer() throws Exception {
        // Create custom Server
        var server = new Server();
        var requestLogFile = new File(temporaryFolder, "request.log");
        server.setRequestLog(new CustomRequestLog(new RequestLogWriter(requestLogFile.getAbsolutePath()), CustomRequestLog.NCSA_FORMAT));
        var serverFactory = mock(JettyServerFactory.class);
        when(serverFactory.create(0, 0, 0)).thenReturn(server);

        var id = "custom";

        // Register custom server
        EmbeddedServers.add(id, new EmbeddedJettyFactory(serverFactory));
        embeddedServer = EmbeddedServers.create(id, null, null, null, false);
        assertThat(embeddedServer).isNotNull();
        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 0, (SslStores) null, 0, 0, 0);

        assertThat(requestLogFile).exists();
        verify(serverFactory).create(0, 0, 0);
    }

    @Test
    void testAdd_whenConfigureRoutes_createsCustomServer(SparkStarter starter) throws Exception {
        var requestLogFile = new File(temporaryFolder, "request.log");
        // Register custom server
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJettyFactory(new JettyServerFactory() {
            @Override
            public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
                var server = new Server();
                server.setRequestLog(new CustomRequestLog(new RequestLogWriter(requestLogFile.getAbsolutePath()), CustomRequestLog.NCSA_FORMAT));
                return server;
            }

            @Override
            public Server create(ThreadPool threadPool) {
                return null;
            }
        }));
        starter.runSpark(http -> http.get("/", (request, response) -> "OK"));

        assertThat(requestLogFile).exists();

        // and the server built by the custom factory actually serves the route
        var response = new SparkTestUtil(starter.port()).get("/");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("OK")
        );
    }

}
