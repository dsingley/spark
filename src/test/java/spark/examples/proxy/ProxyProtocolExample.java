package spark.examples.proxy;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import spark.Service;

/**
 * Example showing that a server accepts the proxy protocol, which a TCP load balancer such as an AWS
 * network load balancer uses to pass the real client address to the application.
 * <p>
 * The server has one route, {@code /ip}, that returns {@code request.ip()}. {@code main} starts the
 * server and then connects to it twice, playing the part of a load balancer: once with a PROXY
 * preamble claiming the client is 203.0.113.7, and once with no preamble. It prints what the route
 * returned each time, then stops the server. The default port is 4567; use {@code -Dport=...} to
 * change it.
 * <p>
 * To try it by hand with curl (8.2 or later), run only the server by passing {@code serve} as an
 * argument, then compare:
 * <pre>
 * curl http://localhost:4567/ip
 * curl --haproxy-protocol --haproxy-clientip 203.0.113.7 http://localhost:4567/ip
 * </pre>
 * The first returns the address curl connected from; the second returns 203.0.113.7.
 */
public class ProxyProtocolExample {

    private static final String PROXY_V1_PREAMBLE = "PROXY TCP4 203.0.113.7 10.0.0.1 51234 80\r\n";

    public static void main(String[] args) throws IOException {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
        http.awaitInitialization();

        if (args.length > 0 && args[0].equals("serve")) {
            return;
        }

        System.out.println("with a PROXY preamble:    " + requestIp(http.port(), PROXY_V1_PREAMBLE.getBytes(StandardCharsets.US_ASCII)));
        System.out.println("without a PROXY preamble: " + requestIp(http.port(), new byte[0]));
        http.stop();
    }

    static void run(Service http) {
        http.get("/ip", (request, response) -> request.ip());
    }

    /**
     * Sends {@code preamble} followed by a plain HTTP request for {@code /ip}, the way a load balancer
     * would, and returns the response body.
     */
    public static String requestIp(int port, byte[] preamble) throws IOException {
        try (var socket = new Socket("127.0.0.1", port)) {
            var out = socket.getOutputStream();
            out.write(preamble);
            out.write("GET /ip HTTP/1.0\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();

            var response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
            return response.substring(response.indexOf("\r\n\r\n") + 4);
        }
    }
}
