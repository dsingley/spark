package spark.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import spark.utils.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

class ResourceUtilsTest {

    @Test
    void testGetFile_whenURLProtocolIsNotFile_thenThrowFileNotFoundException() throws MalformedURLException {
        var url = new URL("http://example.com/");

        assertThatThrownBy(() -> ResourceUtils.getFile(url, "My File Path"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("My File Path cannot be resolved to absolute file path " +
                                     "because it does not reside in the file system: http://example.com/");
    }

    @Test
    void testGetFile_whenURLProtocolIsFile_thenReturnFileObject() throws
                                                                         MalformedURLException,
                                                                         FileNotFoundException,
                                                                         URISyntaxException {
        //given
        var url = new URL("file://public/file.txt");
        var file = ResourceUtils.getFile(url, "Some description");

        //then
        assertThat(new File(ResourceUtils.toURI(url).getSchemeSpecificPart())).isEqualTo(file);
    }

    @ParameterizedTest(name = "isUrl(\"{0}\") = {1}")
    @MethodSource("isUrlCases")
    void testIsUrl(String resourceLocation, boolean expected) {
        assertThat(ResourceUtils.isUrl(resourceLocation)).isEqualTo(expected);
    }

    private static Stream<Arguments> isUrlCases() {
        return Stream.of(
                // null is never a URL
                Arguments.of(null, false),

                // recognized pseudo URL and standard, absolute URLs
                Arguments.of("classpath:/some/resource.txt", true),
                Arguments.of("http://example.com/foo", true),

                // an absolute file: URL containing a space must still resolve, not throw
                // (the deprecated java.net.URL(String) constructor tolerated this;
                // java.net.URI does not unless the space is percent-encoded first)
                Arguments.of("file:/Users/some user/app.jar", true),

                // a plain relative path is a completely normal, non-exceptional input to
                // this method (it exists specifically to detect this case) and must
                // return false rather than propagate URI's "not absolute" complaint
                Arguments.of("some/relative/path", false),
                Arguments.of("has a space/path.txt", false),

                // strings that are not valid URI syntax at all, even after encoding
                // spaces, must also resolve to false rather than propagate
                Arguments.of("bad^caret", false),
                Arguments.of("50%zz off", false)
        );
    }

    @ParameterizedTest(name = "extractJarFileURL({0}) = {1}")
    @MethodSource("extractJarFileURLCases")
    void testExtractJarFileURL(URL jarUrl, String expectedUrl) throws MalformedURLException {
        assertThat(ResourceUtils.extractJarFileURL(jarUrl)).hasToString(expectedUrl);
    }

    private static Stream<Arguments> extractJarFileURLCases() throws MalformedURLException {
        return Stream.of(
                // no "!/" separator at all: the URL is returned unchanged
                Arguments.of(
                        new URL("http://example.com/foo"),
                        "http://example.com/foo"),

                // the ordinary case: a well-formed nested file: URL
                Arguments.of(
                        new URL("jar", "", "file:/some/path/my.jar!/some/Entry.class"),
                        "file:/some/path/my.jar"),

                // a jar file path containing a space must still resolve (percent-encoded),
                // not throw IllegalArgumentException the way a raw java.net.URI would
                Arguments.of(
                        new URL("jar", "", "file:/some/path with space/my.jar!/some/Entry.class"),
                        "file:/some/path%20with%20space/my.jar"),

                // no protocol in the original jar URL, e.g. a Windows path like
                // "jar:C:/mypath/myjar.jar!/..." - falls back to treating it as a
                // file system path rather than propagating MalformedURLException
                Arguments.of(
                        new URL("jar", "", "C:/mypath/myjar.jar!/some/Entry.class"),
                        "file:/C:/mypath/myjar.jar")
        );
    }

}
