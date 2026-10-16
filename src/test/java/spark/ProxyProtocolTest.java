package spark;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.examples.proxy.ProxyProtocolExample;
import spark.testing.SparkServerExtension;

/**
 * Every connector accepts the proxy protocol (v1 and v2), so that behind a TCP load balancer
 * {@code request.ip()} is the real client's address.
 */
class ProxyProtocolTest {

    private static final byte[] V2_SIGNATURE = {
            0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A
    };

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.get("/ip", (request, response) -> request.ip());
    });

    @Test
    void v1PreambleSetsTheClientAddress() throws Exception {
        var preamble = "PROXY TCP4 198.51.100.9 10.0.0.1 40000 80\r\n".getBytes(StandardCharsets.US_ASCII);
        assertThat(ProxyProtocolExample.requestIp(SPARK.port(), preamble)).isEqualTo("198.51.100.9");
    }

    @Test
    void v2PreambleSetsTheClientAddress() throws Exception {
        assertThat(ProxyProtocolExample.requestIp(SPARK.port(), v2Preamble(new byte[]{(byte) 203, 0, 113, 7})))
                .isEqualTo("203.0.113.7");
    }

    @Test
    void connectionWithoutPreambleUsesTheConnectionAddress() throws Exception {
        assertThat(ProxyProtocolExample.requestIp(SPARK.port(), new byte[0])).isEqualTo("127.0.0.1");
    }

    /**
     * PROXY protocol v2 header for a TCP-over-IPv4 connection from {@code sourceAddress}, port 40000,
     * to 10.0.0.1, port 80.
     */
    private static byte[] v2Preamble(byte[] sourceAddress) {
        var out = new ByteArrayOutputStream();
        out.writeBytes(V2_SIGNATURE);
        out.write(0x21);             // version 2, PROXY command
        out.write(0x11);             // TCP over IPv4
        out.write(0x00);             // length of the address block: 12 bytes
        out.write(0x0C);
        out.writeBytes(sourceAddress);
        out.writeBytes(new byte[]{10, 0, 0, 1});
        out.write(0x9C);             // source port 40000
        out.write(0x40);
        out.write(0x00);             // destination port 80
        out.write(0x50);
        return out.toByteArray();
    }
}
