package spark;

/**
 * A group of routes that share a path prefix. Implementations define the routes (and any nested
 * groups) in {@link #addRoutes()}, and are passed to {@link Service#path(String, RouteGroup)},
 * which prefixes every route defined by the group with that path.
 */
@FunctionalInterface
public interface RouteGroup {

    /**
     * Defines the routes in this group. It is called while the group's path prefix is in effect, so
     * the routes are mapped relative to that prefix.
     */
    void addRoutes();
}