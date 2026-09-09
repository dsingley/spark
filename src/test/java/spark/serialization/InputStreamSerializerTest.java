package spark.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class InputStreamSerializerTest {

    private InputStreamSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new InputStreamSerializer();
    }

    @Test
    void testProcess_copiesData() throws IOException {
        byte[] bytes = "Hello, Spark!".getBytes();
        var input = new ByteArrayInputStream(bytes);
        var output = new ByteArrayOutputStream();

        serializer.process(output, input);

        assertThat(output.toByteArray()).containsExactly(bytes);
    }

    @Test
    void testProcess_closesStream() throws IOException {
        var input = new MockInputStream(new ByteArrayInputStream(new byte[0]));
        var output = new ByteArrayOutputStream();

        serializer.process(output, input);

        assertThat(input.closed).isTrue();
    }

    private class MockInputStream extends FilterInputStream {

        boolean closed = false;

        private MockInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }
}
