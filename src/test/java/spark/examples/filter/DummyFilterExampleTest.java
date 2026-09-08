package spark.examples.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;
import spark.util.SparkTestUtil;

import java.time.Duration;

class DummyFilterExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        DummyFilter.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop(Duration.ofSeconds(5));
    }

    @Test
    void hello() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", null);
        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("Hello World!");
    }

    @Test
    void unmappedRouteStill404s() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/anything", null);
        assertThat(response.status).isEqualTo(404);
    }
}
