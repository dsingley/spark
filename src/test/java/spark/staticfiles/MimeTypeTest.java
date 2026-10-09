package spark.staticfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MimeTypeTest {

    @Test
    void register_shouldRejectANullExtension() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MimeType.register(null, "application/x-spark-test"))
                .withMessage("'extension' must not be null");
    }

    @Test
    void register_shouldRejectANullMimeType() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MimeType.register("spark-test-null", null))
                .withMessage("'mimeType' must not be null");
    }

    @Test
    void register_shouldMapTheExtension() {
        MimeType.register("spark-test-one", "application/x-spark-test-one");

        assertThat(MimeType.fromPathInfo("/files/data.spark-test-one")).isEqualTo("application/x-spark-test-one");
    }

    @Test
    void fromPathInfo_shouldFallBackToOctetStream_forAnUnknownExtension() {
        assertThat(MimeType.fromPathInfo("/files/data.spark-test-unknown")).isEqualTo("application/octet-stream");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void register_shouldNotLoseMappings_whenCalledFromSeveralThreadsAtOnce() throws Exception {
        var threads = 8;
        var perThread = 1_000;
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(threads);
        var futures = new ArrayList<java.util.concurrent.Future<?>>();

        try {
            for (var thread = 0; thread < threads; thread++) {
                var threadNumber = thread;
                futures.add(executor.submit(() -> {
                    start.await();
                    for (var i = 0; i < perThread; i++) {
                        MimeType.register("spark-test-" + threadNumber + "-" + i, "application/x-" + threadNumber + "-" + i);
                    }
                    return null;
                }));
            }
            start.countDown();
            for (var future : futures) {
                future.get(50, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        var missing = new ArrayList<String>();
        for (var thread = 0; thread < threads; thread++) {
            for (var i = 0; i < perThread; i++) {
                var extension = "spark-test-" + thread + "-" + i;
                if (!MimeType.fromPathInfo("/f." + extension).equals("application/x-" + thread + "-" + i)) {
                    missing.add(extension);
                }
            }
        }

        assertThat(missing).describedAs("registrations that were lost").isEmpty();
    }
}
