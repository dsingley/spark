package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static spark.Spark.awaitInitialization;
import static spark.Spark.before;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

@ExtendWith(SparkStopExtension.class)
class FilterTest {
    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);

        before("/justfilter", (q, a) -> System.out.println("Filter matched"));
        awaitInitialization();
    }

    @Test
    void testJustFilter() throws Exception {
        var response = testUtil.doMethod("GET", "/justfilter", null);

        System.out.println("response.status = " + response.status);
        assertThat(response.status).isEqualTo(404);
    }

}
