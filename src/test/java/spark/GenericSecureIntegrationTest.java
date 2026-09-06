package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.patch;
import static spark.Spark.post;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.util.SparkTestUtil;
import spark.util.SparkTestUtil.UrlResponse;

import java.util.HashMap;
import java.util.Map;

class GenericSecureIntegrationTest {

    static SparkTestUtil testUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSecureIntegrationTest.class);

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);

        // note that the keystore stuff is retrieved from SparkTestUtil which
        // respects JVM params for keystore, password
        // but offers a default included store if not.
        Spark.secure(SparkTestUtil.getKeyStoreLocation(),
                     SparkTestUtil.getKeystorePassword(), null, null);

        before("/protected/*", (request, response) -> {
            halt(401, "Go Away!");
        });

        get("/hi", (request, response) -> "Hello World!");

        get("/ip", (request, response) -> request.ip());

        get("/:param", (request, response) -> "echo: " + request.params(":param"));

        get("/paramwithmaj/:paramWithMaj", (request, response) -> "echo: " + request.params(":paramWithMaj"));

        get("/", (request, response) -> "Hello Root!");

        post("/poster", (request, response) -> {
            String body = request.body();
            response.status(201); // created
            return "Body was: " + body;
        });

        patch("/patcher", (request, response) -> {
            String body = request.body();
            response.status(200);
            return "Body was: " + body;
        });

        after("/hi", (request, response) -> {
            response.header("after", "foobar");
        });

        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void testGetHi() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void testXForwardedFor() throws Exception {
        final String xForwardedFor = "XXX.XXX.XXX.XXX";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Forwarded-For", xForwardedFor);

        UrlResponse response = testUtil.doMethod("GET", "/ip", null, true, "text/html", headers);
        assertThat(response.body).isEqualTo(xForwardedFor);

        response = testUtil.doMethod("GET", "/ip", null, true, "text/html", null);
        assertThat(response.body).isNotEqualTo(xForwardedFor);
    }

    @Test
    void testHiHead() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("HEAD", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("")
        );
    }

    @Test
    void testGetHiAfterFilter() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/hi", null);
        assertThat(response.headers.get("after")).contains("foobar");
    }

    @Test
    void testGetRoot() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Root!")
        );
    }

    @Test
    void testEchoParam1() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/shizzy", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: shizzy")
        );
    }

    @Test
    void testEchoParam2() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/gunit", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: gunit")
        );
    }

    @Test
    void testEchoParamWithMaj() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("GET", "/paramwithmaj/plop", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("echo: plop")
        );
    }

    @Test
    void testUnauthorized() throws Exception {
        UrlResponse urlResponse = testUtil.doMethodSecure("GET", "/protected/resource", null);
        assertThat(urlResponse.status).isEqualTo(401);
    }

    @Test
    void testNotFound() throws Exception {
        UrlResponse urlResponse = testUtil.doMethodSecure("GET", "/no/resource", null);
        assertThat(urlResponse.status).isEqualTo(404);
    }

    @Test
    void testPost() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("POST", "/poster", "Fo shizzy");
        LOGGER.info(response.body);
        assertAll(
                () -> assertThat(response.status).isEqualTo(201),
                () -> assertThat(response.body).contains("Fo shizzy")
        );
    }

    @Test
    void testPatch() throws Exception {
        UrlResponse response = testUtil.doMethodSecure("PATCH", "/patcher", "Fo shizzy");
        LOGGER.info(response.body);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains("Fo shizzy")
        );
    }
}
