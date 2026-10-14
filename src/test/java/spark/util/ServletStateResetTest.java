package spark.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import spark.ExceptionMapper;
import spark.globalstate.ServletFlag;
import spark.route.ServletRoutes;
import spark.staticfiles.StaticFilesConfiguration;

class ServletStateResetTest {

    @AfterEach
    void tearDown() {
        ServletStateReset.reset();
    }

    @Test
    void reset_shouldClearAllServletState() {
        ServletFlag.runFromServlet();
        StaticFilesConfiguration.servletInstance.configure("/public");
        ExceptionMapper.getServletInstance().map(IllegalStateException.class, null);

        ServletStateReset.reset();

        assertAll(
                () -> assertThat(ServletFlag.isRunningFromServlet()).isFalse(),
                () -> assertThat(StaticFilesConfiguration.servletInstance.isStaticResourcesSet()).isFalse(),
                () -> assertThat(ExceptionMapper.getServletInstance().getHandler(IllegalStateException.class)).isNull(),
                () -> assertThat(ServletRoutes.get().findMultiple(spark.route.HttpMethod.get, "/anything", null)).isEmpty()
        );
    }

    @Test
    void resetRunningFromServletFlag_shouldOnlyClearTheFlag() {
        ServletFlag.runFromServlet();
        StaticFilesConfiguration.servletInstance.configure("/public");

        ServletStateReset.resetRunningFromServletFlag();

        assertAll(
                () -> assertThat(ServletFlag.isRunningFromServlet()).isFalse(),
                () -> assertThat(StaticFilesConfiguration.servletInstance.isStaticResourcesSet()).isTrue()
        );
    }
}
