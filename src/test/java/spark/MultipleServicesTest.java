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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import spark.route.HttpMethod;
import spark.routematch.RouteMatch;
import spark.util.SparkTestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static spark.Service.ignite;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Created by Per Wendel on 2016-02-18.
 */
public class MultipleServicesTest {

    private static Service first;
    private static Service second;

    private static SparkTestUtil firstClient;
    private static SparkTestUtil secondClient;

    @BeforeAll
    public static void beforeAll() throws Exception {
        firstClient = new SparkTestUtil(4567);
        secondClient = new SparkTestUtil(1234);

        first = igniteFirstService();
        second = igniteSecondService();

        first.awaitInitialization();
        second.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        first.stop();
        second.stop();
    }

    @Test
    public void testGetHello() throws Exception {
        SparkTestUtil.UrlResponse response = firstClient.doMethod("GET", "/hello", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    public void testGetRedirectedHi() throws Exception {
        SparkTestUtil.UrlResponse response = secondClient.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Hello World!")
        );
    }

    @Test
    public void testGetUniqueForSecondWithFirst() throws Exception {
        SparkTestUtil.UrlResponse response = firstClient.doMethod("GET", "/uniqueforsecond", null);
        assertThat(response.status).isEqualTo(404);
    }

    @Test
    public void testGetUniqueForSecondWithSecond() throws Exception {
        SparkTestUtil.UrlResponse response = secondClient.doMethod("GET", "/uniqueforsecond", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Bompton")
        );
    }

    @Test
    public void testStaticFileCssStyleCssWithFirst() throws Exception {
        SparkTestUtil.UrlResponse response = firstClient.doMethod("GET", "/css/style.css", null);
        assertThat(response.status).isEqualTo(404);
    }

    @Test
    public void testStaticFileCssStyleCssWithSecond() throws Exception {
        SparkTestUtil.UrlResponse response = secondClient.doMethod("GET", "/css/style.css", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Content of css file")
        );
    }

    @Test
    public void testGetAllRoutesFromBothServices(){
        for(RouteMatch routeMatch : first.routes()){
            assertThat(routeMatch.getAcceptType()).isEqualTo("*/*");
            assertThat(routeMatch.getHttpMethod()).isEqualTo(HttpMethod.get);
            assertThat(routeMatch.getMatchUri()).isEqualTo("/hello");
            assertThat(routeMatch.getRequestURI()).isEqualTo("ALL_ROUTES");
            assertThat(routeMatch.getTarget()).isInstanceOf(RouteImpl.class);
        }

        for(RouteMatch routeMatch : second.routes()){
            assertThat(routeMatch.getAcceptType()).isEqualTo("*/*");
            assertThat(routeMatch.getHttpMethod()).isInstanceOf(HttpMethod.class);
            boolean isUriOnList = ("/hello/hi/uniqueforsecond").contains(routeMatch.getMatchUri());
            assertThat(isUriOnList).isTrue();
            assertThat(routeMatch.getRequestURI()).isEqualTo("ALL_ROUTES");
            assertThat(routeMatch.getTarget()).isInstanceOf(RouteImpl.class);
        }
    }

    private static Service igniteFirstService() {

        Service http = ignite(); // I give the variable the name 'http' for the code to make sense when adding routes.

        http.get("/hello", (q, a) -> "Hello World!");

        return http;
    }

    private static Service igniteSecondService() {

        Service http = ignite()
                .port(1234)
                .staticFileLocation("/public")
                .threadPool(40);

        http.get("/hello", (q, a) -> "Hello World!");
        http.get("/uniqueforsecond", (q, a) -> "Bompton");

        http.redirect.any("/hi", "/hello");

        return http;
    }


}
