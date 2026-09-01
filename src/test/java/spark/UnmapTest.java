package spark;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import spark.util.SparkTestUtil;

import static spark.Spark.awaitInitialization;
import static spark.Spark.get;

import static spark.Spark.unmap;

public class UnmapTest {

    SparkTestUtil testUtil = new SparkTestUtil(4567);

    @Test
    public void testUnmap() throws Exception {
        get("/tobeunmapped", (q, a) -> "tobeunmapped");
        awaitInitialization();

        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("tobeunmapped")
        );

        unmap("/tobeunmapped");

        SparkTestUtil.UrlResponse afterUnmap = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertThat(afterUnmap.status).isEqualTo(404);

        get("/tobeunmapped", (q, a) -> "tobeunmapped");

        SparkTestUtil.UrlResponse afterRemap = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertAll(
                () -> assertThat(afterRemap.status).isEqualTo(200),
                () -> assertThat(afterRemap.body).isEqualTo("tobeunmapped")
        );

        unmap("/tobeunmapped", "get");

        SparkTestUtil.UrlResponse afterUnmapByMethod = testUtil.doMethod("GET", "/tobeunmapped", null);
        assertThat(afterUnmapByMethod.status).isEqualTo(404);
    }
}
