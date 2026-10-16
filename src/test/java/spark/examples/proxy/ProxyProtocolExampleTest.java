package spark.examples.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;

class ProxyProtocolExampleTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(ProxyProtocolExample::run);

    @Test
    void reportsTheClientAddressFromTheProxyPreamble() throws Exception {
        var preamble = "PROXY TCP4 203.0.113.7 10.0.0.1 51234 80\r\n".getBytes(StandardCharsets.US_ASCII);
        assertThat(ProxyProtocolExample.requestIp(SPARK.port(), preamble)).isEqualTo("203.0.113.7");
    }

    @Test
    void reportsTheConnectionAddressWithoutAPreamble() throws Exception {
        assertThat(ProxyProtocolExample.requestIp(SPARK.port(), new byte[0])).isEqualTo("127.0.0.1");
    }
}
