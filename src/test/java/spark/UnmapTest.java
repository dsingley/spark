package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.unmap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.util.SparkStopExtension;
import spark.util.SparkTestUtil;

@ExtendWith(SparkStopExtension.class)
class UnmapTest {

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(4567);

        get("/tobeunmapped", (q, a) -> "tobeunmapped");
        awaitInitialization();
    }

    @Test
    void testUnmap() throws Exception {
        var response = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("tobeunmapped")
        );

        unmap("/tobeunmapped");

        var afterUnmapResponse = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertThat(afterUnmapResponse.status).isEqualTo(404);

        get("/tobeunmapped", (q, a) -> "tobeunmapped");

        var afterRemapResponse = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertAll(
                () -> assertThat(afterRemapResponse.status).isEqualTo(200),
                () -> assertThat(afterRemapResponse.body).isEqualTo("tobeunmapped")
        );

        unmap("/tobeunmapped", "get");

        var afterUnmapByMethodResponse = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertThat(afterUnmapByMethodResponse.status).isEqualTo(404);
    }
}
