package spark.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.List;

class SparkUtilsTest {

    @Test
    void testConvertRouteToList() {

        var expected = List.of("api", "person", ":id");

        var actual = SparkUtils.convertRouteToList("/api/person/:id");

        assertThat(actual).isEqualTo(expected);

    }

    @Test
    void testIsParam_whenParameterFormattedAsParm() {

        assertThat(SparkUtils.isParam(":param")).isTrue();

    }

    @Test
    void testIsParam_whenParameterNotFormattedAsParm() {

        assertThat(SparkUtils.isParam(".param")).isFalse();

    }


    @Test
    void testIsSplat_whenParameterIsASplat() {

        assertThat(SparkUtils.isSplat("*")).isTrue();

    }

    @Test
    void testIsSplat_whenParameterIsNotASplat() {

        assertThat(SparkUtils.isSplat("!")).isFalse();

    }
}
