package spark.examples.templateview;

import java.util.Map;
import spark.ModelAndView;
import spark.Service;

public class FreeMarkerExample {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        http.get("/hello", (request, response) -> {
            var attributes = Map.of("message", "Hello FreeMarker World");

            // The hello.ftl file is located in directory:
            // src/test/resources/spark/examples/templateview/freemarker
            return new ModelAndView(attributes, "hello.ftl");
        }, new FreeMarkerTemplateEngine());
    }

}
