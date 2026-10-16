package spark.examples.hello;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class HelloSecureWorldExampleTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.secure(SparkTestUtil.getKeyStoreLocation(), SparkTestUtil.getKeystorePassword(), null, null);
        HelloSecureWorld.run(http);
    });

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void hello() throws Exception {
        var response = testUtil.doMethodSecure("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello Secure World!")
        );
    }
}
