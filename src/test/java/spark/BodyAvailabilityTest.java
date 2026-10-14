package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class BodyAvailabilityTest {

    private static final Logger LOG = LoggerFactory.getLogger(BodyAvailabilityTest.class);

    private static final String BODY_CONTENT = "the body content";
    
    private static final int HTTP_OK = 200;

    private static String beforeBody = null;
    private static String routeBody = null;
    private static String afterBody = null;

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(0);

        http.before("/hello", (req, res) -> {
            LOG.debug("before-req.body() = {}", req.body());
            beforeBody = req.body();
        });

        http.post("/hello", (req, res) -> {
            LOG.debug("get-req.body() = {}", req.body());
            routeBody = req.body();
            return req.body();
        });

        http.after("/hello", (req, res) -> {
            LOG.debug("after-before-req.body() = {}", req.body());
            afterBody = req.body();
        });
    });

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        LOG.debug("setup()");

        testUtil = new SparkTestUtil(SPARK.port());
    }

    @AfterAll
    static void afterAll() {

        beforeBody = null;
        routeBody = null;
        afterBody = null;
    }

    @Test
    void testPost() throws Exception {
        var response = testUtil.doMethod("POST", "/hello", BODY_CONTENT);
        LOG.info(response.body);
        assertAll(
                () -> assertThat(response.status).isEqualTo(HTTP_OK),
                () -> assertThat(response.body).contains(BODY_CONTENT),
                () -> assertThat(beforeBody).isEqualTo(BODY_CONTENT),
                () -> assertThat(routeBody).isEqualTo(BODY_CONTENT),
                () -> assertThat(afterBody).isEqualTo(BODY_CONTENT)
        );
    }
}
