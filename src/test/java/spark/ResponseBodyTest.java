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
import static spark.Spark.after;
import static spark.Spark.get;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.util.SparkTestUtil;

/**
 * Validates and shows the "rules" for how response "body" is set.
 */
public class ResponseBodyTest {

    public static final String HELLO = "/hello";
    public static final String SPECIAL = "/special";
    public static final String PORAKATIKAOKAO = "/porakatikaokao";
    public static final String MAXIME = "/maxime";

    public static final String HELLO_WORLD = "Hello World!";
    public static final String XIDXUS = "xidxus";
    public static final String DOLLAR_11AB = "$11ab";
    public static final String GALLUS_SCANDALUM = "gallus scandalum";

    private static SparkTestUtil http;

    @BeforeAll
    static void beforeAll() {
        http = new SparkTestUtil(4567);

        get(HELLO, (q, a) -> HELLO_WORLD);

        after(HELLO, (q, a) -> {
            String body = a.body();
            assertThat(body).isEqualTo(HELLO_WORLD);
        });

        get(SPECIAL, (q, a) -> {
            a.body(XIDXUS);
            return "";
        });

        after(SPECIAL, (q, a) -> {
            String body = a.body();
            assertThat(body).isEqualTo(XIDXUS);
        });

        get(PORAKATIKAOKAO, (q, a) -> {
            a.body(GALLUS_SCANDALUM);
            return null;
        });

        after(PORAKATIKAOKAO, (q, a) -> {
            String body = a.body();
            assertThat(body).isEqualTo(GALLUS_SCANDALUM);
        });

        get(MAXIME, (q, a) -> {
            a.body(XIDXUS);
            return DOLLAR_11AB;
        });

        after(MAXIME, (q, a) -> {
            String body = a.body();
            assertThat(body).isEqualTo(DOLLAR_11AB);
        });

        Spark.awaitInitialization();
    }

    @AfterAll
    static void afterAll() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    void testHELLO() {
        try {
            SparkTestUtil.UrlResponse response = http.get(HELLO);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(HELLO_WORLD);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSPECIAL() {
        try {
            SparkTestUtil.UrlResponse response = http.get(SPECIAL);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(XIDXUS);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPORAKATIKAOKAO() {
        try {
            SparkTestUtil.UrlResponse response = http.get(PORAKATIKAOKAO);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(GALLUS_SCANDALUM);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testMAXIME() {
        try {
            SparkTestUtil.UrlResponse response = http.get(MAXIME);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(DOLLAR_11AB);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
