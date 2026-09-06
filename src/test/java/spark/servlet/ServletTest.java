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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import spark.util.SparkTestUtil;
import spark.util.SparkTestUtil.UrlResponse;

import java.util.concurrent.CountDownLatch;

class ServletTest {

    private static final String SOMEPATH = "/somepath";
    private static final int PORT = 9393;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletTest.class);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        testUtil = new SparkTestUtil(PORT);

        final Server server = new Server();
        ServerConnector connector = new ServerConnector(server);

        // Set some timeout options to make debugging easier.
        connector.setIdleTimeout(1000 * 60 * 60);
        connector.setPort(PORT);
        server.setConnectors(new Connector[] {connector});

        WebAppContext bb = new WebAppContext();
        bb.setServer(server);
        bb.setContextPath(SOMEPATH);
        bb.setWar("src/test/webapp");

        server.setHandler(bb);
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info(">>> STARTING EMBEDDED JETTY SERVER for jUnit testing of SparkFilter");
                    server.start();
                    latch.countDown();
                    System.in.read();
                    LOGGER.info(">>> STOPPING EMBEDDED JETTY SERVER");
                    server.stop();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(100);
                }
            }
        }).start();

        latch.await();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
        if (MyApp.tmpExternalFile != null) {
            LOGGER.debug("tearDown().deleting: " + MyApp.tmpExternalFile);
            MyApp.tmpExternalFile.delete();
        }
    }

    @Test
    void testGetHi() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void testHiHead() throws Exception {
        UrlResponse response = testUtil.doMethod("HEAD", SOMEPATH + "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("")
        );
    }

    @Test
    void testGetHiAfterFilter() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/hi", null);
        assertThat(response.headers.get("after")).contains("foobar");
    }

    @Test
    void testGetRoot() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Root!")
        );
    }

    @Test
    void testEchoParam1() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/shizzy", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: shizzy")
        );
    }

    @Test
    void testEchoParam2() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/gunit", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: gunit")
        );
    }

    @Test
    void testUnauthorized() throws Exception {
        UrlResponse urlResponse = testUtil.doMethod("GET", SOMEPATH + "/protected/resource", null);
        assertThat(urlResponse.status).isEqualTo(401);
    }

    @Test
    void testNotFound() throws Exception {
        UrlResponse urlResponse = testUtil.doMethod("GET", SOMEPATH + "/no/resource", null);
        assertThat(urlResponse.status).isEqualTo(404);
    }

    @Test
    void testPost() throws Exception {
        UrlResponse response = testUtil.doMethod("POST", SOMEPATH + "/poster", "Fo shizzy");
        assertAll(
                () -> assertThat(response.status).isEqualTo(201),
                () -> assertThat(response.body).contains("Fo shizzy")
        );
    }

    @Test
    void testStaticResource() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/css/style.css", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains("Content of css file")
        );
    }

    @Test
    void testStaticWelcomeResource() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/pages/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains("<html><body>Hello Static World!</body></html>")
        );
    }

    @Test
    void testExternalStaticFile() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", SOMEPATH + "/" + MyApp.EXTERNAL_FILE, null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Content of external file")
        );
    }
}
