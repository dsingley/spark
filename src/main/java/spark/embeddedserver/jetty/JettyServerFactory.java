package spark.embeddedserver.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * This interface can be implemented to provide custom Jetty server instances
 * with specific settings or features.
 */
public interface JettyServerFactory {
    /**
     * Creates a Jetty server.
     *
     * @param maxThreads          maxThreads
     * @param minThreads          minThreads
     * @param threadTimeoutMillis threadTimeoutMillis
     * @return a new jetty server instance
     */
    Server create(int maxThreads, int minThreads, int threadTimeoutMillis);

    /**
     * Creates a Jetty server using the given thread pool.
     *
     * @param threadPool the thread pool to use
     * @return a new jetty server instance
     */
    Server create(ThreadPool threadPool);
}
