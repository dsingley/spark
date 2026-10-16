package spark.examples.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class FilterExampleAttributesTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(FilterExampleAttributes::run);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void attributeSetInRouteIsVisibleInAfterFilter() throws Exception {
        var response = testUtil.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body)
                        .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><foo>bar</foo>")
        );
    }
}
