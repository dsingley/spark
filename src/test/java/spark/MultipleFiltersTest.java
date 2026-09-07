package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.after;
import static spark.Spark.awaitInitialization;
import static spark.Spark.awaitStop;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.stop;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.util.SparkTestUtil;

/**
 * Basic test to ensure that multiple before and after filters can be mapped to a route.
 */
class MultipleFiltersTest {

    private static SparkTestUtil http;

    @BeforeAll
    static void beforeAll() {
        http = new SparkTestUtil(4567);

        before("/user", INITIALIZE_COUNTER, INCREMENT_COUNTER, LOAD_USER);

        after("/user", INCREMENT_COUNTER, (req, res) -> {
            int counter = req.attribute("counter");
            assertThat(counter).isEqualTo(2);
        });

        // assertions here and in the after filter above run on the request-handling
        // thread; a failure surfaces to the test below only as a non-200 status, not
        // as a direct assertion failure
        get("/user", (request, response) -> {
            assertThat((int) request.attribute("counter")).isEqualTo(1);
            return ((User) request.attribute("user")).name();
        });

        awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        stop();
        awaitStop();
    }

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
        SparkTestUtil.UrlResponse response = http.get("/user");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Kevin")
        );
    }
}
