package spark.examples.filter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FilterExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        FilterExample.main(null);
        Spark.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void rejectsBadCredentials() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello?user=some&password=guy", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(401),
                () -> assertThat(response.body).isEqualTo("You are not welcome here!!!")
        );
    }

    @Test
    public void acceptsGoodCredentials() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello?user=foo&password=bar", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!"),
                () -> assertThat(response.headers.get("Foo")).isEqualTo("Set by second before filter"),
                () -> assertThat(response.headers.get("spark")).isEqualTo("added by after-filter")
        );
    }
}
