/*
 * Copyright 2015 - Per Wendel
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import spark.examples.exception.NotFoundException;

import spark.util.SparkTestUtil;

import static spark.Spark.exception;
import static spark.Spark.get;

import static spark.Spark.staticFiles;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Test static files
 */
public class StaticFilesMemberTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticFilesMemberTest.class);

    private static final String FO_SHIZZY = "Fo shizzy";
    private static final String NOT_FOUND_BRO = "Not found bro";

    private static final String EXTERNAL_FILE_NAME_HTML = "externalFile.html";

    private static final String CONTENT_OF_EXTERNAL_FILE = "Content of external file";

    private static SparkTestUtil testUtil;

    private static File tmpExternalFile;

    @BeforeAll
    public static void beforeAll() throws IOException {
        testUtil = new SparkTestUtil(4567);

        tmpExternalFile = new File(System.getProperty("java.io.tmpdir"), EXTERNAL_FILE_NAME_HTML);

        FileWriter writer = new FileWriter(tmpExternalFile);
        writer.write(CONTENT_OF_EXTERNAL_FILE);
        writer.flush();
        writer.close();

        staticFiles.location("/public");
        staticFiles.externalLocation(System.getProperty("java.io.tmpdir"));

        get("/hello", (q, a) -> FO_SHIZZY);

        get("/*", (q, a) -> {
            throw new NotFoundException();
        });

        exception(NotFoundException.class, (e, request, response) -> {
            response.status(404);
            response.body(NOT_FOUND_BRO);
        });

        Spark.awaitInitialization();
    }

    @AfterAll
    public static void afterAll() {
        Spark.stop();
        if (tmpExternalFile != null) {
            LOGGER.debug("tearDown().deleting: " + tmpExternalFile);
            tmpExternalFile.delete();
        }
    }

    @Test
    public void testStaticFileCssStyleCss() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/css/style.css", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Content of css file")
        );

        testGet();
    }

    @Test
    public void testStaticFileMjs() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/js/module.mjs", null);

        String expectedContentType = response.headers.get("Content-Type");
        assertThat(expectedContentType).isEqualTo("application/javascript");

        String body = response.body;
        assertThat(body).isEqualTo("export default function () { console.log(\"Hello, I'm a .mjs file\"); }\n");
    }

    @Test
    public void testStaticFilePagesIndexHtml() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/pages/index.html", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("<html><body>Hello Static World!</body></html>")
        );

        testGet();
    }

    @Test
    public void testStaticFilePageHtml() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/page.html", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("<html><body>Hello Static Files World!</body></html>")
        );

        testGet();
    }

    @Test
    public void testExternalStaticFile() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/externalFile.html", null);
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).isEqualTo("Content of external file")
        );

        testGet();
    }

    @Test
    public void testStaticFileHeaders() throws Exception {
        staticFiles.headers(new HashMap() {
            {
                put("Server", "Microsoft Word");
                put("Cache-Control", "private, max-age=600");
            }
        });
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/pages/index.html", null);
        assertAll(
                () -> assertThat(response.headers.get("Server")).isEqualTo("Microsoft Word"),
                () -> assertThat(response.headers.get("Cache-Control")).isEqualTo("private, max-age=600")
        );

        testGet();
    }

    @Test
    public void testStaticFileExpireTime() throws Exception {
        staticFiles.expireTime(600);
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/pages/index.html", null);
        assertThat(response.headers.get("Cache-Control")).isEqualTo("private, max-age=600");

        testGet();
    }

    /**
     * Used to verify that "normal" functionality works after static files mapping
     */
    private static void testGet() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", "");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(FO_SHIZZY)
        );
    }

    @Test
    public void testExceptionMapping404() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/filethatdoesntexist.html", null);

        assertAll(
                () -> assertThat(response.status).isEqualTo(404),
                () -> assertThat(response.body).isEqualTo(NOT_FOUND_BRO)
        );
    }
}
