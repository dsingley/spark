/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.examples.simple;

import spark.Service;

/**
 * A simple example just showing some basic functionality
 */
public class SimpleExample {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        http.get("/hello", (request, response) -> "Hello World!");

        http.post("/hello", (request, response) -> "Hello World: " + request.body());

        http.get("/private", (request, response) -> {
            response.status(401);
            return "Go Away!!!";
        });

        http.get("/users/:name", (request, response) -> "Selected user: " + request.params(":name"));

        http.get("/news/:section", (request, response) -> {
            response.type("text/xml");
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>" + request.params("section") + "</news>";
        });

        http.get("/protected", (request, response) -> {
            http.halt(403, "I don't think so!!!");
            return null;
        });

        http.get("/redirect", (request, response) -> {
            response.redirect("/news/world");
            return null;
        });

        http.get("/", (request, response) -> "root");
    }

}
