package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

@ExtendWith(SparkServerExtension.class)
class SparkServerExtensionWithSecurityTest {

    private static final int PORT = 9876;

    @BeforeAll
    static void startServer(SparkStarter starter) throws Exception {
        starter.runSpark(https -> {
            https.ipAddress("127.0.0.1");
            https.port(PORT);
            https.secure(SparkTestUtil.getKeyStoreLocation(), SparkTestUtil.getKeystorePassword(), null, null);
            https.get("/ping", (request, response) -> "pong");
            https.get("/health", (request, response) -> "healthy");
        });
    }

    @Test
    void shouldHandlePingRequestOverHttps() throws Exception {
        var response = new SparkTestUtil(PORT).doMethodSecure("GET", "/ping", null);

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("pong")
        );
    }

    @Test
    void shouldHandleHealthRequestOverHttps() throws Exception {
        var response = new SparkTestUtil(PORT).doMethodSecure("GET", "/health", null);

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("healthy")
        );
    }
}
