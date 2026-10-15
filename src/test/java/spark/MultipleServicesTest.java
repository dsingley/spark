/*
 * Copyright 2016 - Per Wendel
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
package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.route.HttpMethod;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

class MultipleServicesTest {

    @RegisterExtension
    static final SparkServerExtension FIRST = new SparkServerExtension(http -> {
        http.get("/hello", (q, a) -> "Hello World!");
    });

    @RegisterExtension
    static final SparkServerExtension SECOND = new SparkServerExtension(http -> {
        http.staticFileLocation("/public");
        http.threadPool(40);

        http.get("/hello", (q, a) -> "Hello World!");
        http.get("/uniqueforsecond", (q, a) -> "Bompton");

        http.redirect.any("/hi", "/hello");
    });

    private static SparkTestUtil firstClient;
    private static SparkTestUtil secondClient;

    @BeforeAll
    static void beforeAll() {
        firstClient = new SparkTestUtil(FIRST.port());
        secondClient = new SparkTestUtil(SECOND.port());
    }

    @Test
    void testGetHello() throws Exception {
        var response = firstClient.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void testGetRedirectedHi() throws Exception {
        var response = secondClient.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    void testGetUniqueForSecondWithFirst() throws Exception {
        var response = firstClient.doMethod("GET", "/uniqueforsecond", null);
        assertThat(response.status).isEqualTo(404);
    }

    @Test
    void testGetUniqueForSecondWithSecond() throws Exception {
        var response = secondClient.doMethod("GET", "/uniqueforsecond", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Bompton")
        );
    }

    @Test
    void testStaticFileCssStyleCssWithFirst() throws Exception {
        var response = firstClient.doMethod("GET", "/css/style.css", null);
        assertThat(response.status).isEqualTo(404);
    }

    @Test
    void testStaticFileCssStyleCssWithSecond() throws Exception {
        var response = secondClient.doMethod("GET", "/css/style.css", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Content of css file")
        );
    }

    @Test
    void testGetAllRoutesFromBothServices(){
        for (var routeMatch : FIRST.service().routes()) {
            assertAll(
                    () -> assertThat(routeMatch.getAcceptType()).isEqualTo("*/*"),
                    () -> assertThat(routeMatch.getHttpMethod()).isEqualTo(HttpMethod.get),
                    () -> assertThat(routeMatch.getMatchUri()).isEqualTo("/hello"),
                    () -> assertThat(routeMatch.getRequestURI()).isEqualTo("ALL_ROUTES"),
                    () -> assertThat(routeMatch.getTarget()).isInstanceOf(RouteImpl.class)
            );
        }

        for (var routeMatch : SECOND.service().routes()) {
            assertAll(
                    () -> assertThat(routeMatch.getAcceptType()).isEqualTo("*/*"),
                    () -> assertThat(routeMatch.getHttpMethod()).isInstanceOf(HttpMethod.class),
                    () -> assertThat(routeMatch.getMatchUri()).isSubstringOf("/hello/hi/uniqueforsecond"),
                    () -> assertThat(routeMatch.getRequestURI()).isEqualTo("ALL_ROUTES"),
                    () -> assertThat(routeMatch.getTarget()).isInstanceOf(RouteImpl.class)
            );
        }
    }
}
