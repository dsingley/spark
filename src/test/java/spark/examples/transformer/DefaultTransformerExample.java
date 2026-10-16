package spark.examples.transformer;

import spark.ResponseTransformer;
import spark.Service;

public class DefaultTransformerExample {

    private static final ResponseTransformer json = new JsonTransformer();

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        http.defaultResponseTransformer(json);

        http.get("/hello", "application/json", (request, response) -> new MyMessage("Hello World"));

        http.get("/hello2", "application/json", (request, response) -> new MyMessage("Hello World"),
            model -> "custom transformer");
    }

}
