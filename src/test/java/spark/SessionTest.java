package spark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionTest {

    Request request;
    HttpSession httpSession;
    Session session;

    @BeforeEach
    public void setUp() {

        httpSession = mock(HttpSession.class);
        request = mock(Request.class);
        session = new Session(httpSession, request);
    }

    @Test
    public void testSession_whenHttpSessionIsNull_thenThrowException() {

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Session(null, request))
                .withMessage("session cannot be null");
    }

    @Test
    public void testSession_whenRequestIsNull_thenThrowException() {

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Session(httpSession, null))
                .withMessage("request cannot be null");
    }

    @Test
    public void testSession() {

        HttpSession internalSession = KiwiReflection.getTypedFieldValue(session, "session", HttpSession.class);
        assertThat(internalSession).isEqualTo(httpSession);
    }

    @Test
    public void testRaw() {

        assertThat(session.raw()).isEqualTo(httpSession);
    }

    @Test
    public void testAttribute_whenAttributeIsRetrieved() {

        when(httpSession.getAttribute("name")).thenReturn("Jett");

        assertThat((String) session.attribute("name")).isEqualTo("Jett");

    }

    @Test
    public void testAttribute_whenAttributeIsSet() {

        session.attribute("name", "Jett");

        verify(httpSession).setAttribute("name", "Jett");
    }

    @Test
    public void testAttributes() {

        Set<String> attributes = new HashSet<>(Arrays.asList("name", "location"));

        when(httpSession.getAttributeNames()).thenReturn(Collections.enumeration(attributes));

        assertThat(session.attributes()).isEqualTo(attributes);
    }

    @Test
    public void testCreationTime() {

        when(httpSession.getCreationTime()).thenReturn(10000000l);

        assertThat(session.creationTime()).isEqualTo(10000000l);
    }

    @Test
    public void testId() {

        when(httpSession.getId()).thenReturn("id");

        assertThat(session.id()).isEqualTo("id");
    }

    @Test
    public void testLastAccessedTime() {

        when(httpSession.getLastAccessedTime()).thenReturn(20000000l);

        assertThat(session.lastAccessedTime()).isEqualTo(20000000l);
    }

    @Test
    public void testMaxInactiveInterval_whenRetrieved() {

        when(httpSession.getMaxInactiveInterval()).thenReturn(100);

        assertThat(session.maxInactiveInterval()).isEqualTo(100);
    }

    @Test
    public void testMaxInactiveInterval_whenSet() {

        session.maxInactiveInterval(200);

        verify(httpSession).setMaxInactiveInterval(200);
    }

    @Test
    public void testInvalidate() {

        session.invalidate();

        verify(httpSession).invalidate();
    }

    @Test
    public void testIsNew() {

        when(httpSession.isNew()).thenReturn(true);

        assertThat(session.isNew()).isTrue();
    }

    @Test
    public void testRemoveAttribute() {

        session.removeAttribute("name");

        verify(httpSession).removeAttribute("name");
    }
}
