package spark.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;
import spark.RouteImpl;
import spark.ExceptionMapper;
import spark.globalstate.ServletFlag;
import spark.route.HttpMethod;
import spark.route.ServletRoutes;
import spark.staticfiles.StaticFilesConfiguration;

class ServletStateResetTest {

    @AfterEach
    void tearDown() {
        ServletStateReset.reset();
    }

    @Test
    void reset_shouldClearAllServletState() {
        setUpServletState();

        ServletStateReset.reset();

        assertServletStateIsClear();
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

    @Test
    void theExtension_shouldResetBeforeAndAfterAllTests() {
        var extension = new ServletStateReset();
        var context = Mockito.mock(ExtensionContext.class);

        setUpServletState();
        extension.beforeAll(context);
        assertServletStateIsClear();

        setUpServletState();
        extension.afterAll(context);
        assertServletStateIsClear();
    }

    private static void setUpServletState() {
        ServletFlag.runFromServlet();
        ServletRoutes.get().add(HttpMethod.get, RouteImpl.create("/reset-test", (request, response) -> "hi"));
        StaticFilesConfiguration.servletInstance.configure("/public");
        ExceptionMapper.getServletInstance().map(IllegalStateException.class, null);

        assertThat(ServletRoutes.get().findMultiple(HttpMethod.get, "/reset-test", null)).isNotEmpty();
    }

    private static void assertServletStateIsClear() {
        assertAll(
                () -> assertThat(ServletFlag.isRunningFromServlet()).isFalse(),
                () -> assertThat(StaticFilesConfiguration.servletInstance.isStaticResourcesSet()).isFalse(),
                () -> assertThat(ExceptionMapper.getServletInstance().getHandler(IllegalStateException.class)).isNull(),
                () -> assertThat(ServletRoutes.get().findMultiple(HttpMethod.get, "/reset-test", null)).isEmpty()
        );
    }
}
