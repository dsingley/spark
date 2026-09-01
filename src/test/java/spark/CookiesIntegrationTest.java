package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static spark.Spark.halt;
import static spark.Spark.post;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * System tests for the Cookies support.
 *
 * @author dreambrother
 */
public class CookiesIntegrationTest {

    private static final String DEFAULT_HOST_URL = "http://localhost:4567";
    // Jetty 12 renders a deleted cookie's Expires attribute in standard RFC 1123 form
    // (space-separated, e.g. "Thu, 01 Jan 1970 00:00:00 GMT"); Apache HttpClient's default
    // cookie spec only accepts the legacy Netscape/RFC 2109 hyphenated form and silently
    // discards the header otherwise, so the client-side cookie store never sees the
    // deletion. Both forms are RFC 6265-compliant; opt into the more permissive RELAXED spec.
    private CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(StandardCookieSpec.RELAXED).build())
            .build();

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        post("/assertNoCookies", (request, response) -> {
            if (!request.cookies().isEmpty()) {
                halt(500);
            }
            return "";
        });

        post("/setCookie", (request, response) -> {
            response.cookie(request.queryParams("cookieName"), request.queryParams("cookieValue"));
            return "";
        });

        post("/assertHasCookie", (request, response) -> {
            String cookieValue = request.cookie(request.queryParams("cookieName"));
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                halt(500);
            }
            return "";
        });

        post("/removeCookie", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.cookie(cookieName);
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                halt(500);
            }
            response.removeCookie(cookieName);
            return "";
        });

        post("/path/setCookieWithPath", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.queryParams("cookieValue");
            response.cookie("/path", cookieName, cookieValue, -1, false);
            return "";
        }) ;

        post("/path/removeCookieWithPath", (request, response) -> {
            String cookieName = request.queryParams("cookieName");
            String cookieValue = request.cookie(cookieName);
            if (!request.queryParams("cookieValue").equals(cookieValue)) {
                halt(500);
            }
            response.removeCookie("/path", cookieName);
            return "";
        }) ;

        post("/path/assertNoCookies", (request, response) -> {
            if (!request.cookies().isEmpty()) {
                halt(500);
            }
            return "";
        });

        Spark.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        Spark.stop();
    }

    @Test
    public void testEmptyCookies() {
        httpPost("/assertNoCookies");
    }

    @Test
    public void testCreateCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertHasCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
    }

    @Test
    public void testRemoveCookie() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/setCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/removeCookie?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/assertNoCookies");
    }

    @Test
    public void testRemoveCookieWithPath() {
        String cookieName = "testCookie";
        String cookieValue = "testCookieValue";
        httpPost("/path/setCookieWithPath?cookieName=" + cookieName + "&cookieValue=" + cookieValue);

        // for sanity, check that cookie is not sent with request if path doesn't match
        httpPost("/assertNoCookies");

        // now remove cookie with matching path
        httpPost("/path/removeCookieWithPath?cookieName=" + cookieName + "&cookieValue=" + cookieValue);
        httpPost("/path/assertNoCookies");
    }

    private void httpPost(String relativePath) {
        HttpPost request = new HttpPost(DEFAULT_HOST_URL + relativePath);
        assertThatCode(() -> {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                assertThat(response.getCode()).isEqualTo(200);
            }
        }).doesNotThrowAnyException();
    }
}
