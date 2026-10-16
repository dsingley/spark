package spark.examples.accept;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class JsonAcceptTypeExampleTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(JsonAcceptTypeExample::run);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void jsonAcceptTypeMatchesRoute() throws Exception {
        var response = testUtil.doMethod("GET", "/hello", null, "application/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("{\"message\": \"Hello World\"}")
        );
    }

    @Test
    void htmlAcceptTypeDoesNotMatchRoute() throws Exception {
        var response = testUtil.doMethod("GET", "/hello", null, "text/html");
        assertThat(response.status).isEqualTo(404);
    }
}
