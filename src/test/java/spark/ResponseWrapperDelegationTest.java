package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class ResponseWrapperDelegationTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.ipAddress("127.0.0.1");
        http.port(0);

        http.get("/204", (q, a) -> {
            a.status(204);
            return "";
        });

        http.after("/204", (q, a) -> {
            if (a.status() == 204) {
                a.status(200);
                a.body("ok");
            }
        });

        http.get("/json", (q, a) -> {
            a.type("application/json");
            return "{\"status\": \"ok\"}";
        });

        http.after("/json", (q, a) -> {
            if ("application/json".equalsIgnoreCase(a.type())) {
                a.type("text/plain");
            }
        });

        http.exception(Exception.class, (exception, q, a) -> exception.printStackTrace());
    });

    static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void filters_can_detect_response_status() throws Exception {
        var response = testUtil.get("/204");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("ok")
        );
    }

    @Test
    void filters_can_detect_content_type() throws Exception {
        var response = testUtil.get("/json");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("{\"status\": \"ok\"}"),
                // Jetty 12 echoes MimeTypes' assumed charset for text/plain into the Content-Type
                // header when none is set explicitly; Jetty 9 did not. Cosmetic, not a functional change.
            () -> assertThat(response.headers).containsEntry("Content-Type", "text/plain;charset=iso-8859-1")
        );
    }
}
