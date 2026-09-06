package spark.examples.multiple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Service;
import spark.util.SparkTestUtil;

class MultipleServicesExampleTest {

    private static Service first;
    private static Service second;

    private static SparkTestUtil firstClient;
    private static SparkTestUtil secondClient;

    @BeforeAll
    static void beforeAll() {
        firstClient = new SparkTestUtil(4567);
        secondClient = new SparkTestUtil(1234);

        first = MultipleServices.igniteFirstService();
        second = MultipleServices.igniteSecondService();

        first.awaitInitialization();
        second.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        first.stop();
        second.stop();
        first.awaitStop();
        second.awaitStop();
    }

    @Test
    void firstServiceHello() throws Exception {
        SparkTestUtil.UrlResponse response = firstClient.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void secondServiceHello() throws Exception {
        SparkTestUtil.UrlResponse response = secondClient.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void secondServiceHiRedirectsToHello() throws Exception {
        // the client follows the redirect by default, landing on /hello
        SparkTestUtil.UrlResponse response = secondClient.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void servicesAreIndependent() throws Exception {
        SparkTestUtil.UrlResponse response = firstClient.doMethod("GET", "/hi", null);
        assertThat(response.status).isEqualTo(404);
    }
}
