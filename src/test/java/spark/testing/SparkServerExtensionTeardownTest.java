package spark.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        static void startServer(SparkStarter starter) {
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
        void startServer(SparkStarter starter) {
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

    @Test
    void shouldStopServerAfterAllTests_WhenStartedInBeforeAll() {
        var summary = execute(BeforeAllFixture.class);

        assertThat(summary.getTestsSucceededCount()).isEqualTo(1);
        assertThat(summary.getTestsFailedCount()).isZero();

        assertStopped(BeforeAllFixture.service, BeforeAllFixture.PORT);
    }

    @Test
    void shouldStopServerAfterEachTest_WhenStartedInBeforeEach() {
        var summary = execute(BeforeEachFixture.class);

        assertThat(summary.getTestsSucceededCount()).isEqualTo(2);
        assertThat(summary.getTestsFailedCount()).isZero();

        assertThat(BeforeEachFixture.SERVICES).hasSize(2);
        BeforeEachFixture.SERVICES.forEach(service -> assertStopped(service, BeforeEachFixture.PORT));
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
        assertThat(service.awaitStop(STOP_TIMEOUT))
                .describedAs("service on port %d should have been stopped", port)
                .isTrue();

        assertThatThrownBy(() -> new SparkTestUtil(port).get("/ping"))
                .isInstanceOf(IOException.class);
    }
}
