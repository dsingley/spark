package spark.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SparkUtilsTest {

    @Test
    public void testConvertRouteToList() throws Exception {

        List<String> expected = Arrays.asList("api", "person", ":id");

        List<String> actual = SparkUtils.convertRouteToList("/api/person/:id");

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void testIsParam_whenParameterFormattedAsParm() throws Exception {

        assertThat(SparkUtils.isParam(":param")).isTrue();

    }

    @Test
    public void testIsParam_whenParameterNotFormattedAsParm() throws Exception {

        assertThat(SparkUtils.isParam(".param")).isFalse();

    }


    @Test
    public void testIsSplat_whenParameterIsASplat() throws Exception {

        assertThat(SparkUtils.isSplat("*")).isTrue();

    }

    @Test
    public void testIsSplat_whenParameterIsNotASplat() throws Exception {

        assertThat(SparkUtils.isSplat("!")).isFalse();

    }
}
