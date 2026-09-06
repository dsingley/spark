package spark.globalstate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

class ServletFlagTest {

    @BeforeEach
    void setUp() {
        KiwiReflection.setFieldValue(null, isRunningFromServletField(), new AtomicBoolean(false));
    }

    @Test
    void testRunFromServlet_whenDefault() {

        AtomicBoolean isRunningFromServlet = KiwiReflection.getTypedFieldValue(null, isRunningFromServletField(), AtomicBoolean.class);
        assertThat(isRunningFromServlet.get()).isFalse();
    }

    @Test
    void testRunFromServlet_whenExecuted() {

        ServletFlag.runFromServlet();
        AtomicBoolean isRunningFromServlet = KiwiReflection.getTypedFieldValue(null, isRunningFromServletField(), AtomicBoolean.class);

        assertThat(isRunningFromServlet.get()).isTrue();
    }

    @Test
    void testIsRunningFromServlet_whenDefault() {

        assertThat(ServletFlag.isRunningFromServlet()).isFalse();

    }

    @Test
    void testIsRunningFromServlet_whenRunningFromServlet() {

        ServletFlag.runFromServlet();
        assertThat(ServletFlag.isRunningFromServlet()).isTrue();
    }

    private static Field isRunningFromServletField() {
        return KiwiReflection.findField(new ServletFlag(), "isRunningFromServlet");
    }
}
