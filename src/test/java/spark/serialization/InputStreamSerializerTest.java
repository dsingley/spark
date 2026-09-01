package spark.serialization;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class InputStreamSerializerTest {

    private InputStreamSerializer serializer = new InputStreamSerializer();

    @Test
    public void testProcess_copiesData() throws IOException {
        byte[] bytes = "Hello, Spark!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        serializer.process(output, input);

        assertThat(output.toByteArray()).containsExactly(bytes);
    }

    @Test
    public void testProcess_closesStream() throws IOException {
        MockInputStream input = new MockInputStream(new ByteArrayInputStream(new byte[0]));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

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
