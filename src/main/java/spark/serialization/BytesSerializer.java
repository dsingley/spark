/*
 * Copyright 2015 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Serializer for byte[] and ByteBuffer elements, writing them directly to the output
 * stream. Any other element type is rejected with IllegalArgumentException.
 *
 * @implNote For a ByteBuffer, this writes its full backing array via {@link ByteBuffer#array()}.
 * This only works for array-backed buffers - a direct or read-only ByteBuffer throws
 * UnsupportedOperationException - and writes the whole backing array rather than just
 * the buffer's current position/limit window. This has been the behavior since this
 * class was introduced in 2015 and is documented here rather than changed.
 *
 * @author alex
 */
class BytesSerializer extends Serializer {

    /**
     * @param element the element to check.
     * @return true if element is a byte[] or ByteBuffer.
     */
    @Override
    public boolean canProcess(Object element) {
        return element instanceof byte[] || element instanceof ByteBuffer;
    }

    /**
     * Writes a byte[] or ByteBuffer element directly to the output stream.
     *
     * @param outputStream the output stream.
     * @param element      the element to write; must be a byte[] or ByteBuffer.
     * @throws IOException in the case of IO error.
     * @throws IllegalArgumentException if element is neither a byte[] nor a ByteBuffer.
     */
    @Override
    public void process(OutputStream outputStream, Object element)
            throws IOException {
        byte[] bytes;
        if (element instanceof byte[] byteArray) {
            bytes = byteArray;
        } else if (element instanceof ByteBuffer byteBuffer) {
            bytes = byteBuffer.array();
        } else {
            var typeName = element == null ? "null element" : element.getClass().getName();
            throw new IllegalArgumentException(
                    "BytesSerializer cannot process a %s; expected byte[] or ByteBuffer"
                            .formatted(typeName));
        }
        outputStream.write(bytes);
    }

}
