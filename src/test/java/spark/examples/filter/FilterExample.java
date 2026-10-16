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
package spark.examples.filter;

import java.util.HashMap;
import spark.Service;

/**
 * Example showing a very simple (and stupid) authentication filter that is
 * executed before all other resources.
 * <p>
 * When requesting the resource with e.g.,
 * <a href="http://localhost:4567/hello?user=some&password=guy">http://localhost:4567/hello?user=some&password=guy</a> the filter will stop the
 * execution, and the client will get a 401 UNAUTHORIZED with the content 'You
 * are not welcome here'.
 * <p>
 * When requesting the resource with e.g.,
 * <a href="http://localhost:4567/hello?user=foo&password=bar">http://localhost:4567/hello?user=foo&password=bar</a> the filter will accept the
 * request and the request will continue to the /hello route.
 * Note: There is also an "after filter" that adds a header to the response
 */
public class FilterExample {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));
        run(http);
    }

    static void run(Service http) {
        var usernamePasswords = new HashMap<String, String>();
        usernamePasswords.put("foo", "bar");
        usernamePasswords.put("admin", "admin");

        http.before((request, response) -> {
            var user = request.queryParams("user");
            var password = request.queryParams("password");

            var dbPassword = usernamePasswords.get(user);
            if (!(password != null && password.equals(dbPassword))) {
                http.halt(401, "You are not welcome here!!!");
            }
        });

        http.before("/hello", (request, response) -> response.header("Foo", "Set by second before filter"));

        http.get("/hello", (request, response) -> "Hello World!");

        http.after("/hello", (request, response) -> response.header("spark", "added by after-filter"));

    }
}
