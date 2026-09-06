package spark.examples.templateview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.util.SparkTestUtil;

class FreeMarkerExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        FreeMarkerExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void hello() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("<h1>Hello FreeMarker World</h1>")
        );
    }
}
