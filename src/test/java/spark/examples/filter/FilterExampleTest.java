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
class FilterExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        FilterExample.main(null);
        Spark.awaitInitialization();
    }

    @Test
    void rejectsBadCredentials() throws Exception {
        var response = testUtil.doMethod("GET", "/hello?user=some&password=guy", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(401),
                () -> assertThat(response.body).isEqualTo("You are not welcome here!!!")
        );
    }

    @Test
    void acceptsGoodCredentials() throws Exception {
        var response = testUtil.doMethod("GET", "/hello?user=foo&password=bar", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!"),
                () -> assertThat(response.headers).containsEntry("Foo", "Set by second before filter"),
                () -> assertThat(response.headers).containsEntry("spark", "added by after-filter")
        );
    }
}
