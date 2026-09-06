package spark.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class MimeParseTest {

    @Test
    void testBestMatch() {

        final String header = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

        Collection<String> supported = List.of("application/xml", "text/html");

        assertThat(MimeParse.bestMatch(supported, header))
                .describedAs("""
				    bestMatch should return the supported mime type with the highest quality factor\
				     because it is preferred mime type\
				     as indicated in the HTTP header""")
                .isEqualTo("text/html");

    }

    @Test
    void testBestMatch_whenSupportedIsLowQualityFactor() {

        final String header = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

        Collection<String> supported = List.of("application/json");

        assertThat(MimeParse.bestMatch(supported, header))
                .describedAs("""
				    bestMatch should return the mime type even if it is not included in the supported\
				     mime types because it is considered by the */* all media type specified in the Accept\
				     Header""")
                .isEqualTo("application/json");

    }

}
