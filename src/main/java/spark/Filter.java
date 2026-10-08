package spark;

/**
 * Code that runs before or after the routes that handle a request, for example to check
 * authentication or to log requests. A filter is mapped with {@code before}, {@code after} or
 * {@code afterAfter}, on {@link Spark} or {@link Service}, to every path or to a given one. It can
 * stop the request by calling {@code halt}.
 */
@FunctionalInterface
public interface Filter {

    /**
     * Invoked when a request is made on this filter's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @throws java.lang.Exception when handle fails
     */
    // throws Exception is part of the public API; narrowing it would break existing implementations
    @SuppressWarnings("java:S112")
    void handle(Request request, Response response) throws Exception;

}
