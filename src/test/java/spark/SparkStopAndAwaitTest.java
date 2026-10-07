package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import spark.util.SparkTestUtil;

import java.io.IOException;
import java.time.Duration;

class SparkStopAndAwaitTest {

    private static final int PORT = 6561;

    @Test
    void shouldStopTheServerAndWaitForItToStop() throws Exception {
        Spark.ipAddress("127.0.0.1");
        Spark.port(PORT);
        Spark.get("/ping", (request, response) -> "pong");
        Spark.awaitInitialization();
        assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");

        var stopped = Spark.stopAndAwait(Duration.ofSeconds(5));

        assertAll(
                () -> assertThat(stopped).isTrue(),
                () -> assertThatThrownBy(() -> new SparkTestUtil(PORT).get("/ping")).isInstanceOf(IOException.class)
        );
    }
}
