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
    void registerMimeType_shouldRejectANullExtension() {
        var config = StaticFilesConfiguration.create();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> config.registerMimeType(null, "application/x-spark-test"))
                .withMessage("'extension' must not be null");
    }

    @Test
    void registerMimeType_shouldRejectANullMimeType() {
        var config = StaticFilesConfiguration.create();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> config.registerMimeType("spark-test-null", null))
                .withMessage("'mimeType' must not be null");
    }

    @Test
    void registerMimeType_shouldMapTheExtension() {
        var config = StaticFilesConfiguration.create();
        config.registerMimeType("spark-test-one", "application/x-spark-test-one");

        assertThat(config.mimeTypeFor("/files/data.spark-test-one")).isEqualTo("application/x-spark-test-one");
    }

    @Test
    void registerMimeType_shouldOverrideABuiltInMapping() {
        var config = StaticFilesConfiguration.create();
        config.registerMimeType("json", "application/x-custom-json");

        assertThat(config.mimeTypeFor("data.json")).isEqualTo("application/x-custom-json");
        assertThat(MimeType.fromPathInfo("data.json")).isEqualTo("application/json");
    }

    @Test
    void registerMimeType_shouldOnlyAffectTheConfigurationItWasCalledOn() {
        var first = StaticFilesConfiguration.create();
        var second = StaticFilesConfiguration.create();

        first.registerMimeType("spark-test-two", "application/x-spark-test-two");

        assertThat(first.mimeTypeFor("data.spark-test-two")).isEqualTo("application/x-spark-test-two");
        assertThat(second.mimeTypeFor("data.spark-test-two")).isEqualTo("application/octet-stream");
    }

    @Test
    void fromPathInfo_shouldFallBackToOctetStream_forAnUnknownExtension() {
        assertThat(MimeType.fromPathInfo("/files/data.spark-test-unknown")).isEqualTo("application/octet-stream");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void registerMimeType_shouldNotLoseMappings_whenCalledFromSeveralThreadsAtOnce() throws Exception {
        var config = StaticFilesConfiguration.create();
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
                        config.registerMimeType("spark-test-" + threadNumber + "-" + i, "application/x-" + threadNumber + "-" + i);
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
                if (!config.mimeTypeFor("/f." + extension).equals("application/x-" + thread + "-" + i)) {
                    missing.add(extension);
                }
            }
        }

        assertThat(missing).describedAs("registrations that were lost").isEmpty();
    }
}
