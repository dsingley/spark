package spark.route;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import spark.utils.SparkUtils;

class RouteEntryTest {

    @Test
    void testMatches_BeforeAndAllPaths() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.before;
        entry.path = SparkUtils.ALL_PATHS;

        assertThat(entry.matches(HttpMethod.before, SparkUtils.ALL_PATHS))
                .describedAs("""
				    Should return true because HTTP method is "Before",\
				     the methods of route and match request match,\
				     and the path provided is same as ALL_PATHS (+/*paths)""")
                .isTrue();
    }

    @Test
    void testMatches_AfterAndAllPaths() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.after;
        entry.path = SparkUtils.ALL_PATHS;

        assertThat(entry.matches(HttpMethod.after, SparkUtils.ALL_PATHS))
                .describedAs("Should return true because HTTP method is \"After\", the methods of route and match request match," +
                        " and the path provided is same as ALL_PATHS (+/*paths)")
                .isTrue();
    }

    @Test
    void testMatches_NotAllPathsAndDidNotMatchHttpMethod() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.post;
        entry.path = "/test";

        assertThat(entry.matches(HttpMethod.get, "/path")).isFalse();
    }

    @Test
    void testMatches_RouteDoesNotEndWithSlash() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test";

        assertThat(entry.matches(HttpMethod.get, "/test/"))
                .describedAs("Should return false because route path does not end with a slash, does not end with " +
                            "a wildcard, and the route pah supplied ends with a slash ")
                .isFalse();
    }

    @Test
    void testMatches_PathDoesNotEndInSlash() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/";

        assertThat(entry.matches(HttpMethod.get, "/test"))
                .describedAs("Should return false because route path ends with a slash while path supplied as parameter does" +
                            "not end with a slash")
                .isFalse();
    }

    @Test
    void testMatches_MatchingPaths() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/";

        assertThat(entry.matches(HttpMethod.get, "/test/")).isTrue();
    }

    @Test
    void testMatches_WithWildcardOnEntryPath() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/*";

        assertThat(entry.matches(HttpMethod.get, "/test/me")).isTrue();
    }

    @Test
    void testMatches_PathsDoNotMatch() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/me";

        assertThat(entry.matches(HttpMethod.get, "/test/other")).isFalse();
    }

    @Test
    void testMatches_longRoutePathWildcard() {

        var entry = new RouteEntry();
        entry.httpMethod = HttpMethod.get;
        entry.path = "/test/this/resource/*";

        assertThat(entry.matches(HttpMethod.get, "/test/this/resource/child/id")).isTrue();
    }

}
