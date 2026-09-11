package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;
import org.mockito.ArgumentCaptor;

import java.util.Date;

class ResponseTest {

    private Response response;
    private HttpServletResponse httpServletResponse;

    private ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @BeforeEach
    void setUp() {
        httpServletResponse = mock(HttpServletResponse.class);
        response = new Response(httpServletResponse);
        cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
    }

    @Test
    void testConstructor_whenHttpServletResponseParameter() {
        var returnResponse = KiwiReflection.getTypedFieldValue(
            response, "httpServletResponse", HttpServletResponse.class);
        assertThat(returnResponse).isSameAs(httpServletResponse);
    }

    @Test
    void testSetStatus() {
        var finalStatusCode = HttpServletResponse.SC_OK;

        response.status(finalStatusCode);
        verify(httpServletResponse).setStatus(finalStatusCode);
    }

    @Test
    void testGetStatus() {
        response.status();
        verify(httpServletResponse).getStatus();
    }

    @Test
    void testSetType() {
        var finalType = "text/html";

        response.type(finalType);
        verify(httpServletResponse).setContentType(finalType);
    }

    @Test
    void testGetType() {
        response.type();
        verify(httpServletResponse).getContentType();
    }

    @Test
    void testSetBody() {
        var finalBody = "Hello world!";

        response.body(finalBody);
        var returnBody = KiwiReflection.getTypedFieldValue(response, "body", String.class);
        assertThat(returnBody).isEqualTo(finalBody);
    }

    @Test
    void testGetBody() {
        var finalBody = "Hello world!";

        KiwiReflection.setFieldValue(response, "body", finalBody);
        var returnBody = response.body();
        assertThat(returnBody).isEqualTo(finalBody);
    }

    @Test
    void testRaw() {
        var returnResponse = response.raw();
        assertThat(returnResponse).isSameAs(httpServletResponse);
    }

    @Test
    void testHeader() {
        var finalHeaderKey = "Content-Length";
        var finalHeaderValue = "32";

        response.header(finalHeaderKey, finalHeaderValue);
        verify(httpServletResponse).addHeader(finalHeaderKey, finalHeaderValue);
    }

    @Test
    void testIntHeader() {
        response.header("X-Processing-Time", 10);
        verify(httpServletResponse).addIntHeader("X-Processing-Time", 10);
    }

    @Test
    void testJavaUtilDateHeader() {
        var now = new Date();
        response.header("X-Processing-Since", now);
        verify(httpServletResponse).addDateHeader("X-Processing-Since", now.getTime());
    }

    @Test
    void testJavaSqlDateHeader() {
        var now = new Date();
        response.header("X-Processing-Since", new java.sql.Date(now.getTime()));
        verify(httpServletResponse).addDateHeader("X-Processing-Since", now.getTime());
    }

    @Test
    void testInstantDateHeader() {
        var now = new Date();
        response.header("X-Processing-Since", now.toInstant());
        verify(httpServletResponse).addDateHeader("X-Processing-Since", now.getTime());
    }

    private void validateCookieContent(Cookie cookie,
                                       String domain,
                                       String path,
                                       String value,
                                       int maxAge,
                                       boolean secured,
                                       boolean httpOnly) {
        assertAll(
                () -> assertThat(cookie.getDomain()).isEqualTo(domain),
                () -> assertThat(cookie.getPath()).isEqualTo(path),
                () -> assertThat(cookie.getValue()).isEqualTo(value),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(maxAge),
                () -> assertThat(cookie.getSecure()).isEqualTo(secured),
                () -> assertThat(cookie.isHttpOnly()).isEqualTo(httpOnly)
        );
    }

    @Test
    void testCookie_whenNameAndValueParameters_shouldAddCookieSuccessfully() {

        var finalDomain = "";
        var finalPath = "";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = -1;
        var finalSecured = false;
        var finalHttpOnly = false;

        response.cookie(finalName, finalValue);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testCookie_whenNameValueAndMaxAgeParameters_shouldAddCookieSuccessfully() {

        var finalDomain = "";
        var finalPath = "";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = false;
        var finalHttpOnly = false;

        response.cookie(finalName, finalValue, finalMaxAge);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testCookie_whenNameValueMaxAgeAndSecuredParameters_shouldAddCookieSuccessfully() {
        var finalDomain = "";
        var finalPath = "";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = true;
        var finalHttpOnly = false;

        response.cookie(finalName, finalValue, finalMaxAge, finalSecured);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testCookie_whenNameValueMaxAgeSecuredAndHttpOnlyParameters_shouldAddCookieSuccessfully() {
        var finalDomain = "";
        var finalPath = "";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = true;
        var finalHttpOnly = true;

        response.cookie(finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testCookie_whenPathNameValueMaxAgeAndSecuredParameters_shouldAddCookieSuccessfully() {
        var finalDomain = "";
        var finalPath = "/cookie/SetCookie";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = true;
        var finalHttpOnly = false;

        response.cookie(finalPath, finalName, finalValue, finalMaxAge, finalSecured);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testCookie_whenPathNameValueMaxAgeSecuredAndHttpOnlyParameters_shouldAddCookieSuccessfully() {
        var finalDomain = "";
        var finalPath = "/cookie/SetCookie";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = true;
        var finalHttpOnly = true;

        response.cookie(finalPath, finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testCookie_whenDomainPathNameValueMaxAgeSecuredAndHttpOnlyParameters_shouldAddCookieSuccessfully() {
        var finalDomain = "example.com";
        var finalPath = "/cookie/SetCookie";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = true;
        var finalHttpOnly = true;

        response.cookie(finalDomain, finalPath, finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    void testRemoveCookie_shouldModifyPropertiesFromCookieSuccessfully() {
        var finalPath = "/cookie/SetCookie";
        var finalName = "cookie_name";
        var finalValue = "Test Cookie";
        var finalMaxAge = 86400;
        var finalSecured = true;
        var finalHttpOnly = true;

        response.cookie(finalPath, finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        response.removeCookie(finalName);
        verify(httpServletResponse, times(2)).addCookie(cookieArgumentCaptor.capture());

        assertAll(
                () -> assertThat(cookieArgumentCaptor.getValue().getValue()).isEmpty(),
                () -> assertThat(cookieArgumentCaptor.getValue().getMaxAge()).isZero()
        );
    }

    @Test
    void testRedirect_whenLocationParameter_shouldModifyStatusCodeSuccessfully() throws Exception {
        var finalLocation = "/test";

        response.redirect(finalLocation);
        verify(httpServletResponse).sendRedirect(finalLocation);
    }

    @Test
    void testRedirect_whenLocationAndHttpStatusCodeParameters_shouldModifyStatusCodeSuccessfully()
        throws Exception {
        
        var finalLocation = "/test";
        var finalStatusCode = HttpServletResponse.SC_BAD_GATEWAY;

        response.redirect(finalLocation, finalStatusCode);

        verify(httpServletResponse).setStatus(finalStatusCode);
        verify(httpServletResponse).setHeader("Location", finalLocation);
        verify(httpServletResponse).setHeader("Connection", "close");
        verify(httpServletResponse).sendError(finalStatusCode);
    }
}
