package spark.examples.staticresources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class StaticResourcesExampleTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(StaticResources::run);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void hello() throws Exception {
        var response = testUtil.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void staticPageHtml() throws Exception {
        var response = testUtil.doMethod("GET", "/page.html", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("<html><body>Hello Static Files World!</body></html>")
        );
    }
}
