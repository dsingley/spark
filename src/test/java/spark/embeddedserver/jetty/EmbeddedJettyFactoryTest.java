package spark.embeddedserver.jetty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import spark.ExceptionMapper;
import spark.embeddedserver.EmbeddedServer;
import spark.route.Routes;
import spark.ssl.SslStores;
import spark.staticfiles.StaticFilesConfiguration;

class EmbeddedJettyFactoryTest {

    private EmbeddedServer embeddedServer;

    @AfterEach
    void tearDown() {
        if (embeddedServer != null) {
            embeddedServer.extinguish();
        }
    }

    @Test
    void create() throws Exception {
        final JettyServerFactory jettyServerFactory = mock(JettyServerFactory.class);
        final StaticFilesConfiguration staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        final ExceptionMapper exceptionMapper = mock(ExceptionMapper.class);
        final Routes routes = mock(Routes.class);

        var server = new Server();
        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(server);

        var embeddedJettyFactory = new EmbeddedJettyFactory(jettyServerFactory);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, exceptionMapper, false);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6757, (SslStores) null, 100, 10, 10000);

        verify(jettyServerFactory, times(1)).create(100, 10, 10000);
        verifyNoMoreInteractions(jettyServerFactory);
        assertThat(((JettyHandler) server.getHandler()).getSessionCookieConfig().isHttpOnly()).isTrue();
    }

    @Test
    void create_withThreadPool() throws Exception {
        var threadPool = new QueuedThreadPool(100);
        var jettyServerFactory = mock(JettyServerFactory.class);
        var staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        var exceptionMapper = mock(ExceptionMapper.class);
        var routes = mock(Routes.class);

        when(jettyServerFactory.create(threadPool)).thenReturn(new Server(threadPool));

        var embeddedJettyFactory = new EmbeddedJettyFactory(jettyServerFactory).withThreadPool(threadPool);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, exceptionMapper, false);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6758, (SslStores) null, 0, 0, 0);

        verify(jettyServerFactory, times(1)).create(threadPool);
        verifyNoMoreInteractions(jettyServerFactory);
    }

    @Test
    void create_withNullThreadPool() throws Exception {
        var jettyServerFactory = mock(JettyServerFactory.class);
        var staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        var exceptionMapper = mock(ExceptionMapper.class);
        var routes = mock(Routes.class);

        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(new Server());

        var embeddedJettyFactory = new EmbeddedJettyFactory(jettyServerFactory).withThreadPool(null);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, exceptionMapper, false);

        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6759, (SslStores) null, 100, 10, 10000);

        verify(jettyServerFactory, times(1)).create(100, 10, 10000);
        verifyNoMoreInteractions(jettyServerFactory);
    }

    @Test
    void create_withoutHttpOnly() throws Exception {
        var jettyServerFactory = mock(JettyServerFactory.class);
        var staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        var routes = mock(Routes.class);

        var server = new Server();
        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(server);

        var embeddedJettyFactory = new EmbeddedJettyFactory(jettyServerFactory).withHttpOnly(false);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, ExceptionMapper.getServletInstance(), false);
        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6759, (SslStores) null, 100, 10, 10000);

        assertThat(((JettyHandler) server.getHandler()).getSessionCookieConfig().isHttpOnly()).isFalse();
    }

    // this is the same test as above, except it exercises the deprecated EmbeddedServerFactory#create method 
    @SuppressWarnings("deprecation")
    @Test
    void create_deprecated_withoutHttpOnly() throws Exception {
        var jettyServerFactory = mock(JettyServerFactory.class);
        var staticFilesConfiguration = mock(StaticFilesConfiguration.class);
        var routes = mock(Routes.class);

        var server = new Server();
        when(jettyServerFactory.create(100, 10, 10000)).thenReturn(server);

        var embeddedJettyFactory = new EmbeddedJettyFactory(jettyServerFactory).withHttpOnly(false);
        embeddedServer = embeddedJettyFactory.create(routes, staticFilesConfiguration, false);
        embeddedServer.trustForwardHeaders(true);
        embeddedServer.ignite("localhost", 6759, (SslStores) null, 100, 10, 10000);

        assertThat(((JettyHandler) server.getHandler()).getSessionCookieConfig().isHttpOnly()).isFalse();
    }

}
