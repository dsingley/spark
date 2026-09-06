package spark.resource;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UriPathTest {

    @Test
    void canonical() {
        String[][] canonical = {
            {"/aaa/bbb/", "/aaa/bbb/"},
            {"/aaa//bbb/", "/aaa//bbb/"},
            {"/aaa///bbb/", "/aaa///bbb/"},
            {"/aaa/./bbb/", "/aaa/bbb/"},
            {"/aaa/../bbb/", "/bbb/"},
            {"/aaa/./../bbb/", "/bbb/"},
            {"/aaa/bbb/ccc/../../ddd/", "/aaa/ddd/"},
            {"./bbb/", "bbb/"},
            {"./aaa/../bbb/", "bbb/"},
            {"./", ""},
            {".//", ".//"},
            {".///", ".///"},
            {"/.", "/"},
            {"//.", "//"},
            {"///.", "///"},
            {"/", "/"},
            {"aaa/bbb", "aaa/bbb"},
            {"aaa/", "aaa/"},
            {"aaa", "aaa"},
            {"/aaa/bbb", "/aaa/bbb"},
            {"/aaa//bbb", "/aaa//bbb"},
            {"/aaa/./bbb", "/aaa/bbb"},
            {"/aaa/../bbb", "/bbb"},
            {"/aaa/./../bbb", "/bbb"},
            {"./bbb", "bbb"},
            {"./aaa/../bbb", "bbb"},
            {"aaa/bbb/..", "aaa/"},
            {"aaa/bbb/../", "aaa/"},
            {"/aaa//../bbb", "/aaa/bbb"},
            {"/aaa/./../bbb", "/bbb"},
            {"./", ""},
            {".", ""},
            {"", ""},
            {"..", null},
            {"./..", null},
            {"aaa/../..", null},
            {"/foo/bar/../../..", null},
            {"/../foo", null},
            {"/foo/.", "/foo/"},
            {"a", "a"},
            {"a/", "a/"},
            {"a/.", "a/"},
            {"a/..", ""},
            {"a/../..", null},
            {"/foo/../../bar", null},
            {"/foo/../bar//", "/bar//"},
        };

        for (String[] aCanonical : canonical) {
            assertThat(UriPath.canonical(aCanonical[0]))
                    .describedAs("canonical " + aCanonical[0])
                    .isEqualTo(aCanonical[1]);
        }
    }

}
