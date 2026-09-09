package spark.examples.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.Spark;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

@ExtendWith(SparkStopExtension.class)
class SessionExampleTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);
        SessionExample.main(null);
        Spark.awaitInitialization();
    }

    @Test
    void remembersNameAcrossRequestsViaSession() throws Exception {
        var formResponse = testUtil.doMethod("GET", "/", null);
        assertThat(formResponse.body).contains("What's your name?");

        // the client follows both redirects below (POST /entry -> GET /, and GET /clear -> GET /) by default
        var entryResponse = testUtil.doMethod("POST", "/entry?name=Scott", "");
        assertThat(entryResponse.body).isEqualTo("<html><body>Hello, Scott!</body></html>");

        var greetingResponse = testUtil.doMethod("GET", "/", null);
        assertThat(greetingResponse.body).isEqualTo("<html><body>Hello, Scott!</body></html>");

        var afterClearResponse = testUtil.doMethod("GET", "/clear", null);
        assertAll(
                () -> assertThat(afterClearResponse.status).isEqualTo(200),
                () -> assertThat(afterClearResponse.body).contains("What's your name?")
        );
    }
}
