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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.net.URLEncoder;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import spark.Spark;
import spark.examples.exception.NotFoundException;

import spark.util.SparkTestUtil;

import static spark.Spark.exception;
import static spark.Spark.get;

import static spark.Spark.staticFiles;

/**
 * Test external static files
 */
public class StaticFilesTestExternal {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticFilesTestExternal.class);

    private static final String FO_SHIZZY = "Fo shizzy";
    private static final String NOT_FOUND_BRO = "Not found bro";

    private static final String EXTERNAL_FILE_NAME_HTML = "externalFile.html";

    private static final String CONTENT_OF_EXTERNAL_FILE = "Content of external file";

    private static SparkTestUtil testUtil;

    private static File directoryRoot;
    private static File tmpExternalFile1;
    private static File tmpExternalFile2;
    private static File folderOutsideStaticFiles;

    @BeforeAll
    public static void beforeAll() throws IOException {
        testUtil = new SparkTestUtil(4567);

        directoryRoot = new File(System.getProperty("java.io.tmpdir"), "sparkish");
        directoryRoot.mkdirs();

        tmpExternalFile1 = new File(directoryRoot, EXTERNAL_FILE_NAME_HTML);

        FileWriter writer = new FileWriter(tmpExternalFile1);
        writer.write(CONTENT_OF_EXTERNAL_FILE);
        writer.flush();
        writer.close();

        folderOutsideStaticFiles = new File(directoryRoot.getAbsolutePath() + "/../dumpsterstuff");
        folderOutsideStaticFiles.mkdirs();

        String newFilePath = directoryRoot.getAbsolutePath() + "/../dumpsterstuff/Spark.class";
        tmpExternalFile2 = new File(newFilePath);
        tmpExternalFile2.createNewFile();

        staticFiles.externalLocation(directoryRoot.getPath());

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
        if (tmpExternalFile1 != null) {
            LOGGER.debug("tearDown(). Deleting tmp files");
            tmpExternalFile1.delete();
            tmpExternalFile2.delete();
            folderOutsideStaticFiles.delete();
            directoryRoot.delete();
        }
    }

    @Test
    public void testExternalStaticFile() throws Exception {
        SparkTestUtil.UrlResponse response = doGet("/externalFile.html");
        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                // Jetty 12 echoes MimeTypes' assumed charset for text/html into the Content-Type
                // header when none is set explicitly; Jetty 9 did not. Cosmetic, not a functional change.
                () -> assertThat(response.headers.get("Content-Type")).isEqualTo("text/html;charset=utf-8"),
                () -> assertThat(response.body).isEqualTo(CONTENT_OF_EXTERNAL_FILE)
        );

        testGet();
    }

    @Test
    public void testDirectoryTraversalProtectionExternal() throws Exception {
        String path = "/" + URLEncoder.encode("..\\..\\spark\\", "UTF-8") + "Spark.class";
        SparkTestUtil.UrlResponse response = doGet(path);

        assertAll(
                () -> assertThat(response.status).isEqualTo(404),
                () -> assertThat(response.body).isEqualTo(NOT_FOUND_BRO)
        );

        testGet();
    }

    private static void testGet() throws Exception {
        SparkTestUtil.UrlResponse response = testUtil.doMethod("GET", "/hello", "");

        assertAll(
                () -> assertThat(response.status).isEqualTo(200),
                () -> assertThat(response.body).contains(FO_SHIZZY)
        );
    }

    private SparkTestUtil.UrlResponse doGet(String fileName) throws Exception {
        return testUtil.doMethod("GET", fileName, null);
    }

}
