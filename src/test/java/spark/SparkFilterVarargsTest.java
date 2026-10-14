package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

/**
 * Checks the static {@code Spark.before} and {@code Spark.after} methods that take several filters at
 * once, which {@code Service} does not have. They use the static Spark singleton, so this class stops it
 * afterwards, and it uses a port chosen by the operating system.
 */
@ExtendWith(SparkStopExtension.class)
class SparkFilterVarargsTest {

    private static SparkTestUtil http;

    @BeforeAll
    static void beforeAll() {
        Spark.ipAddress("127.0.0.1");
        Spark.port(0);

        Spark.before("/order",
                (request, response) -> request.attribute("steps", "b1"),
                (request, response) -> request.attribute("steps", request.attribute("steps") + ",b2"));

        Spark.get("/order", (request, response) -> request.attribute("steps"));

        Spark.after("/order",
                (request, response) -> response.header("X-Steps", request.attribute("steps") + ",a1"),
                (request, response) -> response.header("X-Steps", response.raw().getHeader("X-Steps") + ",a2"));

        Spark.awaitInitialization();

        http = new SparkTestUtil(Spark.port());
    }

    @Test
    void shouldRunAllTheFiltersInTheOrderTheyWereGiven() throws Exception {
        var response = http.get("/order");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("b1,b2"),
                () -> assertThat(response.headers).containsEntry("X-Steps", "b1,b2,a1,a2")
        );
    }
}
