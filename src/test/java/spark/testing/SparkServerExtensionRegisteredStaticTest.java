package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.util.SparkTestUtil;

class SparkServerExtensionRegisteredStaticTest {

    private static final int PORT = 6548;

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(PORT);
        http.get("/ping", (request, response) -> "pong");
        http.get("/health", (request, response) -> "healthy");
    });

    @Test
    void shouldHandlePingRequest() throws Exception {
        var response = new SparkTestUtil(SPARK.port()).get("/ping");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("pong")
        );
    }

    @Test
    void shouldHandleHealthRequest() throws Exception {
        var response = new SparkTestUtil(SPARK.port()).get("/health");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("healthy")
        );
    }

    @Test
    void shouldExposePortTheServerWasConfiguredWith() {
        assertThat(SPARK.port()).isEqualTo(PORT);
    }
}
