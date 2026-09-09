package spark.examples.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

@ExtendWith(SparkStopExtension.class)
class FilterExampleAttributesTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        FilterExampleAttributes.main(null);
        Spark.awaitInitialization();
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
