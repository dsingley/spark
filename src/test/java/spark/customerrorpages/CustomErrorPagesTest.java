package spark.customerrorpages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.CustomErrorPages;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class CustomErrorPagesTest {

    private static final String CUSTOM_NOT_FOUND = "custom not found 404";
    private static final String CUSTOM_INTERNAL = "custom internal 500";
    private static final String HELLO_WORLD = "hello world!";
    public static final String APPLICATION_JSON = "application/json";
    private static final String QUERY_PARAM_KEY = "qparkey";

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.get("/hello", (q, a) -> HELLO_WORLD);

        http.get("/raiseinternal", (q, a) -> {
            throw new Exception("");
        });

        http.notFound(CUSTOM_NOT_FOUND);

        http.internalServerError((request, response) -> {
            if (request.queryParams(QUERY_PARAM_KEY) != null) {
                throw new Exception();
            }
            response.type(APPLICATION_JSON);
            return CUSTOM_INTERNAL;
        });
    });

    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void testGetHi() throws Exception {
        var response = testUtil.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(HELLO_WORLD)
        );
    }

    @Test
    void testCustomNotFound() throws Exception {
        var response = testUtil.doMethod("GET", "/othernotmapped", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(404),
                () -> assertThat(response.body).isEqualTo(CUSTOM_NOT_FOUND)
        );
    }

    @Test
    void testCustomInternal() throws Exception {
        var response = testUtil.doMethod("GET", "/raiseinternal", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(500),
                () -> assertThat(response.headers).containsEntry("Content-Type", APPLICATION_JSON),
                () -> assertThat(response.body).isEqualTo(CUSTOM_INTERNAL)
        );
    }

    @Test
    void testCustomInternalFailingRoute() throws Exception {
        var response = testUtil.doMethod("GET", "/raiseinternal?" + QUERY_PARAM_KEY + "=sumthin", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(500),
                () -> assertThat(response.body).isEqualTo(CustomErrorPages.INTERNAL_ERROR)
        );
    }

}
