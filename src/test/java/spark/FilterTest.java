package spark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class FilterTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.before("/justfilter", (q, a) -> System.out.println("Filter matched"));
    });

    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void testJustFilter() throws Exception {
        var response = testUtil.doMethod("GET", "/justfilter", null);

        System.out.println("response.status = " + response.status);
        assertThat(response.status).isEqualTo(404);
    }

}
