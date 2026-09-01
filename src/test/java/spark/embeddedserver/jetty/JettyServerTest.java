package spark.embeddedserver.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.jupiter.api.Test;
import org.kiwiproject.reflect.KiwiReflection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

public class JettyServerTest {
    @Test
    public void testCreateServer_useDefaults() {
        Server server = new JettyServer().create(0, 0, 0);

        QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();

        int minThreads = KiwiReflection.getTypedFieldValue(threadPool, "_minThreads", Integer.class);
        int maxThreads = KiwiReflection.getTypedFieldValue(threadPool, "_maxThreads", Integer.class);
        int idleTimeout = KiwiReflection.getTypedFieldValue(threadPool, "_idleTimeout", Integer.class);

        assertAll(
                () -> assertThat(minThreads).isEqualTo(8),
                () -> assertThat(maxThreads).isEqualTo(200),
                () -> assertThat(idleTimeout).isEqualTo(60000)
        );
    }

    @Test
    public void testCreateServer_whenNonDefaultMaxThreadsOnly_thenUseDefaultMinThreadsAndTimeout() {
        Server server = new JettyServer().create(9, 0, 0);

        QueuedThreadPool threadPool = (QueuedThreadPool) server.getThreadPool();

        int minThreads = KiwiReflection.getTypedFieldValue(threadPool, "_minThreads", Integer.class);
        int maxThreads = KiwiReflection.getTypedFieldValue(threadPool, "_maxThreads", Integer.class);
        int idleTimeout = KiwiReflection.getTypedFieldValue(threadPool, "_idleTimeout", Integer.class);

        assertAll(
                () -> assertThat(minThreads).isEqualTo(8),
                () -> assertThat(maxThreads).isEqualTo(9),
                () -> assertThat(idleTimeout).isEqualTo(60000)
        );

    }

    @Test
    public void testCreateServer_whenNonDefaultMaxThreads_isLessThanDefaultMinThreads() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JettyServer().create(2, 0, 0))
                .withMessage("max threads (2) less than min threads (8)");
    }
}
