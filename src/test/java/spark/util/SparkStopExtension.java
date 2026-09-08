package spark.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import spark.Spark;

import java.time.Duration;

/**
 * Stops the shared, static {@link Spark} instance after all tests in the class, with a
 * bounded wait rather than the unbounded {@code Spark.awaitStop()}. Needs no configuration,
 * since the static {@code Spark} facade always operates on a single instance regardless of
 * which test class uses it.
 * <p>Register with {@code @ExtendWith(SparkStopExtension.class)}.
 */
public class SparkStopExtension implements AfterAllCallback {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    @Override
    public void afterAll(ExtensionContext context) {
        Spark.stop();
        Spark.awaitStop(DEFAULT_TIMEOUT);
    }
}
