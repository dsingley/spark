package spark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.routematch.RouteMatch;
import spark.util.SparkTestUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static spark.Spark.after;
import static spark.Spark.afterAfter;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.awaitInitialization;
import static org.junit.jupiter.api.Assertions.assertAll;

public class RequestTest {

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

    RouteMatch match = new RouteMatch(null, "/hi", "/hi", "text/html", null);
    RouteMatch matchWithParams = new RouteMatch(null, "/users/:username", "/users/bob", "text/html", null);

    @BeforeEach
    public void setUp() {
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

        request = new Request(match, servletRequest);

    }

    @Test
    public void queryParamShouldReturnsParametersFromQueryString() {

        when(servletRequest.getParameter("name")).thenReturn("Federico");

        String name = request.queryParams("name");
        assertThat(name).isEqualTo("Federico");
    }

    @Test
    public void queryParamOrDefault_shouldReturnQueryParam_whenQueryParamExists() {

        when(servletRequest.getParameter("name")).thenReturn("Federico");

        String name = request.queryParamOrDefault("name", "David");
        assertThat(name).isEqualTo("Federico");
    }

    @Test
    public void queryParamOrDefault_shouldReturnDefault_whenQueryParamIsNull() {

        when(servletRequest.getParameter("name")).thenReturn(null);

        String name = request.queryParamOrDefault("name", "David");
        assertThat(name).isEqualTo("David");
    }

    @Test
    public void queryParamShouldBeParsedAsHashMap() {
        Map<String, String[]> params = new HashMap<>();
        params.put("user[name]", new String[] {"Federico"});

        when(servletRequest.getParameterMap()).thenReturn(params);

        String name = request.queryMap("user").value("name");
        assertThat(name).isEqualTo("Federico");
    }

    @Test
    public void shouldBeAbleToGetTheServletPath() {

        when(servletRequest.getServletPath()).thenReturn(THE_SERVLET_PATH);

        Request request = new Request(match, servletRequest);
        assertThat(request.servletPath()).isEqualTo(THE_SERVLET_PATH);
    }

    @Test
    public void shouldBeAbleToGetTheContextPath() {

        when(servletRequest.getContextPath()).thenReturn(THE_CONTEXT_PATH);

        Request request = new Request(match, servletRequest);
        assertThat(request.contextPath()).isEqualTo(THE_CONTEXT_PATH);
    }

    @Test
    public void shouldBeAbleToGetTheMatchedPath() {
        Request request = new Request(matchWithParams, servletRequest);
        assertThat(request.matchedPath()).isEqualTo(THE_MATCHED_ROUTE);
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
    public void testSessionNoParams_whenSessionIsNull() {

        when(servletRequest.getSession()).thenReturn(httpSession);

        assertThat(request.session().raw()).isEqualTo(httpSession);
    }

    @Test
    public void testSession_whenCreateIsTrue() {

        when(servletRequest.getSession(true)).thenReturn(httpSession);

        assertThat(request.session(true).raw()).isEqualTo(httpSession);

    }

    @Test
    public void testSession_whenCreateIsFalse() {

        when(servletRequest.getSession(true)).thenReturn(httpSession);

        assertThat(request.session(false)).isEqualTo(null);

    }

    @Test
    public void testSessionNpParams_afterSessionInvalidate() {
        when(servletRequest.getSession()).thenReturn(httpSession);

        Session session = request.session();
        session.invalidate();
        request.session();

        verify(servletRequest, times(2)).getSession();
    }

    @Test
    public void testSession_whenCreateIsTrue_afterSessionInvalidate() {
        when(servletRequest.getSession(true)).thenReturn(httpSession);

        Session session = request.session(true);
        session.invalidate();
        request.session(true);

        verify(servletRequest, times(2)).getSession(true);
    }

    @Test
    public void testSession_whenCreateIsFalse_afterSessionInvalidate() {
        when(servletRequest.getSession()).thenReturn(httpSession);
        when(servletRequest.getSession(false)).thenReturn(null);

        Session session = request.session();
        session.invalidate();
        request.session(false);

        verify(servletRequest, times(1)).getSession(false);
    }

    @Test
    public void testSession_2times() {
        when(servletRequest.getSession(true)).thenReturn(httpSession);

        Session session = request.session(true);
        session = request.session(true);

        assertThat(session).isNotNull();
        verify(servletRequest, times(1)).getSession(true);
    }

    @Test
    public void testCookies_whenCookiesArePresent() {

        Collection<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie("cookie1", "cookie1value"));
        cookies.add(new Cookie("cookie2", "cookie2value"));

        Map<String, String> expected = new HashMap<>();
        for(Cookie cookie : cookies) {
            expected.put(cookie.getName(), cookie.getValue());
        }

        Cookie[] cookieArray = cookies.toArray(new Cookie[cookies.size()]);

        when(servletRequest.getCookies()).thenReturn(cookieArray);

        assertAll(
                () -> assertThat(request.cookies()).hasSize(2),
                () -> assertThat(request.cookies()).isEqualTo(expected)
        );

    }

    @Test
    public void testCookies_whenCookiesAreNotPresent() {

        when(servletRequest.getCookies()).thenReturn(null);

        assertAll(
                () -> assertThat(request.cookies())
                        .describedAs("A Map of Cookies should have been instantiated even if cookies are not present in the request")
                        .isNotNull(),
                () -> assertThat(request.cookies()).isEmpty()
        );

    }

