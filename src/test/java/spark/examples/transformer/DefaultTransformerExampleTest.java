package spark.examples.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.util.SparkTestUtil;

class DefaultTransformerExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        DefaultTransformerExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void helloUsesDefaultTransformer() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("{\"message\":\"Hello World\"}")
        );
    }

    @Test
    void hello2OverridesDefaultTransformer() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello2", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("custom transformer")
        );
    }
}
