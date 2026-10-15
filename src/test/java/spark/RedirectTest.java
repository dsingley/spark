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
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

/**
 * Tests the redirect utility methods in {@link spark.Redirect}
 */
class RedirectTest {

    private static final String REDIRECTED = "Redirected";

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.get("/hello", (request, response) -> REDIRECTED);

        http.redirect.get("/hi", "/hello");
        http.redirect.post("/hi", "/hello");
        http.redirect.put("/hi", "/hello");
        http.redirect.delete("/hi", "/hello");
        http.redirect.any("/any", "/hello");

        http.redirect.get("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        http.redirect.post("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        http.redirect.put("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        http.redirect.delete("/hiagain", "/hello", Redirect.Status.USE_PROXY);
        http.redirect.any("/anyagain", "/hello", Redirect.Status.USE_PROXY);
    });

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
        testUtil.setFollowRedirectStrategy(301, 302); // don't set the others to be able to verify affect of Redirect.Status
    }

    @Test
    void testRedirectGet() throws Exception {
        var response = testUtil.doMethod("GET", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectPost() throws Exception {
        var response = testUtil.doMethod("POST", "/hi", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectPut() throws Exception {
        var response = testUtil.doMethod("PUT", "/hi", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectDelete() throws Exception {
        var response = testUtil.doMethod("DELETE", "/hi", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectAnyGet() throws Exception {
        var response = testUtil.doMethod("GET", "/any", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectAnyPut() throws Exception {
        var response = testUtil.doMethod("PUT", "/any", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectAnyPost() throws Exception {
        var response = testUtil.doMethod("POST", "/any", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectAnyDelete() throws Exception {
        var response = testUtil.doMethod("DELETE", "/any", "");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo(REDIRECTED)
        );
    }

    @Test
    void testRedirectGetWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("GET", "/hiagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectPostWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("POST", "/hiagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectPutWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("PUT", "/hiagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectDeleteWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("DELETE", "/hiagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectAnyGetWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("GET", "/anyagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectAnyPostWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("POST", "/anyagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectAnyPutWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("PUT", "/anyagain", "");
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

    @Test
    void testRedirectAnyDeleteWithSpecificCode() throws Exception {
        var response = testUtil.doMethod("DELETE", "/anyagain", null);
        assertThat(response.status).isEqualTo(Redirect.Status.USE_PROXY.intValue());
    }

}
