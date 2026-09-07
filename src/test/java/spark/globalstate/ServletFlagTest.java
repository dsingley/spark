package spark.globalstate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;
import org.kiwiproject.reflect.RuntimeReflectionException;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

class ServletFlagTest {

    @BeforeEach
    void setUp() {
        resetIsRunningFromServlet();
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

    private static void resetIsRunningFromServlet() {
        var field = getIsRunningFromServletField();
        field.setAccessible(true);
        var fieldValue = KiwiReflection.getTypedFieldValue(null, field, AtomicBoolean.class);
        fieldValue.set(false);
    }

    private static Field getIsRunningFromServletField() {
        try {
            return ServletFlag.class.getDeclaredField("isRunningFromServlet");
        } catch (NoSuchFieldException e) {
            throw new RuntimeReflectionException(e);
        }
    }
}
