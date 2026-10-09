package spark.embeddedserver.jetty.websocket;

import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@WebSocket
public class WebSocketTestClient {
    private final CountDownLatch closeLatch;
    private volatile int localPort;

    public WebSocketTestClient() {
        closeLatch = new CountDownLatch(1);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        closeLatch.countDown();
    }

    /**
     * @return the port this client connected from, which is how the server tells its connection apart
     */
    public int getLocalPort() {
        return localPort;
    }

    @OnWebSocketOpen
    public void onOpen(Session session) {
        localPort = ((InetSocketAddress) session.getLocalSocketAddress()).getPort();
        session.sendText("Hi Spark!", Callback.from(
                () -> session.close(StatusCode.NORMAL, "Bye!", Callback.NOOP),
                Throwable::printStackTrace));
    }
}
