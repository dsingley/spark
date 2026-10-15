package spark.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.jspecify.annotations.NonNull;
import org.kiwiproject.reflect.KiwiReflection;
import spark.ExceptionMapper;
import spark.globalstate.ServletFlag;
import spark.route.ServletRoutes;
import spark.staticfiles.StaticFilesConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Resets the static state that Spark keeps when it runs inside a servlet container, before and after
 * all tests in a class. Deploying {@link spark.servlet.SparkFilter} sets that state and nothing in Spark
 * clears it, so without a reset every later test class in the same JVM behaves as if it were running in
 * a servlet container. It is meant for test classes that deploy the filter.
 * <p>
 * It cannot undo everything. The static {@code Spark} facade creates its {@code Service} the first time it
 * is used, and a {@code Service} created while the flag is set keeps the servlet static files configuration
 * for good. An application deployed in a test must therefore use its own {@code Service.ignite()} instance
 * rather than the static {@code Spark} methods, as {@code MyApp} does, or it leaves that singleton behind
 * and the test class should run in its own fork.
 * <p>
 * Register it with {@code @ExtendWith(ServletStateReset.class)}. This is test-only code on purpose: a
 * public way to reset the servlet flag in {@code spark-core} could be called by an application running in
 * a servlet container, after which Spark would start an embedded server inside it.
 */
public class ServletStateReset implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(@NonNull ExtensionContext context) {
        reset();
    }

    @Override
    public void afterAll(@NonNull ExtensionContext context) {
        reset();
    }

    /**
     * Clears the servlet routes, static files, and exception mappings, and the flag that says Spark is
     * running from a servlet container.
     */
    public static void reset() {
        try {
            ServletRoutes.get().clear();
            StaticFilesConfiguration.servletInstance.clear();
            ExceptionMapper.getServletInstance().clear();
        } finally {
            // the flag does the most harm if it is left set, so it is reset even if clearing fails
            resetRunningFromServletFlag();
        }
    }

    /**
     * Sets the flag that says Spark is running from a servlet container back to false.
     */
    public static void resetRunningFromServletFlag() {
        // ServletFlag deliberately has no way to do this, so it takes reflection
        try {
            var field = ServletFlag.class.getDeclaredField("isRunningFromServlet");
            field.setAccessible(true);
            KiwiReflection.getTypedFieldValue(null, field, AtomicBoolean.class).set(false);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("ServletFlag no longer has a field named isRunningFromServlet", e);
        }
    }
}
