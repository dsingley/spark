package spark;

/**
 * The code that handles the requests to a path, and returns the content of the response. A route is
 * mapped to an HTTP method and a path, for example:
 * <pre>
 * get("/hello", (request, response) -> "Hello World");
 * </pre>
 */
@FunctionalInterface
public interface Route {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @return The content to be set in the response
     * @throws java.lang.Exception implementation can choose to throw exception
     */
    // throws Exception is part of the public API; narrowing it would break existing implementations
    @SuppressWarnings("java:S112")
    Object handle(Request request, Response response) throws Exception;

}