    @Test
    public void testCookie_whenCookiesArePresent() {

        final String cookieKey = "cookie1";
        final String cookieValue = "cookie1value";

        Collection<Cookie> cookies = new ArrayList<>();
        cookies.add(new Cookie(cookieKey, cookieValue));

        Cookie[] cookieArray = cookies.toArray(new Cookie[cookies.size()]);
        when(servletRequest.getCookies()).thenReturn(cookieArray);

        assertAll(
                () -> assertThat(request.cookie(cookieKey)).isNotNull(),
                () -> assertThat(request.cookie(cookieKey)).isEqualTo(cookieValue)
        );

    }

    @Test
    public void testCookie_whenCookiesAreNotPresent() {

        final String cookieKey = "nonExistentCookie";

        when(servletRequest.getCookies()).thenReturn(null);

        assertThat(request.cookie(cookieKey)).isNull();

    }

    @Test
    public void testRequestMethod() {

        final String requestMethod = "GET";

        when(servletRequest.getMethod()).thenReturn(requestMethod);

        assertThat(request.requestMethod()).isEqualTo(requestMethod);

    }

    @Test
    public void testScheme() {

        final String scheme = "http";

        when(servletRequest.getScheme()).thenReturn(scheme);

        assertThat(request.scheme()).isEqualTo(scheme);

    }

    @Test
    public void testHost() {

        final String host = "www.google.com";

        when(servletRequest.getHeader("host")).thenReturn(host);

        assertThat(request.host()).isEqualTo(host);

    }

    @Test
    public void testUserAgent() {

        final String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36";

        when(servletRequest.getHeader("user-agent")).thenReturn(userAgent);

        assertThat(request.userAgent()).isEqualTo(userAgent);

    }

    @Test
    public void testPort() {

        final int port = 80;

        when(servletRequest.getServerPort()).thenReturn(80);

        assertThat(request.port()).isEqualTo(port);

    }

    @Test
    public void testPathInfo() {

        final String pathInfo = "/path/to/resource";

        when(servletRequest.getPathInfo()).thenReturn(pathInfo);

        assertThat(request.pathInfo()).isEqualTo(pathInfo);

    }

    @Test
    public void testServletPath() {

        final String servletPath = "/api";

        when(servletRequest.getServletPath()).thenReturn(servletPath);

        assertThat(request.servletPath()).isEqualTo(servletPath);

    }

    @Test
    public void testContextPath() {

        final String contextPath = "/my-app";

        when(servletRequest.getContextPath()).thenReturn(contextPath);

        assertThat(request.contextPath()).isEqualTo(contextPath);

    }

    @Test
    public void testUrl() {

        final String url = "http://www.myapp.com/myapp/a";

        when(servletRequest.getRequestURL()).thenReturn(new StringBuffer(url));

        assertThat(request.url()).isEqualTo(url);

    }

    @Test
    public void testContentType() {

        final String contentType = "image/jpeg";

        when(servletRequest.getContentType()).thenReturn(contentType);

        assertThat(request.contentType()).isEqualTo(contentType);

    }

    @Test
    public void testIp() {

        final String ip = "216.58.197.106:80";

        when(servletRequest.getRemoteAddr()).thenReturn(ip);

        assertThat(request.ip()).isEqualTo(ip);

    }

    @Test
    public void testContentLength() {

        final int contentLength = 500;

        when(servletRequest.getContentLength()).thenReturn(contentLength);

        assertThat(request.contentLength()).isEqualTo(contentLength);

    }

    @Test
    public void testHeaders() {

        final String headerKey = "host";
        final String host = "www.google.com";

        when(servletRequest.getHeader(headerKey)).thenReturn(host);

        assertThat(request.headers(headerKey)).isEqualTo(host);

    }

    @Test
    public void testQueryParamsValues_whenParamExists() {

        final String[] paramValues = {"foo", "bar"};

        when(servletRequest.getParameterValues("id")).thenReturn(paramValues);

        assertThat(request.queryParamsValues("id")).containsExactly(paramValues);

    }

    @Test
    public void testQueryParamsValues_whenParamDoesNotExists() {

        when(servletRequest.getParameterValues("id")).thenReturn(null);

        assertThat(request.queryParamsValues("id")).isNull();

    }

    @Test
    public void testQueryParams() {

        Map<String, String[]> params = new HashMap<>();
        params.put("sort", new String[]{"asc"});
        params.put("items", new String[]{"10"});

        when(servletRequest.getParameterMap()).thenReturn(params);

        Set<String> result = request.queryParams();

        assertThat(result.toArray()).containsExactly(params.keySet().toArray());

    }

    @Test
    public void testURI() {

        final String requestURI = "http://localhost:8080/myapp/";

        when(servletRequest.getRequestURI()).thenReturn(requestURI);

        assertThat(request.uri()).isEqualTo(requestURI);

    }

    @Test
    public void testProtocol() {

        final String protocol = "HTTP/1.1";

        when(servletRequest.getProtocol()).thenReturn(protocol);

        assertThat(request.protocol()).isEqualTo(protocol);

    }
}
