package spark.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

/**
 * Verifies {@link SparkFilter} by deploying it as a real servlet filter in a Jetty
 * {@link WebAppContext}, rather than through Spark's own embedded server. The deployment
 * descriptor is src/test/webapp/WEB-INF/web.xml, which maps SparkFilter to every path and
 * points it at {@link MyApp} (via the applicationClass init-param) as the application whose
 * routes it should serve.
 */
@ExtendWith(SparkStopExtension.class)
class ServletTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServletTest.class);

    private static final String SOME_PATH = "/somepath";
    private static final int PORT = 9393;

    private static SparkTestUtil testUtil;
    private static Server server;

    @BeforeAll
    static void beforeAll() throws Exception {
        testUtil = new SparkTestUtil(PORT);

        server = new Server();
        var connector = new ServerConnector(server);

        // Set some timeout options to make debugging easier.
        connector.setIdleTimeout(1000 * 60 * 60);
        connector.setPort(PORT);
        server.setConnectors(new Connector[] {connector});

        var context = new WebAppContext();
        context.setServer(server);
        context.setContextPath(SOME_PATH);
        context.setWar("src/test/webapp");

        server.setHandler(context);
        server.start();
    }

    @AfterAll
    static void afterAll() throws Exception {
        server.stop();
        server.join();

        if (MyApp.tmpExternalFile != null) {
            LOG.debug("tearDown().deleting: {}", MyApp.tmpExternalFile);
            MyApp.tmpExternalFile.delete();
        }
    }

    @Test
    void testGetHi() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void testHiHead() throws Exception {
        var response = testUtil.doMethod("HEAD", SOME_PATH + "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEmpty()
        );
    }

    @Test
    void testGetHiAfterFilter() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/hi", null);
        assertThat(response.headers.get("after")).contains("foobar");
    }

    @Test
    void testGetRoot() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Root!")
        );
    }

    @Test
    void testEchoParam1() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/shizzy", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: shizzy")
        );
    }

    @Test
    void testEchoParam2() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/gunit", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: gunit")
        );
    }

    @Test
    void testUnauthorized() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/protected/resource", null);
        assertThat(response.status).isEqualTo(401);
    }

    @Test
    void testNotFound() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/no/resource", null);
        assertThat(response.status).isEqualTo(404);
    }

    @Test
    void testPost() throws Exception {
        var response = testUtil.doMethod("POST", SOME_PATH + "/poster", "Fo shizzy");
        assertAll(
                () -> assertThat(response.status).isEqualTo(201),
                () -> assertThat(response.body).contains("Fo shizzy")
        );
    }

    @Test
    void testStaticResource() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/css/style.css", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains("Content of css file")
        );
    }

    @Test
    void testStaticWelcomeResource() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/pages/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains("<html><body>Hello Static World!</body></html>")
        );
    }

    @Test
    void testExternalStaticFile() throws Exception {
        var response = testUtil.doMethod("GET", SOME_PATH + "/" + MyApp.EXTERNAL_FILE, null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Content of external file")
        );
    }
}
