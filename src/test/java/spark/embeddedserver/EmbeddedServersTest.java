package spark.embeddedserver;

import java.io.File;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import spark.Spark;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.JettyServerFactory;
import spark.ssl.SslStores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmbeddedServersTest {

    @TempDir
    File temporaryFolder;

    @AfterAll
    public static void afterAll() {
        Spark.stop();
    }

    @Test
    public void testAddAndCreate_whenCreate_createsCustomServer() throws Exception {
        // Create custom Server
        Server server = new Server();
        File requestLogFile = new File(temporaryFolder, "request.log");
        server.setRequestLog(new CustomRequestLog(new RequestLogWriter(requestLogFile.getAbsolutePath()), CustomRequestLog.NCSA_FORMAT));
        JettyServerFactory serverFactory = mock(JettyServerFactory.class);
        when(serverFactory.create(0, 0, 0)).thenReturn(server);

        String id = "custom";

        // Register custom server
        EmbeddedServers.add(id, new EmbeddedJettyFactory(serverFactory));
        EmbeddedServer embeddedServer = EmbeddedServers.create(id, null, null, null, false);
        assertThat(embeddedServer).isNotNull();
        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 0, (SslStores) null, 0, 0, 0);

        assertThat(requestLogFile.exists()).isTrue();
        embeddedServer.extinguish();
        verify(serverFactory).create(0, 0, 0);
    }

    @Test
    public void testAdd_whenConfigureRoutes_createsCustomServer() throws Exception {
        File requestLogFile = new File(temporaryFolder, "request.log");
        // Register custom server
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, new EmbeddedJettyFactory(new JettyServerFactory() {
            @Override
            public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
                Server server = new Server();
                server.setRequestLog(new CustomRequestLog(new RequestLogWriter(requestLogFile.getAbsolutePath()), CustomRequestLog.NCSA_FORMAT));
                return server;
            }

            @Override
            public Server create(ThreadPool threadPool) {
                return null;
            }
        }));
        Spark.get("/", (request, response) -> "OK");
        Spark.awaitInitialization();

        assertThat(requestLogFile.exists()).isTrue();
    }

}
