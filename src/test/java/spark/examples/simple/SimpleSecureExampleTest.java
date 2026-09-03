package spark.examples.simple;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.Spark;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class SimpleSecureExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        SimpleSecureExample.main(null);
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
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Secure World!")
        );
    }

    @Test
    public void helloPost() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("POST", "/hello", "body text");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Secure World: body text")
        );
    }

    @Test
    public void selectedUser() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/users/scott", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Selected user: scott")
        );
    }

    @Test
    public void privateRoute() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/private", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(401),
                () -> assertThat(response.body).isEqualTo("Go Away!!!")
        );
    }

    @Test
    public void newsSection() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/news/world", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body)
                        .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>world</news>")
        );
    }

    @Test
    public void protectedRoute() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/protected", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(403),
                () -> assertThat(response.body).isEqualTo("I don't think so!!!")
        );
    }

    @Test
    public void redirect() throws Exception {
        // the client follows the redirect by default, landing on /news/world
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/redirect", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body)
                        .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>world</news>")
        );
    }

    @Test
    public void root() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethodSecure("GET", "/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("root")
        );
    }
}
