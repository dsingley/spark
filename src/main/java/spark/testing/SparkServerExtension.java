package spark.testing;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;

import java.time.Duration;
import java.util.Objects;

/**
 * A JUnit Jupiter extension that starts and stops a Spark {@link Service} for tests. The server is
 * stopped automatically when the scope it was started in ends.
 * <p>
 * There are two ways to use it.
 *
 * <h2>Registered with {@code @RegisterExtension}</h2>
 * Create the extension with a {@link ServiceInitializer} that configures the server. A static field
 * starts one server for all the tests in the class, before any {@code @BeforeAll} method of the test
 * class runs; an instance field starts a new server for each test, before any {@code @BeforeEach}
 * method of the test class runs:
 * <pre>
 * class MyTest {
 *
 *     {@literal @}RegisterExtension
 *     static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
 *         http.port(56789);
 *         http.get("/ping", (request, response) -> "pong");
 *     });
 *
 *     // tests can use SPARK.port() or SPARK.service()
 * }
 * </pre>
 * Note that if the test class uses {@code @TestInstance(Lifecycle.PER_CLASS)}, there is only one test
 * instance, so an instance field behaves like a static field: one server is shared by all the tests.
 * <p>
 * An extension created with an initializer does not inject {@link SparkStarter} parameters.
 *
 * <h2>Injected with {@code @ExtendWith}</h2>
 * Annotate the test class with {@code @ExtendWith(SparkServerExtension.class)} and declare a
 * {@link SparkStarter} parameter on a lifecycle or test method. Call
 * {@link SparkStarter#runSpark(ServiceInitializer)} to start the server. Where the parameter is
 * declared determines the lifecycle: a static {@code @BeforeAll} method gives one server for the class,
 * a {@code @BeforeEach} method one server per test, and a {@code @Test} method a server for that test:
 * <pre>
 * {@literal @}ExtendWith(SparkServerExtension.class)
 * class MyTest {
 *
 *     {@literal @}BeforeAll
 *     static void startServer(SparkStarter starter) throws Exception {
 *         starter.runSpark(http -> {
 *             http.port(56789);
 *             http.get("/ping", (request, response) -> "pong");
 *         });
 *     }
 * }
 * </pre>
 * <p>
 * This class requires {@code junit-jupiter-api} on the classpath. It is an optional dependency of
 * spark-core, so it is not brought in transitively; code that uses this extension is a test and
 * therefore already has JUnit Jupiter available.
 */
public class SparkServerExtension implements ParameterResolver, BeforeAllCallback, BeforeEachCallback {

    /**
     * Starts a Spark {@link Service} and stops it when closed. Instances are injected into tests by
     * {@link SparkServerExtension}, and are also used internally by the registered form.
     */
    public static class SparkStarter implements AutoCloseable {

        private volatile Service service;
        private volatile boolean closed;

        /**
         * Ignites a new {@link Service}, passes it to the given initializer so that it can be
         * configured, and then blocks until the service has been initialized.
         * <p>
         * The initializer must define at least one route, because a service is only initialized once
         * its first route is defined. Otherwise this method fails with an {@link IllegalStateException}.
         * <p>
         * If the initializer throws, the service that was ignited is still stopped when this
         * instance is closed.
         *
         * @param initializer configures the service, e.g. sets the port and defines routes
         * @return this instance
         * @throws IllegalStateException if this instance already started a service, or the
         *                               initializer did not define any routes
         * @throws Exception             if the initializer throws
         */
        public SparkStarter runSpark(ServiceInitializer initializer) throws Exception {
            if (service != null) {
                throw new IllegalStateException("This SparkStarter has already started Spark");
            }

            var newService = Service.ignite();
            service = newService;
            initializer.init(newService);
            newService.awaitInitialization();
            return this;
        }

        /**
         * The running service.
         *
         * @return the service started by {@link #runSpark(ServiceInitializer)}
         * @throws IllegalStateException if no service was started, or it has been stopped
         */
        public Service service() {
            if (service == null || closed) {
                throw new IllegalStateException("Spark is not running");
            }
            return service;
        }

        /**
         * The port the running service is listening on.
         *
         * @return the port
         * @throws IllegalStateException if no service was started, or it has been stopped
         */
        public int port() {
            return service().port();
        }

        /**
         * Stops the service if one was started, and waits (for a bounded time) for it to finish
         * stopping, so that the next server can use the same port. Called by JUnit when the extension
         * context is closed, and does nothing if {@link #runSpark(ServiceInitializer)} was never called.
         */
        @Override
        public void close() {
            closed = true;
            if (service == null) {
                return;
            }

            service.stop();
            if (!service.awaitStop(STOP_TIMEOUT)) {
                LOG.warn("Spark did not stop within {}", STOP_TIMEOUT);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SparkServerExtension.class);

    // Normally the server stops in a few milliseconds; this only bounds a shutdown that is stuck
    private static final Duration STOP_TIMEOUT = Duration.ofSeconds(5);

    private static final Namespace NAMESPACE = create(SparkServerExtension.class);

    private final ServiceInitializer initializer;

    private volatile SparkStarter starter;

    /**
     * Creates an extension that only injects {@link SparkStarter} parameters. This is the constructor
     * JUnit uses for {@code @ExtendWith(SparkServerExtension.class)}.
     */
    public SparkServerExtension() {
        this.initializer = null;
    }

    /**
     * Creates an extension that starts a server configured by the given initializer. Register it with
     * {@code @RegisterExtension}, on a static field for one server per class or an instance field for
     * one server per test.
     *
     * @param initializer configures the service, e.g. sets the port and defines routes
     */
    public SparkServerExtension(ServiceInitializer initializer) {
        this.initializer = Objects.requireNonNull(initializer, "initializer must not be null");
    }

    /**
     * The service started by this extension.
     *
     * @return the running service
     * @throws IllegalStateException if this extension has no running server
     */
    public Service service() {
        if (starter == null) {
            throw new IllegalStateException("Spark is not running");
        }
        return starter.service();
    }

    /**
     * The port the service started by this extension is listening on.
     *
     * @return the port
     * @throws IllegalStateException if this extension has no running server
     */
    public int port() {
        return service().port();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        startServer(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        startServer(context);
    }

    private void startServer(ExtensionContext context) throws Exception {
        if (initializer == null) {
            return;
        }

        var store = context.getStore(NAMESPACE);

        // The store lookup also searches parent contexts, so a server started for the whole class
        // is reused by each test and by nested test classes.
        if (store.get(this, SparkStarter.class) != null) {
            return;
        }

        var newStarter = new SparkStarter();
        // Register before starting so the server is stopped even if the initializer throws
        store.put(this, newStarter);
        newStarter.runSpark(initializer);
        starter = newStarter;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return initializer == null && appliesTo(parameterContext.getParameter().getType());
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
