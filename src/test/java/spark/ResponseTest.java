package spark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ResponseTest {

    private Response response;
    private HttpServletResponse httpServletResponse;

    private ArgumentCaptor<Cookie> cookieArgumentCaptor;

    @BeforeEach
    public void setUp() {
        httpServletResponse = mock(HttpServletResponse.class);
        response = new Response(httpServletResponse);
        cookieArgumentCaptor = ArgumentCaptor.forClass(Cookie.class);
    }

    @Test
    public void testConstructor_whenHttpServletResponseParameter() {
        HttpServletResponse returnResponse = KiwiReflection.getTypedFieldValue(response, "response", HttpServletResponse.class);
        assertThat(returnResponse).isSameAs(httpServletResponse);
    }

    @Test
    public void testSetStatus() {
        final int finalStatusCode = HttpServletResponse.SC_OK;

        response.status(finalStatusCode);
        verify(httpServletResponse).setStatus(finalStatusCode);
    }

    @Test
    public void testGetStatus() {
        response.status();
        verify(httpServletResponse).getStatus();
    }

    @Test
    public void testSetType() {
        final String finalType = "text/html";

        response.type(finalType);
        verify(httpServletResponse).setContentType(finalType);
    }

    @Test
    public void testGetType() {
        response.type();
        verify(httpServletResponse).getContentType();
    }

    @Test
    public void testSetBody() {
        final String finalBody = "Hello world!";

        response.body(finalBody);
        String returnBody = KiwiReflection.getTypedFieldValue(response, "body", String.class);
        assertThat(returnBody).isEqualTo(finalBody);
    }

    @Test
    public void testGetBody() {
        final String finalBody = "Hello world!";

        KiwiReflection.setFieldValue(response, "body", finalBody);
        String returnBody = response.body();
        assertThat(returnBody).isEqualTo(finalBody);
    }

    @Test
    public void testRaw() {
        HttpServletResponse returnResponse = response.raw();
        assertThat(returnResponse).isSameAs(httpServletResponse);
    }

    @Test
    public void testHeader() {
        final String finalHeaderKey = "Content-Length";
        final String finalHeaderValue = "32";

        response.header(finalHeaderKey, finalHeaderValue);
        verify(httpServletResponse).addHeader(finalHeaderKey, finalHeaderValue);
    }

    @Test
    public void testIntHeader() {
        response.header("X-Processing-Time", 10);
        verify(httpServletResponse).addIntHeader("X-Processing-Time", 10);
    }

    @Test
    public void testJavaUtilDateHeader() {
        Date now = new Date();
        response.header("X-Processing-Since", now);
        verify(httpServletResponse).addDateHeader("X-Processing-Since", now.getTime());
    }

    @Test
    public void testJavaSqlDateHeader() {
        Date now = new Date();
        response.header("X-Processing-Since", new java.sql.Date(now.getTime()));
        verify(httpServletResponse).addDateHeader("X-Processing-Since", now.getTime());
    }

    @Test
    public void testInstantDateHeader() {
        Date now = new Date();
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
    public void testCookie_whenNameAndValueParameters_shouldAddCookieSuccessfully() {

        final String finalDomain = "";
        final String finalPath = "";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = -1;
        final boolean finalSecured = false;
        final boolean finalHttpOnly = false;

        response.cookie(finalName, finalValue);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testCookie_whenNameValueAndMaxAgeParameters_shouldAddCookieSuccessfully() {

        final String finalDomain = "";
        final String finalPath = "";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = false;
        final boolean finalHttpOnly = false;

        response.cookie(finalName, finalValue, finalMaxAge);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testCookie_whenNameValueMaxAgeAndSecuredParameters_shouldAddCookieSuccessfully() {
        final String finalDomain = "";
        final String finalPath = "";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = true;
        final boolean finalHttpOnly = false;

        response.cookie(finalName, finalValue, finalMaxAge, finalSecured);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testCookie_whenNameValueMaxAgeSecuredAndHttpOnlyParameters_shouldAddCookieSuccessfully() {
        final String finalDomain = "";
        final String finalPath = "";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = true;
        final boolean finalHttpOnly = true;

        response.cookie(finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testCookie_whenPathNameValueMaxAgeAndSecuredParameters_shouldAddCookieSuccessfully() {
        final String finalDomain = "";
        final String finalPath = "/cookie/SetCookie";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = true;
        final boolean finalHttpOnly = false;

        response.cookie(finalPath, finalName, finalValue, finalMaxAge, finalSecured);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testCookie_whenPathNameValueMaxAgeSecuredAndHttpOnlyParameters_shouldAddCookieSuccessfully() {
        final String finalDomain = "";
        final String finalPath = "/cookie/SetCookie";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = true;
        final boolean finalHttpOnly = true;

        response.cookie(finalPath, finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testCookie_whenDomainPathNameValueMaxAgeSecuredAndHttpOnlyParameters_shouldAddCookieSuccessfully() {
        final String finalDomain = "example.com";
        final String finalPath = "/cookie/SetCookie";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = true;
        final boolean finalHttpOnly = true;

        response.cookie(finalDomain, finalPath, finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        verify(httpServletResponse).addCookie(cookieArgumentCaptor.capture());
        validateCookieContent(cookieArgumentCaptor.getValue(), finalDomain, finalPath, finalValue, finalMaxAge, finalSecured, finalHttpOnly);
    }

    @Test
    public void testRemoveCookie_shouldModifyPropertiesFromCookieSuccessfully() {
        final String finalPath = "/cookie/SetCookie";
        final String finalName = "cookie_name";
        final String finalValue = "Test Cookie";
        final int finalMaxAge = 86400;
        final boolean finalSecured = true;
        final boolean finalHttpOnly = true;

        response.cookie(finalPath, finalName, finalValue, finalMaxAge, finalSecured, finalHttpOnly);

        response.removeCookie(finalName);
        verify(httpServletResponse, times(2)).addCookie(cookieArgumentCaptor.capture());

        assertAll(
                () -> assertThat(cookieArgumentCaptor.getValue().getValue()).isEmpty(),
                () -> assertThat(cookieArgumentCaptor.getValue().getMaxAge()).isZero()
        );
    }

    @Test
    public void testRedirect_whenLocationParameter_shouldModifyStatusCodeSuccessfully() throws Exception { // NOSONAR
        final String finalLocation = "/test";

        response.redirect(finalLocation);
        verify(httpServletResponse).sendRedirect(finalLocation);
    }

    @Test
    public void testRedirect_whenLocationAndHttpStatusCodeParameters_shouldModifyStatusCodeSuccessfully() throws
                                                                                                          Exception { // NOSONAR
        final String finalLocation = "/test";
        int finalStatusCode = HttpServletResponse.SC_BAD_GATEWAY;

        response.redirect(finalLocation, finalStatusCode);

        verify(httpServletResponse).setStatus(finalStatusCode);
        verify(httpServletResponse).setHeader("Location", finalLocation);
        verify(httpServletResponse).setHeader("Connection", "close");
        verify(httpServletResponse).sendError(finalStatusCode);
    }
}
