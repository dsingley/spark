package spark;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import spark.util.SparkTestUtil;

import spark.util.SparkTestUtil.UrlResponse;

import static spark.Spark.awaitInitialization;
import static spark.Spark.before;

import static spark.Spark.stop;

public class FilterTest {
    static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() throws IOException {
        testUtil = new SparkTestUtil(4567);

        before("/justfilter", (q, a) -> System.out.println("Filter matched"));
        awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        stop();
    }

    @Test
    public void testJustFilter() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/justfilter", null);

        System.out.println("response.status = " + response.status);
        assertThat(response.status).isEqualTo(404);
    }

}
