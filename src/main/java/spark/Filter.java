package spark;

/**
 * Created by Per Wendel on 2014-05-10.
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
