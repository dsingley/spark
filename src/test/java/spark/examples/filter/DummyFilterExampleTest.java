package spark.examples.filter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyFilterExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        DummyFilter.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void hello() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null);
        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("Hello World!");
    }

    @Test
    public void unmappedRouteStill404s() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/anything", null);
        assertThat(response.status).isEqualTo(404);
    }
}
