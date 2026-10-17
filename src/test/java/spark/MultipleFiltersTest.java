package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

/**
 * Basic test to ensure that multiple before and after filters can be mapped to a route with one call.
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
        http.before("/user", INITIALIZE_COUNTER, INCREMENT_COUNTER, LOAD_USER);

        http.after("/user", INCREMENT_COUNTER, (req, res) -> {
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

        // filters without a path apply to every request, in the order given
        http.before(
                (request, response) -> response.header("X-Before", "first"),
                (request, response) -> response.header("X-Before", response.raw().getHeader("X-Before") + ",second"));
        http.after(
                (request, response) -> response.header("X-After", "first"),
                (request, response) -> response.header("X-After", response.raw().getHeader("X-After") + ",second"));

        // filters with an accept type apply only to requests that accept that type
        http.before("/typed", "application/json",
                (request, response) -> response.header("X-Typed-Before", "1"),
                (request, response) -> response.header("X-Typed-Before", response.raw().getHeader("X-Typed-Before") + "2"));
        http.after("/typed", "application/json",
                (request, response) -> response.header("X-Typed-After", "1"),
                (request, response) -> response.header("X-Typed-After", response.raw().getHeader("X-Typed-After") + "2"));
        http.get("/typed", (request, response) -> "typed");
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

    @Test
    void filtersWithoutPathApplyToEveryRequestInOrder() throws Exception {
        var response = http.get("/typed");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.headers.get("X-Before")).isEqualTo("first,second"),
                () -> assertThat(response.headers.get("X-After")).isEqualTo("first,second")
        );
    }

    @Test
    void filtersWithAcceptTypeApplyOnlyToMatchingRequests() throws Exception {
        var matching = http.doMethod("GET", "/typed", null, "application/json");
        var other = http.doMethod("GET", "/typed", null, "text/plain");
        assertAll(
                () -> assertThat(matching.headers.get("X-Typed-Before")).isEqualTo("12"),
                () -> assertThat(matching.headers.get("X-Typed-After")).isEqualTo("12"),
                () -> assertThat(other.headers).doesNotContainKeys("X-Typed-Before", "X-Typed-After")
        );
    }
}
