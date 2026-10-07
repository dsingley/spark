package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import spark.Service;
import spark.testing.SparkServerExtension.SparkStarter;
import spark.util.SparkTestUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs fixture test classes with the extension via the JUnit Launcher, and then verifies that
 * the servers they started were stopped when JUnit closed the extension context.
 */
class SparkServerExtensionTeardownTest {

    private static final Duration STOP_TIMEOUT = Duration.ofSeconds(5);

    @ExtendWith(SparkServerExtension.class)
    static class BeforeAllFixture {

        static final int PORT = 6544;
        static volatile Service service;

        @BeforeAll
        static void startServer(SparkStarter starter) throws Exception {
            starter.runSpark(http -> {
                http.ipAddress("127.0.0.1");
                http.port(PORT);
                http.get("/ping", (request, response) -> "pong");
                service = http;
            });
        }

        @Test
        void shouldBeRunningDuringTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }
    }

    @ExtendWith(SparkServerExtension.class)
    static class BeforeEachFixture {

        static final int PORT = 6545;
        static final List<Service> SERVICES = new CopyOnWriteArrayList<>();

        private Service service;

        @BeforeEach
        void startServer(SparkStarter starter) throws Exception {
            starter.runSpark(http -> {
                http.ipAddress("127.0.0.1");
                http.port(PORT);
                http.get("/ping", (request, response) -> "pong");
                service = http;
            });
            SERVICES.add(service);
        }

        @Test
        void shouldBeRunningDuringFirstTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }

        @Test
        void shouldBeRunningDuringSecondTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }
    }

    @ExtendWith(SparkServerExtension.class)
    static class TestMethodFixture {

        static final int PORT = 6547;
        static volatile Service service;

        @Test
        void shouldBeRunningDuringTest(SparkStarter starter) throws Exception {
            starter.runSpark(http -> {
                http.ipAddress("127.0.0.1");
                http.port(PORT);
                http.get("/ping", (request, response) -> "pong");
                service = http;
            });

            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }
    }

    static class RegisteredStaticFixture {

        static final int PORT = 6552;
        static final List<Service> SERVICES = new CopyOnWriteArrayList<>();

        @RegisterExtension
        static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
            http.ipAddress("127.0.0.1");
            http.port(PORT);
            http.get("/ping", (request, response) -> "pong");
            SERVICES.add(http);
        });

        @Test
        void shouldBeRunningDuringFirstTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }

        @Test
        void shouldBeRunningDuringSecondTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }
    }

    static class RegisteredInstanceFixture {

        static final int PORT = 6553;
        static final List<Service> SERVICES = new CopyOnWriteArrayList<>();

        @RegisterExtension
        final SparkServerExtension spark = new SparkServerExtension(http -> {
            http.ipAddress("127.0.0.1");
            http.port(PORT);
            http.get("/ping", (request, response) -> "pong");
            SERVICES.add(http);
        });

        @Test
        void shouldBeRunningDuringFirstTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }

        @Test
        void shouldBeRunningDuringSecondTest() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }
    }

    static class RegisteredNestedFixture {

        static final int PORT = 6554;
        static final AtomicInteger START_COUNT = new AtomicInteger();
        static final List<Service> SERVICES = new CopyOnWriteArrayList<>();

        @RegisterExtension
        static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
            http.ipAddress("127.0.0.1");
            http.port(PORT);
            http.get("/ping", (request, response) -> "pong");
            START_COUNT.incrementAndGet();
            SERVICES.add(http);
        });

        @Test
        void shouldBeRunningInOuterClass() throws Exception {
            assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
        }

        @Nested
        class Inner {

            @Test
            void shouldBeRunningInNestedClass() throws Exception {
                assertThat(new SparkTestUtil(PORT).get("/ping").body).isEqualTo("pong");
            }
        }
    }

    static class FailingInitializerFixture {

        static final int PORT = 6555;
        static final List<Service> SERVICES = new CopyOnWriteArrayList<>();

        @RegisterExtension
        static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
            http.ipAddress("127.0.0.1");
            http.port(PORT);
            http.get("/ping", (request, response) -> "pong");
            http.awaitInitialization();
            SERVICES.add(http);
            throw new IOException("initializer failed");
        });

        @Test
        void shouldNotRun() {
            // never runs because the extension fails in beforeAll
        }
    }

    @Test
    void shouldStopServerAfterAllTests_WhenStartedInBeforeAll() {
        var summary = execute(BeforeAllFixture.class);

        assertAll(
                () -> assertThat(summary.getTestsSucceededCount()).isEqualTo(1),
                () -> assertThat(summary.getTestsFailedCount()).isZero()
        );

        assertStopped(BeforeAllFixture.service, BeforeAllFixture.PORT);
    }

    @Test
    void shouldStopServerAfterEachTest_WhenStartedInBeforeEach() {
        var summary = execute(BeforeEachFixture.class);

        assertAll(
                () -> assertThat(summary.getTestsSucceededCount()).isEqualTo(2),
                () -> assertThat(summary.getTestsFailedCount()).isZero()
        );

        assertThat(BeforeEachFixture.SERVICES).hasSize(2);
        BeforeEachFixture.SERVICES.forEach(service -> assertStopped(service, BeforeEachFixture.PORT));
    }

    @Test
    void shouldStopServerAfterTest_WhenStartedInTestMethod() {
        var summary = execute(TestMethodFixture.class);

        assertAll(
                () -> assertThat(summary.getTestsSucceededCount()).isEqualTo(1),
                () -> assertThat(summary.getTestsFailedCount()).isZero()
        );

        assertStopped(TestMethodFixture.service, TestMethodFixture.PORT);
    }

    @Test
    void shouldStopServerAfterAllTests_WhenRegisteredOnStaticField() {
        var summary = execute(RegisteredStaticFixture.class);

        assertAll(
                () -> assertThat(summary.getTestsSucceededCount()).isEqualTo(2),
                () -> assertThat(summary.getTestsFailedCount()).isZero()
        );

        assertThat(RegisteredStaticFixture.SERVICES).hasSize(1);
        assertStopped(RegisteredStaticFixture.SERVICES.get(0), RegisteredStaticFixture.PORT);
    }

    @Test
    void shouldStopServerAfterEachTest_WhenRegisteredOnInstanceField() {
        var summary = execute(RegisteredInstanceFixture.class);

        assertAll(
                () -> assertThat(summary.getTestsSucceededCount()).isEqualTo(2),
                () -> assertThat(summary.getTestsFailedCount()).isZero()
        );

        assertThat(RegisteredInstanceFixture.SERVICES).hasSize(2);
        RegisteredInstanceFixture.SERVICES.forEach(service -> assertStopped(service, RegisteredInstanceFixture.PORT));
    }

    @Test
    void shouldStartOnlyOneServer_WhenRegisteredOnStaticFieldAndTestsAreNested() {
        var summary = execute(RegisteredNestedFixture.class);

        assertAll(
                () -> assertThat(summary.getTestsSucceededCount()).isEqualTo(2),
                () -> assertThat(summary.getTestsFailedCount()).isZero()
        );

        assertThat(RegisteredNestedFixture.START_COUNT).hasValue(1);
        assertStopped(RegisteredNestedFixture.SERVICES.get(0), RegisteredNestedFixture.PORT);
    }

    @Test
    void shouldStopServer_WhenInitializerThrows() {
        var summary = execute(FailingInitializerFixture.class);

        assertAll(
                () -> assertThat(summary.getContainersFailedCount()).isEqualTo(1),
                () -> assertThat(summary.getFailures())
                        .singleElement()
                        .satisfies(failure -> assertThat(failure.getException())
                                .isExactlyInstanceOf(IOException.class)
                                .hasMessage("initializer failed"))
        );

        assertThat(FailingInitializerFixture.SERVICES).hasSize(1);
        assertStopped(FailingInitializerFixture.SERVICES.get(0), FailingInitializerFixture.PORT);
    }

    @Test
    void shouldNotFailToClose_WhenSparkWasNeverStarted() {
        assertThatCode(() -> new SparkStarter().close()).doesNotThrowAnyException();
    }

    private static TestExecutionSummary execute(Class<?> fixtureClass) {
        var request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(fixtureClass))
                .build();
        var listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(request, listener);
        return listener.getSummary();
    }

    private static void assertStopped(Service service, int port) {
        assertAll(
                () -> assertThat(service.awaitStop(STOP_TIMEOUT))
                        .describedAs("service on port %d should have been stopped", port)
                        .isTrue(),
                () -> assertThatThrownBy(() -> new SparkTestUtil(port).get("/ping"))
                        .isInstanceOf(IOException.class)
        );
    }
}
