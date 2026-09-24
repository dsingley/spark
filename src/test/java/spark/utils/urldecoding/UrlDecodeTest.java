package spark.utils.urldecoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class UrlDecodeTest {

    @Test
    void path_whenNoSpecialCharacters_thenReturnsOriginalString() {
        assertThat(UrlDecode.path("/hello/world")).isEqualTo("/hello/world");
    }

    @Test
    void path_whenPercentEscapedByte_thenDecodesIt() {
        assertThat(UrlDecode.path("/hello%20world")).isEqualTo("/hello world");
    }

    @Test
    void path_whenPercentUEscapedCodepoint_thenDecodesIt() {
        assertThat(UrlDecode.path("/caf%u00e9")).isEqualTo("/café");
    }

    @Test
    void path_whenSemicolonPathParamFollowedBySlash_thenStripsToSlash() {
        assertThat(UrlDecode.path("/foo;jsessionid=123/bar")).isEqualTo("/foo/bar");
    }

    @Test
    void path_whenSemicolonPathParamAtEnd_thenStripsToEnd() {
        assertThat(UrlDecode.path("/foo;jsessionid=123")).isEqualTo("/foo");
    }

    @Test
    void path_withOffsetAndLength_thenDecodesOnlyThatRange() {
        assertThat(UrlDecode.path("XX/hello%20worldYY", 2, 15)).isEqualTo("/hello worldY");
    }

    @Test
    void path_whenTruncatedPercentEscape_thenThrowsBadUriEncoding() {
        // Only one character follows the '%' - not enough for either a %XX byte escape or a
        // %uXXXX codepoint escape. Already handled correctly today.
        assertThatThrownBy(() -> UrlDecode.path("/foo%1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bad URI % encoding: %1");
    }

    @Test
    void path_whenTruncatedPercentUEscape_thenThrowsBadUriUEncoding() {
        // "%u12" has only 2 of the 4 hex digits a %u escape requires. The existing bounds
        // check before this branch only confirms 2 more characters exist after the '%', which
        // is enough for a %XX escape but not a %uXXXX one - see issue #260.
        assertThatThrownBy(() -> UrlDecode.path("/foo%u12"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bad URI %u encoding: %u12");
    }

    @Test
    void path_whenInvalidUtf8Byte_thenThrowsCannotDecodeUri() {
        // %FF is never a valid UTF-8 lead byte. This used to silently fall back to a
        // separately-broken ISO-8859-1 reinterpretation (see issue #262) - now removed in
        // favor of matching Jetty's own current approach: reject non-UTF-8 percent-encoded
        // input outright, rather than accept it under a second, ambiguous interpretation.
        assertThatThrownBy(() -> UrlDecode.path("/caf%FF"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("cannot decode URI")
                .cause()
                .isInstanceOf(Utf8Appendable.NotUtf8Exception.class);
    }

    @Test
    void path_whenPercentUEscapedLoneHighSurrogate_thenReplacesWithReplacementChar() {
        // 0xD83D is only ever valid as the first half of a surrogate pair, never a
        // standalone character - see issue #243. Encoding it alone to UTF-8, as Jetty's
        // current decodePath() does, replaces it with '?' rather than letting the raw,
        // invalid surrogate code unit through unvalidated.
        assertThat(UrlDecode.path("/%uD83D")).isEqualTo("/?");
    }

    @Test
    void path_whenPercentUEscapedLoneLowSurrogate_thenReplacesWithReplacementChar() {
        assertThat(UrlDecode.path("/%uDC00")).isEqualTo("/?");
    }

    @Test
    void path_whenPercentUEscapedSurrogatePair_thenEachHalfIsReplacedIndependently() {
        // %uD83D%uDE00 is a legitimate UTF-16 surrogate pair for U+1F600 (grinning face)
        // if read together, but each %uXXXX escape is decoded independently - matching
        // Jetty's own current behavior - so the pair is not reassembled. This is a known,
        // accepted tradeoff: the encoding is legacy/non-standard, and a supplementary
        // character split across two %u escapes is expected to be vanishingly rare in
        // real traffic compared to the value of rejecting unvalidated invalid surrogates.
        assertThat(UrlDecode.path("/%uD83D%uDE00")).isEqualTo("/??");
    }

}
