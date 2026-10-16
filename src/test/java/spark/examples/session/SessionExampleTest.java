package spark.examples.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class SessionExampleTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(SessionExample::run);

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
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
