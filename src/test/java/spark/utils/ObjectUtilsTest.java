package spark.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ObjectUtilsTest {

    @Test
    void testIsEmpty_whenArrayIsEmpty() {

        assertThat(ObjectUtils.isEmpty(new Object[]{})).isTrue();

    }

    @Test
    void testIsEmpty_whenArrayIsNotEmpty() {

        assertThat(ObjectUtils.isEmpty(new Integer[]{1,2})).isFalse();

    }
}
