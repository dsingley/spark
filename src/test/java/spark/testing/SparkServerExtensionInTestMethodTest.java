package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

@ExtendWith(SparkServerExtension.class)
class SparkServerExtensionInTestMethodTest {

    private static final int PORT = 6546;

    @Test
    void shouldStartServerWhenStarterIsInjectedIntoTestMethod(SparkStarter starter) throws Exception {
        starter.runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.port(PORT);
            http.get("/ping", (request, response) -> "pong");
        });

        var response = new SparkTestUtil(PORT).get("/ping");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("pong")
        );
    }
}
