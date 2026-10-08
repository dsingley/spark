package spark.staticfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

class StaticFilesConfigurationTest {

    @Test
    void configuring_shouldServeTheFile_andApplyTheCustomHeaders() throws IOException {
        var config = StaticFilesConfiguration.create();
        config.configure("/public");
        config.putCustomHeader("X-A", "1");
        config.putCustomHeaders(Map.of("X-B", "2", "X-A", "3"));
        config.setExpireTimeSeconds(60);
        var response = response();

        var consumed = config.consume(request(), response);

        assertThat(consumed).isTrue();
        assertAll(
                () -> verify(response).setHeader("X-A", "3"),
                () -> verify(response).setHeader("X-B", "2"),
                () -> verify(response).setHeader("Cache-Control", "private, max-age=60"),
                () -> verify(response).setHeader(eq("Expires"), anyString())
        );
    }

    @Test
    void configure_shouldOnlyAddTheFirstFolder() {
        var config = StaticFilesConfiguration.create();

        config.configure("/public");
        config.configure("/other");

        assertThat(config.isStaticResourcesSet()).isTrue();
        assertThat(config.isExternalStaticResourcesSet()).isFalse();
    }

    @Test
    void clear_shouldRemoveTheConfiguredFolders() throws IOException {
        var config = StaticFilesConfiguration.create();
        config.configure("/public");

        config.clear();

        assertAll(
                () -> assertThat(config.isStaticResourcesSet()).isFalse(),
                () -> assertThat(config.consume(request(), response())).isFalse()
        );
    }

    @Test
    void clear_shouldAllowTheFolderToBeConfiguredAgain() throws IOException {
        var config = StaticFilesConfiguration.create();
        config.configure("/public");
        config.clear();

        config.configure("/public");

        assertThat(config.consume(request(), response())).isTrue();
    }

    @Test
    void noMethodIsSynchronized_soCallersCannotInterfereByLockingTheInstance() {
        var synchronizedMethods = Arrays.stream(StaticFilesConfiguration.class.getDeclaredMethods())
                .filter(method -> Modifier.isSynchronized(method.getModifiers()))
                .map(Method::getName)
                .toList();

        assertThat(synchronizedMethods).isEmpty();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void changingTheConfiguration_isNotBlockedWhenACallerHoldsTheInstanceMonitor() throws Exception {
        var config = StaticFilesConfiguration.create();
        var monitorHeld = new CountDownLatch(1);
        var release = new CountDownLatch(1);

        var holder = new Thread(() -> {
            synchronized (config) {
                monitorHeld.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        holder.start();
        monitorHeld.await();

        try {
            // These would block until the monitor was released if the instance synchronized on itself
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
                config.configure("/public");
                config.putCustomHeader("X-A", "1");
                config.setExpireTimeSeconds(60);
                config.clear();
            });
        } finally {
            release.countDown();
            holder.join();
        }
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void servingFiles_whileTheConfigurationChanges_shouldNeverFail() throws Exception {
        var config = StaticFilesConfiguration.create();
        config.configure("/public");
        // Many headers, so that serving a file takes long enough for a change to land in the middle of it
        for (var i = 0; i < 500; i++) {
            config.putCustomHeader("X-Preset-" + i, "value");
        }

        // Warm up, so the request path runs at full speed from the start of the test, as it would on a
        // server that has been running for a while, which is when a change could land mid-request
        var warmUpRequest = request();
        var warmUpResponse = mock(HttpServletResponse.class, withSettings().stubOnly());
        stubOutputStream(warmUpResponse);
        for (var i = 0; i < 2_000; i++) {
            config.consume(warmUpRequest, warmUpResponse);
        }

        var failures = new ConcurrentLinkedQueue<Throwable>();
        var served = new AtomicInteger();
        var start = new CountDownLatch(1);
        var readers = 4;
        var readersDone = new CountDownLatch(readers);
        var executor = Executors.newFixedThreadPool(readers + 2);

        try {
            for (var reader = 0; reader < readers; reader++) {
                executor.submit(() -> {
                    var request = request();
                    var response = mock(HttpServletResponse.class, withSettings().stubOnly());
                    stubOutputStream(response);
                    awaitStart(start);
                    try {
                        for (var i = 0; i < 1_500; i++) {
                            try {
                                if (config.consume(request, response)) {
                                    served.incrementAndGet();
                                }
                            } catch (Throwable t) {
                                failures.add(t);
                            }
                        }
                    } finally {
                        readersDone.countDown();
                    }
                });
            }
            // Changes headers for as long as the readers are serving files
            executor.submit(() -> {
                awaitStart(start);
                for (var i = 0; readersDone.getCount() > 0; i++) {
                    try {
                        config.putCustomHeader("X-Header-" + (i % 50), "value-" + i);
                        config.setExpireTimeSeconds(i);
                    } catch (Throwable t) {
                        failures.add(t);
                    }
                }
            });
            // Clears and reconfigures the folder now and then
            executor.submit(() -> {
                awaitStart(start);
                while (readersDone.getCount() > 0) {
                    try {
                        config.clear();
                        config.configure("/public");
                        LockSupport.parkNanos(1_000_000);
                    } catch (Throwable t) {
                        failures.add(t);
                    }
                }
            });

            start.countDown();
            executor.shutdown();
            assertThat(executor.awaitTermination(50, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }

        assertAll(
                () -> assertThat(failures).isEmpty(),
                () -> assertThat(served).hasPositiveValue()
        );
    }

    private static void awaitStart(CountDownLatch start) {
        try {
            start.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static HttpServletRequest request() {
        var request = mock(HttpServletRequest.class, withSettings().stubOnly());
        when(request.getServletPath()).thenReturn("");
        when(request.getPathInfo()).thenReturn("/page.html");
        when(request.getHeaders("Accept-Encoding")).thenReturn(Collections.emptyEnumeration());
        return request;
    }

    private static HttpServletResponse response() {
        var response = mock(HttpServletResponse.class);
        stubOutputStream(response);
        return response;
    }

    private static void stubOutputStream(HttpServletResponse response) {
        try {
            when(response.getOutputStream()).thenReturn(new DiscardingOutputStream());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class DiscardingOutputStream extends ServletOutputStream {
        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // not used
        }

        @Override
        public void write(int b) {
            // discard
        }

        @Override
        public void write(byte[] b, int off, int len) {
            // discard
        }
    }
}
