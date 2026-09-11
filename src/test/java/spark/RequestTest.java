package spark;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static spark.Spark.after;
import static spark.Spark.afterAfter;
import static spark.Spark.awaitInitialization;
import static spark.Spark.before;
import static spark.Spark.get;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.routematch.RouteMatch;
import spark.util.SparkTestUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RequestTest {

    private static final String THE_SERVLET_PATH = "/the/servlet/path";
    private static final String THE_CONTEXT_PATH = "/the/context/path";
    private static final String THE_MATCHED_ROUTE = "/users/:username";
    private static final String BEFORE_MATCHED_ROUTE = "/users/:before";
    private static final String AFTER_MATCHED_ROUTE = "/users/:after";
    private static final String AFTERAFTER_MATCHED_ROUTE = "/users/:afterafter";

    private static SparkTestUtil http;

    HttpServletRequest servletRequest;
    HttpSession httpSession;
    Request request;

    RouteMatch routeMatch;
    RouteMatch routeMatchWithParams;

    @BeforeEach
    void setUp() {
        routeMatch = new RouteMatch(null, "/hi", "/hi", "text/html", null);
        routeMatchWithParams = new RouteMatch(null, "/users/:username", "/users/bob", "text/html", null);

        http = new SparkTestUtil(4567);

        before(BEFORE_MATCHED_ROUTE, (q, a) -> {
            System.out.println("before filter matched");
            shouldBeAbleToGetTheMatchedPathInBeforeFilter(q);
        });
        get(THE_MATCHED_ROUTE, (q,a)-> "Get filter matched");
        after(AFTER_MATCHED_ROUTE, (q, a) -> {
            System.out.println("after filter matched");
            shouldBeAbleToGetTheMatchedPathInAfterFilter(q);
        });
        afterAfter(AFTERAFTER_MATCHED_ROUTE, (q, a) -> {
            System.out.println("afterafter filter matched");
            shouldBeAbleToGetTheMatchedPathInAfterAfterFilter(q);
        });

        awaitInitialization();


        servletRequest = mock(HttpServletRequest.class);
        httpSession = mock(HttpSession.class);

        request = new Request(routeMatch, servletRequest);

    }

    @Test
    void queryParamShouldReturnsParametersFromQueryString() {

        when(servletRequest.getParameter("name")).thenReturn("Federico");

        var name = request.queryParams("name");
        assertThat(name).isEqualTo("Federico");
    }

    @Test
    void queryParamOrDefault_shouldReturnQueryParam_whenQueryParamExists() {

        when(servletRequest.getParameter("name")).thenReturn("Federico");

        var name = request.queryParamOrDefault("name", "David");
        assertThat(name).isEqualTo("Federico");
    }

    @Test
    void queryParamOrDefault_shouldReturnDefault_whenQueryParamIsNull() {

        when(servletRequest.getParameter("name")).thenReturn(null);

        var name = request.queryParamOrDefault("name", "David");
        assertThat(name).isEqualTo("David");
    }

    @Test
    void queryParamShouldBeParsedAsHashMap() {
        var params = Map.of("user[name]", new String[] {"Federico"});

        when(servletRequest.getParameterMap()).thenReturn(params);

        var name = request.queryMap("user").value("name");
        assertThat(name).isEqualTo("Federico");
    }

    @Test
    void shouldBeAbleToGetTheServletPath() {

        when(servletRequest.getServletPath()).thenReturn(THE_SERVLET_PATH);

        var req = new Request(routeMatch, servletRequest);
        assertThat(req.servletPath()).isEqualTo(THE_SERVLET_PATH);
    }

    @Test
    void shouldBeAbleToGetTheContextPath() {

        when(servletRequest.getContextPath()).thenReturn(THE_CONTEXT_PATH);

        var req = new Request(routeMatch, servletRequest);
        assertThat(req.contextPath()).isEqualTo(THE_CONTEXT_PATH);
    }

    @Test
    void shouldBeAbleToGetTheMatchedPath() {
        var req = new Request(routeMatchWithParams, servletRequest);
        assertThat(req.matchedPath()).isEqualTo(THE_MATCHED_ROUTE);
        try {
            http.get("/users/bob");
        } catch (Exception e) {
            // TODO - something...
            e.printStackTrace();
        }
    }

    public void shouldBeAbleToGetTheMatchedPathInBeforeFilter(Request q) {
        assertThat(q.matchedPath()).isEqualTo(BEFORE_MATCHED_ROUTE);
    }

    public void shouldBeAbleToGetTheMatchedPathInAfterFilter(Request q) {
        assertThat(q.matchedPath()).isEqualTo(AFTER_MATCHED_ROUTE);
    }

    public void shouldBeAbleToGetTheMatchedPathInAfterAfterFilter(Request q) {
        assertThat(q.matchedPath()).isEqualTo(AFTERAFTER_MATCHED_ROUTE);
    }

    @Test
    void testSessionNoParams_whenSessionIsNull() {

        when(servletRequest.getSession()).thenReturn(httpSession);

        assertThat(request.session().raw()).isEqualTo(httpSession);
    }

    @Test
    void testSession_whenCreateIsTrue() {

        when(servletRequest.getSession(true)).thenReturn(httpSession);

        assertThat(request.session(true).raw()).isEqualTo(httpSession);

    }

    @Test
    void testSession_whenCreateIsFalse() {

        when(servletRequest.getSession(true)).thenReturn(httpSession);

        assertThat(request.session(false)).isNull();

    }

    @Test
    void testSessionNpParams_afterSessionInvalidate() {
        when(servletRequest.getSession()).thenReturn(httpSession);

        var session = request.session();
        session.invalidate();
        request.session();

        verify(servletRequest, times(2)).getSession();
    }

    @Test
    void testSession_whenCreateIsTrue_afterSessionInvalidate() {
        when(servletRequest.getSession(true)).thenReturn(httpSession);

        var session = request.session(true);
        session.invalidate();
        request.session(true);

        verify(servletRequest, times(2)).getSession(true);
    }

    @Test
    void testSession_whenCreateIsFalse_afterSessionInvalidate() {
        when(servletRequest.getSession()).thenReturn(httpSession);
        when(servletRequest.getSession(false)).thenReturn(null);

        var session = request.session();
        session.invalidate();
        request.session(false);

        verify(servletRequest, times(1)).getSession(false);
    }

    @Test
    void testSession_2times() {
        when(servletRequest.getSession(true)).thenReturn(httpSession);

        request.session(true);
        var session = request.session(true);

        assertThat(session).isNotNull();
        verify(servletRequest, times(1)).getSession(true);
    }

    @Test
    void testCookies_whenCookiesArePresent() {

        Cookie[] cookieArray = {
                new Cookie("cookie1", "cookie1value"),
                new Cookie("cookie2", "cookie2value")
        };

        when(servletRequest.getCookies()).thenReturn(cookieArray);

        var expected = Arrays.stream(cookieArray)
                .collect(toMap(Cookie::getName, Cookie::getValue));

        assertAll(
                () -> assertThat(request.cookies()).hasSize(2),
                () -> assertThat(request.cookies()).isEqualTo(expected));
    }

    @Test
    void testCookies_whenCookiesAreNotPresent() {

        when(servletRequest.getCookies()).thenReturn(null);

        assertAll(
                () -> assertThat(request.cookies())
                        .describedAs("A Map of Cookies should have been instantiated even if cookies are not present in the request")
                        .isNotNull(),
                () -> assertThat(request.cookies()).isEmpty()
        );

    }

    @Test
    void testCookie_whenCookiesArePresent() {

        var cookieKey = "cookie1";
        var cookieValue = "cookie1value";

        var cookies = List.of(new Cookie(cookieKey, cookieValue));

        var cookieArray = cookies.toArray(new Cookie[cookies.size()]);
        when(servletRequest.getCookies()).thenReturn(cookieArray);

        assertAll(
                () -> assertThat(request.cookie(cookieKey)).isNotNull(),
                () -> assertThat(request.cookie(cookieKey)).isEqualTo(cookieValue)
        );

    }

    @Test
    void testCookie_whenCookiesAreNotPresent() {

        var cookieKey = "nonExistentCookie";

        when(servletRequest.getCookies()).thenReturn(null);

        assertThat(request.cookie(cookieKey)).isNull();

    }

    @Test
    void testRequestMethod() {

        var requestMethod = "GET";

        when(servletRequest.getMethod()).thenReturn(requestMethod);

        assertThat(request.requestMethod()).isEqualTo(requestMethod);

    }

    @Test
    void testScheme() {

        var scheme = "http";

        when(servletRequest.getScheme()).thenReturn(scheme);

        assertThat(request.scheme()).isEqualTo(scheme);

    }

    @Test
    void testHost() {

        var host = "www.google.com";

        when(servletRequest.getHeader("host")).thenReturn(host);

        assertThat(request.host()).isEqualTo(host);

    }

    @Test
    void testUserAgent() {

        var userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";

        when(servletRequest.getHeader("user-agent")).thenReturn(userAgent);

        assertThat(request.userAgent()).isEqualTo(userAgent);

    }

    @Test
    void testPort() {

        var port = 80;

        when(servletRequest.getServerPort()).thenReturn(80);

        assertThat(request.port()).isEqualTo(port);

    }

    @Test
    void testPathInfo() {

        var pathInfo = "/path/to/resource";

        when(servletRequest.getPathInfo()).thenReturn(pathInfo);

        assertThat(request.pathInfo()).isEqualTo(pathInfo);

    }

    @Test
    void testServletPath() {

        var servletPath = "/api";

        when(servletRequest.getServletPath()).thenReturn(servletPath);

        assertThat(request.servletPath()).isEqualTo(servletPath);

    }

    @Test
    void testContextPath() {

        var contextPath = "/my-app";

        when(servletRequest.getContextPath()).thenReturn(contextPath);

        assertThat(request.contextPath()).isEqualTo(contextPath);

    }

    @Test
    void testUrl() {

        var url = "http://www.myapp.com/myapp/a";

        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));

        assertThat(request.url()).isEqualTo(url);

    }

    @Test
    void testContentType() {

        var contentType = "image/jpeg";

        when(servletRequest.getContentType()).thenReturn(contentType);

        assertThat(request.contentType()).isEqualTo(contentType);

    }

    @Test
    void testIp() {

        var ip = "216.58.197.106:80";

        when(servletRequest.getRemoteAddr()).thenReturn(ip);

        assertThat(request.ip()).isEqualTo(ip);

    }

    @Test
    void testContentLength() {

        var contentLength = 500;

        when(servletRequest.getContentLength()).thenReturn(contentLength);

        assertThat(request.contentLength()).isEqualTo(contentLength);

    }

    @Test
    void testHeaders() {

        var headerKey = "host";
        var host = "www.google.com";

        when(servletRequest.getHeader(headerKey)).thenReturn(host);

        assertThat(request.headers(headerKey)).isEqualTo(host);

    }

    @Test
    void testQueryParamsValues_whenParamExists() {

        String[] paramValues = {"foo", "bar"};

        when(servletRequest.getParameterValues("id")).thenReturn(paramValues);

        assertThat(request.queryParamsValues("id")).containsExactly(paramValues);

    }

    @Test
    void testQueryParamsValues_whenParamDoesNotExists() {

        when(servletRequest.getParameterValues("id")).thenReturn(null);

        assertThat(request.queryParamsValues("id")).isNull();

    }

    @Test
    void testQueryParams() {

        var params = Map.of(
                "sort", new String[] { "asc" },
                "items", new String[] { "10" }
        );

        when(servletRequest.getParameterMap()).thenReturn(params);

        var queryParams = request.queryParams();

        assertThat(queryParams.toArray()).containsExactly(params.keySet().toArray());

    }

    @Test
    void testURI() {

        var requestURI = "http://localhost:8080/myapp/";

        when(servletRequest.getRequestURI()).thenReturn(requestURI);

        assertThat(request.uri()).isEqualTo(requestURI);

    }

    @Test
    void testProtocol() {

        var protocol = "HTTP/1.1";

        when(servletRequest.getProtocol()).thenReturn(protocol);

        assertThat(request.protocol()).isEqualTo(protocol);

    }
}
