package spark.examples.transformer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DefaultTransformerExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        DefaultTransformerExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void helloUsesDefaultTransformer() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("{\"message\":\"Hello World\"}")
        );
    }

    @Test
    public void hello2OverridesDefaultTransformer() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello2", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("custom transformer")
        );
    }
}
