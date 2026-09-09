package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.unmap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.util.SparkTestUtil;

class UnmapTest {

    private SparkTestUtil testUtil;

    @BeforeEach 
    void setUp() {
        testUtil = new SparkTestUtil(4567);
    }

    @Test
    void testUnmap() throws Exception {
        get("/tobeunmapped", (q, a) -> "tobeunmapped");
        awaitInitialization();

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
