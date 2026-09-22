package spark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Base64Test {

    @Test
    final void test_encode() {
        var in = "hello";
        var encode = Base64.encode(in);
        assertThat(in).isNotEqualTo(encode);
    }

    @Test
    final void test_encode_whenInputIsNull_thenReturnsNull() {
        assertThat(Base64.encode(null)).isNull();
    }

}
