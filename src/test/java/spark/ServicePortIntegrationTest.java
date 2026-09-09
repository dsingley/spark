package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Service.ignite;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.util.ServiceStopExtension;
import spark.util.SparkTestUtil;

/**
 * Created by Tom on 08/02/2017.
 */
class ServicePortIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServicePortIntegrationTest.class);

    private static Service service;

    @RegisterExtension
    static ServiceStopExtension stopExtension = new ServiceStopExtension(() -> service);

    @BeforeAll
    static void beforeAll() {
        service = ignite();
        service.port(0);

        service.get("/hi", (q, a) -> "Hello World!");

        service.awaitInitialization();
    }

    @Test
    void testGetPort_withRandomPort() throws Exception {
        int actualPort = service.port();

        LOG.info("got port {}", actualPort);

        SparkTestUtil testUtil = new SparkTestUtil(actualPort);

        var response = testUtil.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

}
