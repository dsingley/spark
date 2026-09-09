package spark.examples.hello;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

@ExtendWith(SparkStopExtension.class)
class HelloSecureWorldExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        HelloSecureWorld.main(new String[0]);
        Spark.awaitInitialization();
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
