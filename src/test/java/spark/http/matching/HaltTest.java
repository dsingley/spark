package spark.http.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import spark.HaltException;
import spark.Service;

class HaltTest {

    @Test
    void modify_whenHaltHasBody_thenSetsStatusAndBody() {
        var halt = catchThrowableOfType(() -> Service.ignite().halt(401, "Go away"), HaltException.class);
        var httpResponse = mock(HttpServletResponse.class);
        var body = Body.create();

        Halt.modify(httpResponse, body, halt);

        verify(httpResponse).setStatus(401);
        assertThat(body.get()).isEqualTo("Go away");
    }

    @Test
    void modify_whenHaltHasNoBody_thenSetsStatusAndEmptyBody() {
        var halt = catchThrowableOfType(() -> Service.ignite().halt(404), HaltException.class);
        var httpResponse = mock(HttpServletResponse.class);
        var body = Body.create();

        Halt.modify(httpResponse, body, halt);

        verify(httpResponse).setStatus(404);
        assertThat(body.get()).isEqualTo("");
    }

}
