package spark.servlet;

import java.io.File;
import java.io.FileWriter;
import spark.Service;

public class MyApp implements SparkApplication {

    public static final String EXTERNAL_FILE = "externalFileServlet.html";

    static File tmpExternalFile;

    @Override
    public synchronized void init() {
        // an instance rather than the static Spark methods, so no singleton outlives this test in a shared JVM
        var http = Service.ignite();

        try {
            http.externalStaticFileLocation(System.getProperty("java.io.tmpdir"));
            http.staticFileLocation("/public");

            tmpExternalFile = new File(System.getProperty("java.io.tmpdir"), EXTERNAL_FILE);
            var writer = new FileWriter(tmpExternalFile);
            writer.write("Content of external file");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        http.before("/protected/*", (request, response) -> http.halt(401, "Go Away!"));

        http.get("/hi", (request, response) -> "Hello World!");

        http.get("/:param", (request, response) -> "echo: " + request.params(":param"));

        http.get("/", (request, response) -> "Hello Root!");

        http.post("/poster", (request, response) -> {
            var body = request.body();
            response.status(201); // created
            return "Body was: " + body;
        });

        http.after("/hi", (request, response) -> response.header("after", "foobar"));

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            // ignored
        }
    }

}
