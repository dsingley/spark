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

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import spark.util.SparkTestUtil;

import static spark.Spark.get;

import static spark.Spark.redirect;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Tests the redirect utility methods in {@link spark.Redirect}
 */
public class RedirectTest {

    private static final String REDIRECTED = "Redirected";

    private static SparkTestUtil testUtil;

    @BeforeAll
    public static void beforeAll() throws IOException {
        testUtil = new SparkTestUtil(4567);
        testUtil.setFollowRedirectStrategy(301, 302); // don't set the others to be able to verify affect of Redirect.Status

        get("/hello", (request, response) -> REDIRECTED);

        redirect.get("/hi", "/hello");
        redirect.post("/hi", "/hello");
        redirect.put("/hi", "/hello");
        redirect.delete("/hi", "/hello");
        redirect.any("/any", "/hello");

        redirect.get("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.post("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.put("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.delete("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        redirect.any("/anyagain", "/hello", Redirect.Status.USE_PROXY);

        Spark.awaitInitialization();
    }

    @Test
    public void testRedirectGet() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectPost() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("POST", "/hi", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectPut() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/hi", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectDelete() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectAnyGet() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/any", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectAnyPut() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/any", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectAnyPost() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("POST", "/any", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectAnyDelete() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/any", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    public void testRedirectGetWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hiagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectPostWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("POST", "/hiagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectPutWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/hiagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectDeleteWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/hiagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectAnyGetWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/anyagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectAnyPostWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("POST", "/anyagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectAnyPutWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("PUT", "/anyagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    public void testRedirectAnyDeleteWithSpecificCode() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("DELETE", "/anyagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

}
