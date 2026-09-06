package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static spark.Spark.awaitInitialization;
import static spark.Spark.awaitStop;
import static spark.Spark.before;
import static spark.Spark.stop;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.util.SparkTestUtil;
import spark.util.SparkTestUtil.UrlResponse;

class FilterTest {
    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);

        before("/justfilter", (q, a) -> System.out.println("Filter matched"));
        awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        stop();
        awaitStop();
    }

    @Test
    void testJustFilter() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/justfilter", null);

        System.out.println("response.status = " + response.status);
        assertThat(response.status).isEqualTo(404);
    }

}
