package spark.staticfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

/**
 * The MIME type settings of one Service do not affect another Service in the same JVM (#318).
 */
class MimeTypePerServiceTest {

    @RegisterExtension
    static final SparkServerExtension CUSTOMIZED = new SparkServerExtension(http -> {
        http.staticFiles.location("/public");
        http.staticFiles.registerMimeType("cxt", "custom-extension-type");
        http.staticFiles.registerMimeType("html", "text/x-custom-html");
        http.init();
    });

    @RegisterExtension
    static final SparkServerExtension NO_GUESSING = new SparkServerExtension(http -> {
        http.staticFiles.location("/public");
        http.staticFiles.disableMimeTypeGuessing();
        http.init();
    });

    @RegisterExtension
    static final SparkServerExtension DEFAULT = new SparkServerExtension(http -> {
        http.staticFiles.location("/public");
        http.init();
    });

    private static SparkTestUtil customized;
    private static SparkTestUtil noGuessing;
    private static SparkTestUtil defaults;

    @BeforeAll
    static void beforeAll() {
        customized = new SparkTestUtil(CUSTOMIZED.port());
        noGuessing = new SparkTestUtil(NO_GUESSING.port());
        defaults = new SparkTestUtil(DEFAULT.port());
    }

    @Test
    void registeredMimeTypesApplyOnlyToTheServiceThatRegisteredThem() {
        assertAll(
                () -> assertThat(contentType(customized, "/img/file.cxt")).isEqualTo("custom-extension-type"),
                () -> assertThat(contentType(defaults, "/img/file.cxt")).isEqualTo("application/octet-stream"),
                () -> assertThat(contentType(noGuessing, "/img/file.cxt")).isNull()
        );
    }

    @Test
    void overridingABuiltInMimeTypeApplyOnlyToTheServiceThatDidIt() {
        assertAll(
                () -> assertThat(contentType(customized, "/pages/index.html")).isEqualTo("text/x-custom-html"),
                () -> assertThat(contentType(defaults, "/pages/index.html")).startsWith("text/html")
        );
    }

    @Test
    void disablingGuessingAppliesOnlyToTheServiceThatDidIt() {
        assertAll(
                () -> assertThat(contentType(noGuessing, "/pages/index.html")).isNull(),
                () -> assertThat(contentType(customized, "/pages/index.html")).isNotNull(),
                () -> assertThat(contentType(defaults, "/pages/index.html")).isNotNull()
        );
    }

    private static String contentType(SparkTestUtil client, String path) throws Exception {
        return client.doMethod("GET", path, null).headers.get("Content-Type");
    }
}
