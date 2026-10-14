package spark.globalstate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.util.ServletStateReset;

class ServletFlagTest {

    @BeforeEach
    void setUp() {
        ServletStateReset.resetRunningFromServletFlag();
    }

    @AfterEach
    void tearDown() {
        // do not leave the flag set for the test classes that run after this one in the same JVM
        ServletStateReset.resetRunningFromServletFlag();
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
}
