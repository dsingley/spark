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
import spark.util.ServiceStopExtension;
import spark.util.SparkTestUtil;

/**
 * Created by Per Wendel on 2016-02-18.
 */
class MultipleServicesTest {

    private static Service first;
    private static Service second;

    @RegisterExtension
    static ServiceStopExtension stopExtension = new ServiceStopExtension(() -> first, () -> second);

    private static SparkTestUtil firstClient;
    private static SparkTestUtil secondClient;

    @BeforeAll
    static void beforeAll() {
        firstClient = new SparkTestUtil(4567);
        secondClient = new SparkTestUtil(1234);

        first = igniteFirstService();
        second = igniteSecondService();

        first.awaitInitialization();
        second.awaitInitialization();
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
        for (var routeMatch : first.routes()) {
            assertAll(
                    () -> assertThat(routeMatch.getAcceptType()).isEqualTo("*/*"),
                    () -> assertThat(routeMatch.getHttpMethod()).isEqualTo(HttpMethod.get),
                    () -> assertThat(routeMatch.getMatchUri()).isEqualTo("/hello"),
                    () -> assertThat(routeMatch.getRequestURI()).isEqualTo("ALL_ROUTES"),
                    () -> assertThat(routeMatch.getTarget()).isInstanceOf(RouteImpl.class)
            );
        }

        for (var routeMatch : second.routes()) {
            assertAll(
                    () -> assertThat(routeMatch.getAcceptType()).isEqualTo("*/*"),
                    () -> assertThat(routeMatch.getHttpMethod()).isInstanceOf(HttpMethod.class),
                    () -> assertThat(routeMatch.getMatchUri()).isSubstringOf("/hello/hi/uniqueforsecond"),
                    () -> assertThat(routeMatch.getRequestURI()).isEqualTo("ALL_ROUTES"),
                    () -> assertThat(routeMatch.getTarget()).isInstanceOf(RouteImpl.class)
            );
        }
    }

    private static Service igniteFirstService() {

        var service = Service.ignite();

        service.get("/hello", (q, a) -> "Hello World!");

        return service;
    }

    private static Service igniteSecondService() {

        var service = Service.ignite()
                .port(1234)
                .staticFileLocation("/public")
                .threadPool(40);

        service.get("/hello", (q, a) -> "Hello World!");
        service.get("/uniqueforsecond", (q, a) -> "Bompton");

        service.redirect.any("/hi", "/hello");

        return service;
    }

}
