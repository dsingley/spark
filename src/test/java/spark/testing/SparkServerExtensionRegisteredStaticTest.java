package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.Service;
import spark.util.SparkTestUtil;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class SparkServerExtensionRegisteredStaticTest {

    private static final int PORT = 6548;
    private static final Set<Service> SERVICES = ConcurrentHashMap.newKeySet();

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(PORT);
        http.get("/ping", (request, response) -> "pong");
        http.get("/health", (request, response) -> "healthy");
    });

    @AfterAll
    static void assertOneServerWasUsedForAllTests() {
        assertThat(SERVICES).hasSize(1);
    }

    @Test
    void shouldHandlePingRequest() throws Exception {
        SERVICES.add(SPARK.service());

        var response = new SparkTestUtil(SPARK.port()).get("/ping");

        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("pong");
    }

    @Test
    void shouldHandleHealthRequest() throws Exception {
        SERVICES.add(SPARK.service());

        var response = new SparkTestUtil(SPARK.port()).get("/health");

        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("healthy");
    }

    @Test
    void shouldExposePortTheServerWasConfiguredWith() {
        assertThat(SPARK.port()).isEqualTo(PORT);
    }
}
