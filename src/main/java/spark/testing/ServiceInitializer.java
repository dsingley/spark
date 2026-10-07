package spark.testing;

import spark.Service;

/**
 * Configures a newly ignited Spark {@link Service}: its port, IP address, security, routes, filters, etc.
 * <p>
 * Things like the port and IP address must be configured before routes, because defining the first
 * route initializes the server.
 * <p>
 * This is a functional interface so that it can be written as a lambda, for example:
 * <pre>{@code
 * http -> {
 *     http.port(56789);
 *     http.get("/ping", (request, response) -> "pong");
 * }
 * }</pre>
 */
@FunctionalInterface
public interface ServiceInitializer {

    /**
     * Configure the given service.
     *
     * @param service the newly ignited service
     * @throws Exception if the service cannot be configured, e.g., a keystore cannot be loaded
     */
    void init(Service service) throws Exception;
}
