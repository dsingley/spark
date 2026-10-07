package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.util.SparkTestUtil;

class SparkServerExtensionRegisteredInstanceTest {

    private static final int PORT = 6549;

    @RegisterExtension
    final SparkServerExtension spark = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(PORT);
        http.get("/ping", (request, response) -> "pong");
        http.get("/health", (request, response) -> "healthy");
    });

    @Test
    void shouldHandlePingRequest() throws Exception {
        var response = new SparkTestUtil(spark.port()).get("/ping");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("pong")
        );
    }

    @Test
    void shouldHandleHealthRequest() throws Exception {
        var response = new SparkTestUtil(spark.port()).get("/health");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("healthy")
        );
    }
}
