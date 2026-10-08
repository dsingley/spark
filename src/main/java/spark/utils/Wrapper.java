package spark.utils;

/**
 * Implemented by objects that wrap another object and can give it back. The route and filter
 * implementations that Spark creates implement it, so that the handler the application supplied can
 * be recovered: {@code RouteMatch.getTarget()} returns the wrapper, and {@link #delegate()} returns
 * the original {@link spark.Route} or {@link spark.Filter}.
 */
public interface Wrapper {

    /**
     * @return the underlying object this instance wraps
     */
    Object delegate();

}
