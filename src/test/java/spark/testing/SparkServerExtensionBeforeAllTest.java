package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Service;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

@ExtendWith(SparkServerExtension.class)
class SparkServerExtensionBeforeAllTest {

    @BeforeAll
    static void startServer(SparkStarter starter) throws Exception {
        starter.runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.get("/ping", (request, response) -> "pong");
            http.get("/health", (request, response) -> "healthy");
        });
    }

    @Test
    void shouldHandlePingRequest() throws Exception {
        var response = new SparkTestUtil(Service.SPARK_DEFAULT_PORT).get("/ping");

        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("pong");
    }

    @Test
    void shouldHandleHealthRequest() throws Exception {
        var response = new SparkTestUtil(Service.SPARK_DEFAULT_PORT).get("/health");

        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("healthy");
    }
}
