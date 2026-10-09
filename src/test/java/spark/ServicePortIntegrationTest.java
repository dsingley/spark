package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Service.ignite;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.util.ServiceStopExtension;
import spark.util.SparkTestUtil;

import java.time.Duration;
import java.util.ArrayList;

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

        var testUtil = new SparkTestUtil(actualPort);

        var response = testUtil.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    @Timeout(60)
    void testRandomPorts_whenSeveralServersRunAtOnce_eachGetsItsOwnWorkingPort() throws Exception {
        var services = new ArrayList<Service>();
        try {
            for (int i = 0; i < 10; i++) {
                var other = ignite();
                other.ipAddress("127.0.0.1");
                other.port(0);
                other.get("/hi", (q, a) -> "Hello World!");
                other.awaitInitialization();
                services.add(other);
            }

            var ports = services.stream().map(Service::port).toList();

            assertThat(ports).doesNotHaveDuplicates().allMatch(p -> p > 0);
            for (int port : ports) {
                assertThat(new SparkTestUtil(port).doMethod("GET", "/hi", null).body).isEqualTo("Hello World!");
            }
        } finally {
            services.forEach(s -> s.stopAndAwait(Duration.ofSeconds(5)));
        }
    }

    @Test
    @Timeout(120)
    void testRandomPorts_whenServersAreStartedAndStoppedInARow_noneFailsToBind() throws Exception {
        for (int i = 0; i < 50; i++) {
            var other = ignite();
            other.ipAddress("127.0.0.1");
            other.port(0);
            other.get("/hi", (q, a) -> "Hello World!");
            other.awaitInitialization();
            try {
                assertThat(other.port()).isPositive();
                assertThat(new SparkTestUtil(other.port()).doMethod("GET", "/hi", null).body).isEqualTo("Hello World!");
            } finally {
                other.stopAndAwait(Duration.ofSeconds(5));
            }
        }
    }

}
