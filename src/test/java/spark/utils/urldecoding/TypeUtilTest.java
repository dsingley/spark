package spark.utils.urldecoding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TypeUtilTest {

    @Test
    void convertHexDigit_char_whenValidDigits_thenReturnsCorrectValues() {
        assertAll(
                () -> assertThat(TypeUtil.convertHexDigit('0')).isZero(),
                () -> assertThat(TypeUtil.convertHexDigit('9')).isEqualTo(9),
                () -> assertThat(TypeUtil.convertHexDigit('a')).isEqualTo(10),
                () -> assertThat(TypeUtil.convertHexDigit('f')).isEqualTo(15),
                () -> assertThat(TypeUtil.convertHexDigit('A')).isEqualTo(10),
                () -> assertThat(TypeUtil.convertHexDigit('F')).isEqualTo(15)
        );
    }

    @Test
    void convertHexDigit_int_whenValidDigits_thenReturnsCorrectValues() {
        assertAll(
                () -> assertThat(TypeUtil.convertHexDigit((int) '0')).isZero(),
                () -> assertThat(TypeUtil.convertHexDigit((int) '9')).isEqualTo(9),
                () -> assertThat(TypeUtil.convertHexDigit((int) 'a')).isEqualTo(10),
                () -> assertThat(TypeUtil.convertHexDigit((int) 'f')).isEqualTo(15),
                () -> assertThat(TypeUtil.convertHexDigit((int) 'A')).isEqualTo(10),
                () -> assertThat(TypeUtil.convertHexDigit((int) 'F')).isEqualTo(15)
        );
    }

    // These specific characters (':' through '?' following '9', and '@'/'`' preceding
    // 'A'/'a') used to alias onto valid hex digit values via the old bit-trick
    // implementation - see issue #248.
    @ParameterizedTest
    @ValueSource(chars = {':', ';', '<', '=', '>', '?', '@', '`'})
    void convertHexDigit_char_whenCharacterAdjacentToValidRanges_thenThrows(char c) {
        assertThatThrownBy(() -> TypeUtil.convertHexDigit(c))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("'" + c + "' is not a valid hex digit");
    }

    @ParameterizedTest
    @ValueSource(chars = {':', ';', '<', '=', '>', '?', '@', '`'})
    void convertHexDigit_int_whenCharacterAdjacentToValidRanges_thenThrows(char c) {
        int codePoint = c;
        assertThatThrownBy(() -> TypeUtil.convertHexDigit(codePoint))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("'" + codePoint + "' is not a valid hex digit");
    }

    @Test
    void convertHexDigit_whenClearlyInvalidCharacter_thenThrows() {
        assertAll(
                () -> assertThatThrownBy(() -> TypeUtil.convertHexDigit('g'))
                        .isInstanceOf(NumberFormatException.class)
                        .hasMessage("'g' is not a valid hex digit"),
                () -> assertThatThrownBy(() -> TypeUtil.convertHexDigit(' '))
                        .isInstanceOf(NumberFormatException.class)
                        .hasMessage("' ' is not a valid hex digit")
        );
    }

    @Test
    void parseInt_whenValidHex_thenReturnsParsedValue() {
        assertAll(
                () -> assertThat(TypeUtil.parseInt("ff", 0, 2, 16)).isEqualTo(255),
                () -> assertThat(TypeUtil.parseInt("1A", 0, -1, 16)).isEqualTo(26)
        );
    }

    @Test
    void parseInt_whenInvalidHex_thenThrows() {
        assertThatThrownBy(() -> TypeUtil.parseInt("1G", 0, 2, 16))
                .isInstanceOf(NumberFormatException.class);
    }

}
