package spark.staticfiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Map;

class MimeTypeTest {

    @Test
    void getMimeType_shouldResolveABuiltInMapping() {
        assertThat(MimeType.getMimeType("/files/data.json", Map.of())).isEqualTo("application/json");
    }

    @Test
    void getMimeType_shouldFallBackToOctetStream_forAnUnknownExtension() {
        assertThat(MimeType.getMimeType("/files/data.spark-test-unknown", Map.of())).isEqualTo("application/octet-stream");
    }

    @Test
    void getMimeType_shouldPreferARegisteredMappingOverABuiltInOne() {
        var registered = Map.of("json", "application/x-custom-json");

        assertThat(MimeType.getMimeType("data.json", registered)).isEqualTo("application/x-custom-json");
        assertThat(MimeType.getMimeType("data.xml", registered)).isEqualTo("application/xml");
    }
}
