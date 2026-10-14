package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

/**
 * Basic test to ensure that multiple before and after filters can be mapped to a route.
 */
class MultipleFiltersTest {

    private static final Filter LOAD_USER = (request, response) -> {
        var u = new User();
        u.name("Kevin");
        request.attribute("user", u);
    };

    private static final Filter INITIALIZE_COUNTER = (request, response) -> request.attribute("counter", 0);

    private static final Filter INCREMENT_COUNTER = (request, response) -> {
        int counter = request.attribute("counter");
        counter++;
        request.attribute("counter", counter);
    };

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(0);

        // Spark's static before and after methods accept several filters; a Service takes them one at a time
        http.before("/user", INITIALIZE_COUNTER);
        http.before("/user", INCREMENT_COUNTER);
        http.before("/user", LOAD_USER);

        http.after("/user", INCREMENT_COUNTER);
        http.after("/user", (req, res) -> {
            int counter = req.attribute("counter");
            assertThat(counter).isEqualTo(2);
        });

        // assertions here and in the after filter above run on the request-handling
        // thread; a failure surfaces to the test below only as a non-200 status, not
        // as a direct assertion failure
        http.get("/user", (request, response) -> {
            assertThat((int) request.attribute("counter")).isEqualTo(1);
            return ((User) request.attribute("user")).name();
        });
    });

    private static SparkTestUtil http;

    @BeforeAll
    static void beforeAll() {
        http = new SparkTestUtil(SPARK.port());
    }

    private static class User {
        private String name;

        public String name() {
            return name;
        }

        public void name(String name) {
            this.name = name;
        }
    }

    @Test
    void testMultipleFilters() throws Exception {
        var response = http.get("/user");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Kevin")
        );
    }
}
