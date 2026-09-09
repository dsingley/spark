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
class FilterExampleWildcardTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        FilterExampleWildcard.main(null);
        Spark.awaitInitialization();
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
