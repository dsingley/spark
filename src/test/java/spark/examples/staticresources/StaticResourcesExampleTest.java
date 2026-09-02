package spark.examples.staticresources;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class StaticResourcesExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        StaticResources.main(null);
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
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    public void staticPageHtml() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/page.html", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("<html><body>Hello Static Files World!</body></html>")
        );
    }
}
