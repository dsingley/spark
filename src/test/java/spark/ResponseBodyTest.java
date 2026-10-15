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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;
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

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.get(HELLO, (q, a) -> HELLO_WORLD);

        http.after(HELLO, (q, a) -> {
            var body = a.body();
            assertThat(body).isEqualTo(HELLO_WORLD);
        });

        http.get(SPECIAL, (q, a) -> {
            a.body(XIDXUS);
            return "";
        });

        http.after(SPECIAL, (q, a) -> {
            var body = a.body();
            assertThat(body).isEqualTo(XIDXUS);
        });

        http.get(PORAKATIKAOKAO, (q, a) -> {
            a.body(GALLUS_SCANDALUM);
            return null;
        });

        http.after(PORAKATIKAOKAO, (q, a) -> {
            var body = a.body();
            assertThat(body).isEqualTo(GALLUS_SCANDALUM);
        });

        http.get(MAXIME, (q, a) -> {
            a.body(XIDXUS);
            return DOLLAR_11AB;
        });

        http.after(MAXIME, (q, a) -> {
            var body = a.body();
            assertThat(body).isEqualTo(DOLLAR_11AB);
        });
    });

    private static SparkTestUtil testUtil;

    @BeforeAll
    static void beforeAll() {
        testUtil = new SparkTestUtil(SPARK.port());
    }

    @Test
    void testHELLO() {
        try {
            var response = testUtil.get(HELLO);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(HELLO_WORLD);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSPECIAL() {
        try {
            var response = testUtil.get(SPECIAL);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(XIDXUS);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPORAKATIKAOKAO() {
        try {
            var response = testUtil.get(PORAKATIKAOKAO);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(GALLUS_SCANDALUM);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testMAXIME() {
        try {
            var response = testUtil.get(MAXIME);
            assertThat(response.status).isEqualTo(200);
            assertThat(response.body).isEqualTo(DOLLAR_11AB);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
