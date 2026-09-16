package spark.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClassPathResourceTest {

    @Test
    void testConstructor_whenPathIsValid_thenSucceeds() {
        var resource = new ClassPathResource("public/page.html");

        assertThat(resource.getPath()).isEqualTo("public/page.html");
    }

    @Test
    void testConstructor_whenPathIsNull_thenThrows() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClassPathResource(null))
                .withMessage("Path must not be null");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "..",
            "../",
            "../etc/passwd",
            "a/../..",
            "public/../../etc/passwd",
            "WEB-INF/web.xml",
            "META-INF/MANIFEST.MF",
    })
    void testConstructor_whenPathIsInvalid_thenThrows(String path) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClassPathResource(path))
                .withMessage("Path is not valid");
    }

    @Test
    void testConstructor_whenPathIsJustParentDirectoryTraversal_thenThrows() {
        // the specific case that was previously missed: "../" was rejected but a bare
        // ".." (no trailing slash) was not, even though it means the same thing
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClassPathResource(".."))
                .withMessage("Path is not valid");
    }

    @Test
    void testInternalConstructor_whenPathIsInvalid_thenThrows() {
        // the 3-arg constructor (used internally by createRelative) previously
        // performed no validation at all
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ClassPathResource("..", null, null))
                .withMessage("Path is not valid");
    }

    @Test
    void testInternalConstructor_whenPathIsValid_thenSucceeds() {
        var resource = new ClassPathResource("public/page.html", null, null);

        assertThat(resource.getPath()).isEqualTo("public/page.html");
    }

    @Test
    void testCreateRelative_whenResultingPathIsJustTraversal_thenThrows() {
        var resource = new ClassPathResource("public/page.html");

        // "public/page.html" combined with "../.." resolves (after cleaning) to
        // exactly "..", the same bare-traversal case as above, reached via
        // createRelative()'s call into the previously-unvalidated 3-arg constructor
        assertThatIllegalArgumentException()
                .isThrownBy(() -> resource.createRelative("../.."))
                .withMessage("Path is not valid");
    }

    @Test
    void testCreateRelative_whenRelativePathIsValid_thenSucceeds() {
        var resource = new ClassPathResource("public/page.html");

        var relative = (ClassPathResource) resource.createRelative("other.html");

        assertThat(relative.getPath()).isEqualTo("public/other.html");
    }

}
