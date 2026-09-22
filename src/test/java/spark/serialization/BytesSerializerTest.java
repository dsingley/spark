package spark.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

class BytesSerializerTest {

    private BytesSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new BytesSerializer();
    }

    @Test
    void canProcess_whenByteArray_thenTrue() {
        assertThat(serializer.canProcess(new byte[0])).isTrue();
    }

    @Test
    void canProcess_whenByteBuffer_thenTrue() {
        assertThat(serializer.canProcess(ByteBuffer.allocate(0))).isTrue();
    }

    @Test
    void canProcess_whenOtherType_thenFalse() {
        assertThat(serializer.canProcess("not bytes")).isFalse();
    }

    @Test
    void process_whenByteArray_writesItDirectly() throws IOException {
        var bytes = "Hello, Spark!".getBytes();
        var output = new ByteArrayOutputStream();

        serializer.process(output, bytes);

        assertThat(output.toByteArray()).containsExactly(bytes);
    }

    @Test
    void process_whenByteBuffer_writesItsBackingArray() throws IOException {
        var bytes = "Hello, Spark!".getBytes();
        var output = new ByteArrayOutputStream();

        serializer.process(output, ByteBuffer.wrap(bytes));

        assertThat(output.toByteArray()).containsExactly(bytes);
    }

    @Test
    void process_whenUnsupportedType_thenThrowsIllegalArgumentException() {
        var output = new ByteArrayOutputStream();

        assertThatThrownBy(() -> serializer.process(output, "not bytes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BytesSerializer cannot process a java.lang.String; expected byte[] or ByteBuffer");
    }

    @Test
    void process_whenNull_thenThrowsIllegalArgumentException() {
        var output = new ByteArrayOutputStream();

        assertThatThrownBy(() -> serializer.process(output, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("BytesSerializer cannot process a null element; expected byte[] or ByteBuffer");
    }

}
