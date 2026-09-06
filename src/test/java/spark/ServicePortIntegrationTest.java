package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Service.ignite;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.util.SparkTestUtil;

/**
 * Created by Tom on 08/02/2017.
 */
class ServicePortIntegrationTest {

    private static Service service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicePortIntegrationTest.class);

    @BeforeAll
    static void beforeAll() {
        service = ignite();
        service.port(0);

        service.get("/hi", (q, a) -> "Hello World!");

        service.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        service.stop();
        service.awaitStop();
    }

    @Test
    void testGetPort_withRandomPort() throws Exception {
        int actualPort = service.port();

        LOGGER.info("got port ");

        SparkTestUtil testUtil = new SparkTestUtil(actualPort);

        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

}
