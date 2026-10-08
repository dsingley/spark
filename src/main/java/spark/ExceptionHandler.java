package spark;

/**
 * Handles an exception that is thrown while a request is being handled, by writing a response for
 * it. A handler is mapped to an exception type with {@code exception(Class, ExceptionHandler)}, on
 * {@link Spark} or {@link Service}, and then also handles subclasses of that type.
 *
 * @param <T> the type of exception this handler handles
 */
@FunctionalInterface
public interface ExceptionHandler<T extends Exception> {

    /**
     * Invoked when an exception that is mapped to this handler occurs during routing
     *
     * @param exception The exception that was thrown during routing
     * @param request   The request object providing information about the HTTP request
     * @param response  The response object providing functionality for modifying the response
     */
    void handle(T exception, Request request, Response response);
}
