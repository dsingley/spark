package spark.examples.accept;

import spark.Service;

/**
 * Serves a route that only matches requests with an {@code Accept} header of {@code application/json}.
 * With the server on its default port, 4567 (use {@code -Dport=...} to change it):
 * <ul>
 * <li>{@code curl -i -H "Accept: application/json" http://localhost:4567/hello} returns the JSON message.</li>
 * <li>{@code curl -i -H "Accept: text/html" http://localhost:4567/hello} returns an HTTP 404.</li>
 * </ul>
 */
public class JsonAcceptTypeExample {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        http.get("/hello", "application/json", (request, response) -> "{\"message\": \"Hello World\"}");
    }

}
