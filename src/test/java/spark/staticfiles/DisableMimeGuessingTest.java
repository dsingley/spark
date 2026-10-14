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
package spark.staticfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.examples.exception.NotFoundException;
import spark.testing.SparkServerExtension;
import spark.util.SparkTestUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Test static files
 */
class DisableMimeGuessingTest {

    private static final Logger LOG = LoggerFactory.getLogger(DisableMimeGuessingTest.class);

    private static final String FO_SHIZZY = "Fo shizzy";
    private static final String EXTERNAL_FILE_NAME_HTML = "externalFile.html";
    private static final String CONTENT_OF_EXTERNAL_FILE = "Content of external file";

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {

        http.staticFiles.location("/public");
        http.staticFiles.externalLocation(System.getProperty("java.io.tmpdir"));
        http.staticFiles.disableMimeTypeGuessing();

        http.get("/hello", (q, a) -> FO_SHIZZY);

        http.get("/*", (q, a) -> {
            throw new NotFoundException();
        });
    });

    private static SparkTestUtil testUtil;

    private static File tmpExternalFile;

    @BeforeAll
    static void beforeAll() throws IOException {
        testUtil = new SparkTestUtil(SPARK.port());

        tmpExternalFile = new File(System.getProperty("java.io.tmpdir"), EXTERNAL_FILE_NAME_HTML);

        var writer = new FileWriter(tmpExternalFile);
        writer.write(CONTENT_OF_EXTERNAL_FILE);
        writer.flush();
        writer.close();
    }

    @AfterAll
    static void afterAll() throws Exception {
        try {
            if (tmpExternalFile != null) {
                LOG.debug("tearDown().deleting: {}", tmpExternalFile);
                tmpExternalFile.delete();
            }
        } finally {
            // disabling MIME type guessing is JVM-wide and cannot be undone through Spark, so undo it here for
            // the test classes that run after this one in the same JVM
            var guessingOn = MimeType.class.getDeclaredField("guessingOn");
            guessingOn.setAccessible(true);
            guessingOn.setBoolean(null, true);
        }
    }

    @Test
    void testMimeTypes() {
        assertAll(
                () -> assertThat(doGet("/pages/index.html").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/js/scripts.js").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/css/style.css").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/img/sparklogo.png").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/img/sparklogo.svg").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/img/sparklogoPng").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/img/sparklogoSvg").headers.get("Content-Type")).isNull(),
                () -> assertThat(doGet("/externalFile.html").headers.get("Content-Type")).isNull()
        );
    }

    @Test
    void testCustomMimeType() throws Exception {
        SPARK.service().staticFiles.registerMimeType("cxt", "custom-extension-type");
        assertThat(doGet("/img/file.cxt").headers.get("Content-Type")).isNull();
    }

    private SparkTestUtil.UrlResponse doGet(String fileName) throws Exception {
        return testUtil.doMethod("GET", fileName, null);
    }

}
