package spark.examples.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class DefaultTransformerExampleTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(DefaultTransformerExample::run);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void helloUsesDefaultTransformer() throws Exception {
        var response = testUtil.doMethod("GET", "/hello", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("{\"message\":\"Hello World\"}")
        );
    }

    @Test
    void hello2OverridesDefaultTransformer() throws Exception {
        var response = testUtil.doMethod("GET", "/hello2", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("custom transformer")
        );
    }
}
