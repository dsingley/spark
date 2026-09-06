package spark.examples.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.util.SparkTestUtil;

class SessionExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        SessionExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void remembersNameAcrossRequestsViaSession() throws Exception {
        SparkTestUtil.UrlResponse formResponse = testUtil.doMethod("GET", "/", null);
        assertThat(formResponse.body).contains("What's your name?");

        // the client follows both redirects below (POST /entry -> GET /, and GET /clear -> GET /) by default
        SparkTestUtil.UrlResponse entryResponse = testUtil.doMethod("POST", "/entry?name=Scott", "");
        assertThat(entryResponse.body).isEqualTo("<html><body>Hello, Scott!</body></html>");

        SparkTestUtil.UrlResponse greetingResponse = testUtil.doMethod("GET", "/", null);
        assertThat(greetingResponse.body).isEqualTo("<html><body>Hello, Scott!</body></html>");

        SparkTestUtil.UrlResponse afterClearResponse = testUtil.doMethod("GET", "/clear", null);
        assertAll(
                () -> assertThat(afterClearResponse.status).isEqualTo(200),
                () -> assertThat(afterClearResponse.body).contains("What's your name?")
        );
    }
}
