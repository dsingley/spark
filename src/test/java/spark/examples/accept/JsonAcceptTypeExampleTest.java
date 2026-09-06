package spark.examples.accept;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.util.SparkTestUtil;

class JsonAcceptTypeExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        JsonAcceptTypeExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void jsonAcceptTypeMatchesRoute() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("{\"message\": \"Hello World\"}")
        );
    }

    @Test
    void htmlAcceptTypeDoesNotMatchRoute() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null, "text/html");
        assertThat(response.status).isEqualTo(404);
    }
}
