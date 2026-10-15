package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

import java.io.IOException;
import java.time.Duration;

// This tests the static facade on purpose, so it uses the Spark singleton. The extension stops it even
// if the test fails before reaching stopAndAwait, so the next test class starts with a fresh one.
@ExtendWith(SparkStopExtension.class)
class SparkStopAndAwaitTest {

    @Test
    void shouldStopTheServerAndWaitForItToStop() throws Exception {
        Spark.ipAddress("127.0.0.1");
        Spark.port(0);
        Spark.get("/ping", (request, response) -> "pong");
        Spark.awaitInitialization();
        var port = Spark.port();
        assertThat(new SparkTestUtil(port).get("/ping").body).isEqualTo("pong");

        var stopped = Spark.stopAndAwait(Duration.ofSeconds(5));

        assertAll(
                () -> assertThat(stopped).isTrue(),
                () -> assertThatThrownBy(() -> new SparkTestUtil(port).get("/ping")).isInstanceOf(IOException.class)
        );
    }
}
