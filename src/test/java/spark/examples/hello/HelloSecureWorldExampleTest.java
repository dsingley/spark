package spark.examples.hello;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloSecureWorldExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        HelloSecureWorld.main(new String[0]);
        Spark.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void hello() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/hello", null);
        assertThat(response.status).isEqualTo(200);
        assertThat(response.body).isEqualTo("Hello Secure World!");
    }
}
