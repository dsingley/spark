package spark.customerrorpages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.get;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.CustomErrorPages;
import spark.Spark;
import spark.util.SparkTestUtil;

class CustomErrorPagesTest {

    private static final String CUSTOM_NOT_FOUND = "custom not found 404";
    private static final String CUSTOM_INTERNAL = "custom internal 500";
    private static final String HELLO_WORLD = "hello world!";
    public static final String APPLICATION_JSON = "application/json";
    private static final String QUERY_PARAM_KEY = "qparkey";

    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);

        get("/hello", (q, a) -> HELLO_WORLD);

        get("/raiseinternal", (q, a) -> {
            throw new Exception("");
        });

        notFound(CUSTOM_NOT_FOUND);

        internalServerError((request, response) -> {
            if (request.queryParams(QUERY_PARAM_KEY) != null) {
                throw new Exception();
            }
            response.type(APPLICATION_JSON);
            return CUSTOM_INTERNAL;
        });

        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void testGetHi() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(HELLO_WORLD)
        );
    }

    @Test
    void testCustomNotFound() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/othernotmapped", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(404),
                () -> assertThat(response.body).isEqualTo(CUSTOM_NOT_FOUND)
        );
    }

    @Test
    void testCustomInternal() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/raiseinternal", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(500),
                () -> assertThat(response.headers).containsEntry("Content-Type", APPLICATION_JSON),
                () -> assertThat(response.body).isEqualTo(CUSTOM_INTERNAL)
        );
    }

    @Test
    void testCustomInternalFailingRoute() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/raiseinternal?" + QUERY_PARAM_KEY + "=sumthin", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(500),
                () -> assertThat(response.body).isEqualTo(CustomErrorPages.INTERNAL_ERROR)
        );
    }

}
