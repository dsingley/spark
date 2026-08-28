package spark;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.util.SparkTestUtil;
import spark.util.SparkTestUtil.UrlResponse;

import java.io.IOException;

import static spark.Spark.*;

public class ResponseWrapperDelegationTest {

    static SparkTestUtil testUtil;

    @AfterClass
    public static void tearDown() {
        Spark.stop();
    }

    @BeforeClass
    public static void setup() throws IOException {
        testUtil = new SparkTestUtil(4567);

        get("/204", (q, a) -> {
            a.status(204);
            return "";
        });

        after("/204", (q, a) -> {
            if (a.status() == 204) {
                a.status(200);
                a.body("ok");
            }
        });

        get("/json", (q, a) -> {
            a.type("application/json");
            return "{\"status\": \"ok\"}";
        });

        after("/json", (q, a) -> {
            if ("application/json".equalsIgnoreCase(a.type())) {
                a.type("text/plain");
            }
        });

        exception(Exception.class, (exception, q, a) -> {
            exception.printStackTrace();
        });

        Spark.awaitInitialization();
    }

    @Test
    public void filters_can_detect_response_status() throws Exception {
        UrlResponse response = testUtil.get("/204");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("ok", response.body);
    }

    @Test
    public void filters_can_detect_content_type() throws Exception {
        UrlResponse response = testUtil.get("/json");
        Assert.assertEquals(200, response.status);
        Assert.assertEquals("{\"status\": \"ok\"}", response.body);
        // Jetty 12 echoes MimeTypes' assumed charset for text/plain into the Content-Type
        // header when none is set explicitly; Jetty 9 did not. Cosmetic, not a functional change.
        Assert.assertEquals("text/plain;charset=iso-8859-1", response.headers.get("Content-Type"));
    }
}
