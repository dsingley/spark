package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class UnmapTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.get("/tobeunmapped", (q, a) -> "tobeunmapped");
    });

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void testUnmap() throws Exception {
        var response = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("tobeunmapped")
        );

        SPARK.service().unmap("/tobeunmapped");

        var afterUnmapResponse = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertThat(afterUnmapResponse.status).isEqualTo(404);

        SPARK.service().get("/tobeunmapped", (q, a) -> "tobeunmapped");

        var afterRemapResponse = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertAll(
                () -> assertThat(afterRemapResponse.status).isEqualTo(200),
                () -> assertThat(afterRemapResponse.body).isEqualTo("tobeunmapped")
        );

        SPARK.service().unmap("/tobeunmapped", "get");

        var afterUnmapByMethodResponse = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertThat(afterUnmapByMethodResponse.status).isEqualTo(404);
    }
}
