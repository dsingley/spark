package spark.customerrorpages;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import spark.CustomErrorPages;
import spark.Spark;

import spark.util.SparkTestUtil;

import static spark.Spark.get;
import static spark.Spark.internalServerError;

import static spark.Spark.notFound;
import static org.junit.jupiter.api.Assertions.assertAll;

public class CustomErrorPagesTest {

    private static final String CUSTOM_NOT_FOUND = "custom not found 404";
    private static final String CUSTOM_INTERNAL = "custom internal 500";
    private static final String HELLO_WORLD = "hello world!";
    public static final String APPLICATION_JSON = "application/json";
    private static final String QUERY_PARAM_KEY = "qparkey";

    static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() throws IOException {
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
    public static void afterAll() {
        Spark.stop();
    }

    @Test
    public void testGetHi() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(HELLO_WORLD)
        );
    }

    @Test
    public void testCustomNotFound() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/othernotmapped", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(404),
                () -> assertThat(response.body).isEqualTo(CUSTOM_NOT_FOUND)
        );
    }

    @Test
    public void testCustomInternal() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/raiseinternal", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(500),
                () -> assertThat(response.headers.get("Content-Type")).isEqualTo(APPLICATION_JSON),
                () -> assertThat(response.body).isEqualTo(CUSTOM_INTERNAL)
        );
    }

    @Test
    public void testCustomInternalFailingRoute() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/raiseinternal?" + QUERY_PARAM_KEY + "=sumthin", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(500),
                () -> assertThat(response.body).isEqualTo(CustomErrorPages.INTERNAL_ERROR)
        );
    }

}
