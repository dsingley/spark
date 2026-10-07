package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

@ExtendWith(SparkServerExtension.class)
class SparkServerExtensionWithFilterTest {

    private static final int PORT = 56789;

    private static boolean authenticated;

    @BeforeAll
    static void startServer(SparkStarter starter) throws Exception {
        starter.runSpark(http -> {
            http.ipAddress("127.0.0.1");
            http.port(PORT);
            http.before((request, response) -> {
                if (!authenticated) {
                    http.halt(401, "Go away!");
                }
            });
            http.get("/secret", (request, response) -> "Don't forget to drink your Ovaltine!");
        });
    }

    @Test
    void shouldAllowRequest_WhenAuthenticated() throws Exception {
        authenticated = true;

        var response = new SparkTestUtil(PORT).get("/secret");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Don't forget to drink your Ovaltine!")
        );
    }

    @Test
    void shouldRejectRequest_WhenNotAuthenticated() throws Exception {
        authenticated = false;

        var response = new SparkTestUtil(PORT).get("/secret");

        assertAll(
                () -> assertThat(response.status).isEqualTo(401),
                () -> assertThat(response.body).isEqualTo("Go away!")
        );
    }
}
