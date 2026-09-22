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
        // 'G' isn't a valid hex digit at all, so this throws from convertHexDigit's own
        // validation before parseInt's digit < 0 / digit >= base check is ever reached.
        assertThatThrownBy(() -> TypeUtil.parseInt("1G", 0, 2, 16))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("'G' is not a valid hex digit");
    }

    @Test
    void parseInt_whenValidHexDigitExceedsRequestedBase_thenThrowsWithParseIntsOwnMessage() {
        // 'a' is a valid hex digit (10), just too large for base 10 - the one path that
        // reaches parseInt's own throw line rather than convertHexDigit's.
        assertThatThrownBy(() -> TypeUtil.parseInt("9a", 0, 2, 10))
                .isInstanceOf(NumberFormatException.class)
                .hasMessage("'a' at index 1 is not a valid base-10 digit (in \"9a\")");
    }

    @Test
    void parseInt_whenNullString_thenThrows() {
        assertThatThrownBy(() -> TypeUtil.parseInt(null, 0, 2, 16))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("s must not be null");
    }

    @Test
    void parseInt_whenOffsetIsNegative_thenThrows() {
        assertThatThrownBy(() -> TypeUtil.parseInt("ff", -1, 2, 16))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("offset -1 is out of bounds for a string of length 2");
    }

    @Test
    void parseInt_whenOffsetExceedsStringLength_thenThrows() {
        assertThatThrownBy(() -> TypeUtil.parseInt("ff", 3, 2, 16))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("offset 3 is out of bounds for a string of length 2");
    }

    @Test
    void parseInt_whenRequestedLengthExceedsStringLength_thenThrowsWithClearMessage() {
        // offset=0, length=10 asks for far more characters than "9a" (length 2) actually
        // has. The old code built its exception message via s.substring(offset, offset +
        // length), which itself threw StringIndexOutOfBoundsException here rather than
        // reporting the real problem, masking it entirely - see issue #260. Now this is
        // caught immediately, before the parsing loop ever runs.
        assertThatThrownBy(() -> TypeUtil.parseInt("9a", 0, 10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("offset 0 + length 10 exceeds string length 2");
    }

    @Test
    void toHexString_byte_whenValidByte_thenReturnsHexRepresentation() {
        // The high nibble is rendered uppercase and the low nibble lowercase - surprising,
        // but existing, long-standing behavior; not something to silently change here.
        assertAll(
                () -> assertThat(TypeUtil.toHexString((byte) 0x00)).isEqualTo("00"),
                () -> assertThat(TypeUtil.toHexString((byte) 0x0F)).isEqualTo("0f"),
                () -> assertThat(TypeUtil.toHexString((byte) 0xAB)).isEqualTo("Ab"),
                () -> assertThat(TypeUtil.toHexString((byte) 0xFF)).isEqualTo("Ff")
        );
    }

    @Test
    void toHexString_byteArray_whenFullRange_thenReturnsHexRepresentation() {
        byte[] bytes = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        assertThat(TypeUtil.toHexString(bytes, 0, bytes.length)).isEqualTo("DeAdBeEf");
    }

    @Test
    void toHexString_byteArray_whenSlice_thenReturnsHexRepresentationOfSliceOnly() {
        byte[] bytes = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        assertThat(TypeUtil.toHexString(bytes, 1, 2)).isEqualTo("AdBe");
    }

    @Test
    void toHexString_byteArray_whenZeroLength_thenReturnsEmptyString() {
        byte[] bytes = {(byte) 0xDE, (byte) 0xAD};
        assertThat(TypeUtil.toHexString(bytes, 0, 0)).isEmpty();
    }

}
