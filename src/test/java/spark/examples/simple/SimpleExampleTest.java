package spark.examples.simple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

@ExtendWith(SparkStopExtension.class)
class SimpleExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        SimpleExample.main(null);
        Spark.awaitInitialization();
    }

    @Test
    void hello() throws Exception {
        var response = testUtil.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void helloPost() throws Exception {
        var response = testUtil.doMethod("POST", "/hello", "body text");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World: body text")
        );
    }

    @Test
    void privateRoute() throws Exception {
        var response = testUtil.doMethod("GET", "/private", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(401),
                () -> assertThat(response.body).isEqualTo("Go Away!!!")
        );
    }

    @Test
    void selectedUser() throws Exception {
        var response = testUtil.doMethod("GET", "/users/scott", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Selected user: scott")
        );
    }

    @Test
    void newsSection() throws Exception {
        var response = testUtil.doMethod("GET", "/news/world", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body)
                        .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>world</news>")
        );
    }

    @Test
    void protectedRoute() throws Exception {
        var response = testUtil.doMethod("GET", "/protected", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(403),
                () -> assertThat(response.body).isEqualTo("I don't think so!!!")
        );
    }

    @Test
    void redirect() throws Exception {
        // the client follows the redirect by default, landing on /news/world
        var response = testUtil.doMethod("GET", "/redirect", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body)
                        .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>world</news>")
        );
    }

    @Test
    void root() throws Exception {
        var response = testUtil.doMethod("GET", "/", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("root")
        );
    }
}
