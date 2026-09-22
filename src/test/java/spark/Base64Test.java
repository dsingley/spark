package spark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class Base64Test {

    @Test
    void test_encode() {
        assertThat(Base64.encode("hello")).isEqualTo("aGVsbG8=");
    }

    @Test
    void test_encode_usesUrlSafeAlphabet() {
        // "???" is the shortest input whose standard base64 encoding ("Pz8/") contains
        // a character ('/') that the URL-safe alphabet replaces (with '_'), proving encode()
        // actually uses the URL-safe encoder rather than the standard one.
        assertThat(Base64.encode("???")).isEqualTo("Pz8_");
    }

    @Test
    void test_encode_whenInputIsNull_thenReturnsNull() {
        assertThat(Base64.encode(null)).isNull();
    }

}
