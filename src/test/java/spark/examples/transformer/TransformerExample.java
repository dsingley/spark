package spark.examples.transformer;

import spark.Service;

public class TransformerExample {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        http.get("/hello", "application/json", (request, response) -> new MyMessage("Hello World"), new JsonTransformer());
    }

}
