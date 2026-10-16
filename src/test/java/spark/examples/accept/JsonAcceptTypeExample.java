package spark.examples.accept;

import spark.Service;

public class JsonAcceptTypeExample {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        //Running curl -i -H "Accept: application/json" http://localhost:4567/hello json message is read.
        //Running curl -i -H "Accept: text/html" http://localhost:4567/hello HTTP 404 error is thrown.
        http.get("/hello", "application/json", (request, response) -> "{\"message\": \"Hello World\"}");
    }

}
