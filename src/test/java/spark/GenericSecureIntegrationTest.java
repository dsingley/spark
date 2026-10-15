package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.halt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

import java.util.Map;

class GenericSecureIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericSecureIntegrationTest.class);

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        // note that the keystore stuff is retrieved from SparkTestUtil which
        // respects JVM params for keystore, password
        // but offers a default included store if not.
        http.secure(SparkTestUtil.getKeyStoreLocation(), SparkTestUtil.getKeystorePassword(), null, null);

        http.before("/protected/*", (request, response) -> halt(401, "Go Away!"));

        http.get("/hi", (request, response) -> "Hello World!");

        http.get("/ip", (request, response) -> request.ip());

        http.get("/:param", (request, response) -> "echo: " + request.params(":param"));

        http.get("/paramwithmaj/:paramWithMaj", (request, response) -> "echo: " + request.params(":paramWithMaj"));

        http.get("/", (request, response) -> "Hello Root!");

        http.post("/poster", (request, response) -> {
            var body = request.body();
            response.status(201); // created
            return "Body was: " + body;
        });

        http.patch("/patcher", (request, response) -> {
            var body = request.body();
            response.status(200);
            return "Body was: " + body;
        });

        http.after("/hi", (request, response) -> response.header("after", "foobar"));
    });

    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void testGetHi() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void testXForwardedFor() throws Exception {
        var xForwardedFor = "XXX.XXX.XXX.XXX";
        var headers = Map.of("X-Forwarded-For", xForwardedFor);

        var response = testUtil.doMethod("GET", "/ip", null, true, "text/html", headers);
        assertThat(response.body).isEqualTo(xForwardedFor);

        response = testUtil.doMethod("GET", "/ip", null, true, "text/html", null);
        assertThat(response.body).isNotEqualTo(xForwardedFor);
    }

    @Test
    void testHiHead() throws Exception {
        var response = testUtil.doMethodSecure("HEAD", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEmpty()
        );
    }

    @Test
    void testGetHiAfterFilter() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/hi", null);
        assertThat(response.headers.get("after")).contains("foobar");
    }

    @Test
    void testGetRoot() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Root!")
        );
    }

    @Test
    void testEchoParam1() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/shizzy", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: shizzy")
        );
    }

    @Test
    void testEchoParam2() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/gunit", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: gunit")
        );
    }

    @Test
    void testEchoParamWithMaj() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/paramwithmaj/plop", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: plop")
        );
    }

    @Test
    void testUnauthorized() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/protected/resource", null);
        assertThat(response.status).isEqualTo(401);
    }

    @Test
    void testNotFound() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/no/resource", null);
        assertThat(response.status).isEqualTo(404);
    }

    @Test
    void testPost() throws Exception {
        var response = testUtil.doMethodSecure("POST", "/poster", "Fo shizzy");
        LOG.info(response.body);
        assertAll(
                () -> assertThat(response.status).isEqualTo(201),
                () -> assertThat(response.body).contains("Fo shizzy")
        );
    }

    @Test
    void testPatch() throws Exception {
        var response = testUtil.doMethodSecure("PATCH", "/patcher", "Fo shizzy");
        LOG.info(response.body);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains("Fo shizzy")
        );
    }
}
