package spark.examples.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class FilterExampleWildcardTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(FilterExampleWildcard::run);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void wildcardPathIsBlocked() throws Exception {
        var response = testUtil.doMethod("GET", "/protected/anything", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(401),
                () -> assertThat(response.body).isEqualTo("Go Away!")
        );
    }
}
