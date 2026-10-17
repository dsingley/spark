package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
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

        // filters without a path apply to every request, in the order given; each one records itself
        // in a request attribute and the after filters append to the response body
        http.before(trace("b1"), trace("b2"));
        http.after(
                (request, response) -> response.body(response.body() + ",a1"),
                (request, response) -> response.body(response.body() + ",a2"));

        // filters with an accept type apply only to requests that accept that type
        http.before("/typed", "application/json", trace("t1"), trace("t2"));
        http.after("/typed", "application/json",
                (request, response) -> response.body(response.body() + ",t3"),
                (request, response) -> response.body(response.body() + ",t4"));
        http.get("/typed", (request, response) -> String.join(",", request.<List<String>>attribute("trace")));
        http.get("/other", (request, response) -> String.join(",", request.<List<String>>attribute("trace")));
    });

    private static Filter trace(String name) {
        return (request, response) -> {
            List<String> trace = request.attribute("trace");
            if (trace == null) {
                trace = new ArrayList<>();
                request.attribute("trace", trace);
            }
            trace.add(name);
        };
    }

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
                // the after filters without a path also run for /user and append to the body
                () -> assertThat(response.body).isEqualTo("Kevin,a1,a2")
        );
    }

    @Test
    void filtersWithoutPathApplyToEveryRequestInOrder() throws Exception {
        var response = http.get("/other");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("b1,b2,a1,a2")
        );
    }

    @Test
    void filtersWithAcceptTypeApplyOnlyToMatchingRequests() throws Exception {
        var matching = http.doMethod("GET", "/typed", null, "application/json");
        var other = http.doMethod("GET", "/typed", null, "text/plain");
        assertAll(
                () -> assertThat(matching.body).isEqualTo("b1,b2,t1,t2,a1,a2,t3,t4"),
                () -> assertThat(other.body).isEqualTo("b1,b2,a1,a2")
        );
    }
}
