package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

@ExtendWith(SparkServerExtension.class)
class SparkServerExtensionBeforeAllTest {

    private static SparkStarter running;

    @BeforeAll
    static void startServer(SparkStarter starter) throws Exception {
        running = starter.runSpark(http -> {
            http.get("/ping", (request, response) -> "pong");
            http.get("/health", (request, response) -> "healthy");
        });
    }

    @Test
    void shouldHandlePingRequest() throws Exception {
        var response = new SparkTestUtil(running.port()).get("/ping");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("pong")
        );
    }

    @Test
    void shouldHandleHealthRequest() throws Exception {
        var response = new SparkTestUtil(running.port()).get("/health");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("healthy")
        );
    }
}
