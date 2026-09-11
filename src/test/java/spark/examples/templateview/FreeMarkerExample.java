package spark.examples.templateview;

import static spark.Spark.get;
import static spark.Spark.modelAndView;

import java.util.Map;

public class FreeMarkerExample {

    public static void main(String[] args) {

        get("/hello", (request, response) -> {
            var attributes = Map.of("message", "Hello FreeMarker World");

            // The hello.ftl file is located in directory:
            // src/test/resources/spark/examples/templateview/freemarker
            return modelAndView(attributes, "hello.ftl");
        }, new FreeMarkerTemplateEngine());

    }

}
