package spark.examples.multiple;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class MultipleServicesExampleTest {

    @RegisterExtension
    static final SparkServerExtension FIRST = new SparkServerExtension(MultipleServices::configureFirst);

    @RegisterExtension
    static final SparkServerExtension SECOND = new SparkServerExtension(MultipleServices::configureSecond);

    private static SparkTestUtil firstClient;
    private static SparkTestUtil secondClient;

    @BeforeAll
    static void beforeAll() {
        firstClient = new SparkTestUtil(FIRST.port());
        secondClient = new SparkTestUtil(SECOND.port());
    }

    @Test
    void firstServiceHello() throws Exception {
        var response = firstClient.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void secondServiceHello() throws Exception {
        var response = secondClient.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void secondServiceHiRedirectsToHello() throws Exception {
        // the client follows the redirect by default, landing on /hello
        var response = secondClient.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void servicesAreIndependent() throws Exception {
        var response = firstClient.doMethod("GET", "/hi", null);
        assertThat(response.status).isEqualTo(404);
    }
}
