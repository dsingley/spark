package spark.testing;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import spark.Service;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A JUnit Jupiter extension that starts and stops a Spark {@link Service} for tests.
 * <p>
 * The extension injects a {@link SparkStarter} into any test lifecycle method or test method
 * that declares one as a parameter. Calling {@link SparkStarter#runSpark(Consumer)} ignites a
 * new {@code Service}, lets you configure it (port, routes, filters, security, etc.), and
 * waits until it has initialized. The server is stopped automatically when the extension
 * context that the {@code SparkStarter} was created in is closed.
 * <p>
 * Where you inject the {@code SparkStarter} determines the lifecycle of the server. In a
 * static {@code @BeforeAll} method, one server is used for all tests in the class:
 * <pre>{@code
 * @ExtendWith(SparkServerExtension.class)
 * class MyTest {
 *
 *     @BeforeAll
 *     static void startServer(SparkStarter starter) {
 *         starter.runSpark(http -> {
 *             http.port(0);
 *             http.get("/ping", (request, response) -> "pong");
 *         });
 *     }
 *
 *     // tests...
 * }
 * }</pre>
 * In a {@code @BeforeEach} method, a new server is started and stopped for each test.
 * <p>
 * This class requires {@code junit-jupiter-api} on the classpath. It is an optional
 * dependency of spark-core, so it is not brought in transitively; code that uses this
 * extension is a test and therefore already has JUnit Jupiter available.
 */
public class SparkServerExtension implements ParameterResolver {

    /**
     * Starts a Spark {@link Service} and stops it when closed. Instances are created by
     * {@link SparkServerExtension} and injected as test parameters.
     */
    public static class SparkStarter implements AutoCloseable {

        private Service service;

        /**
         * Ignites a new {@link Service}, passes it to the given consumer so that it can be
         * configured, and then blocks until the service has been initialized.
         *
         * @param consumer configures the service, e.g. sets the port and defines routes
         * @return this instance
         */
        public SparkStarter runSpark(Consumer<Service> consumer) {
            service = Service.ignite();
            consumer.accept(service);
            service.awaitInitialization();
            return this;
        }

        /**
         * Stops the service if one was started. Called by JUnit when the extension context
         * is closed, and does nothing if {@link #runSpark(Consumer)} was never called.
         */
        @Override
        public void close() {
            Optional.ofNullable(service).ifPresent(Service::stop);
        }

    }

    private static final Namespace NAMESPACE = create(SparkServerExtension.class);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return appliesTo(parameterContext.getParameter().getType());
    }

    private boolean appliesTo(Class<?> type) {
        return type == SparkStarter.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE)
                .computeIfAbsent(parameterContext, key -> new SparkStarter(), SparkStarter.class);
    }

}
