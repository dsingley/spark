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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import spark.util.SparkTestUtil;

import spark.util.SparkTestUtil.UrlResponse;

public class StaticFilesFromArchiveTest {

    private static SparkTestUtil testUtil;
    private static ClassLoader classLoader;
    private static ClassLoader initialClassLoader;

    @BeforeAll
    public static void beforeAll() throws Exception {
        setupClassLoader();
        testUtil = new SparkTestUtil(4567);

        Class<?> sparkClass = classLoader.loadClass("spark.Spark");

        Method staticFileLocationMethod = sparkClass.getMethod("staticFileLocation", String.class);
        staticFileLocationMethod.invoke(null, "/public-jar");

        Method initMethod = sparkClass.getMethod("init");
        initMethod.invoke(null);

        Method awaitInitializationMethod = sparkClass.getMethod("awaitInitialization");
        awaitInitializationMethod.invoke(null);
    }

    @AfterAll
    public static void afterAll() {
        Thread.currentThread().setContextClassLoader(initialClassLoader);
    }

    private static void setupClassLoader() throws Exception {
        ClassLoader extendedClassLoader = createExtendedClassLoader();
        initialClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(extendedClassLoader);
        classLoader = extendedClassLoader;
    }

    private static URLClassLoader createExtendedClassLoader() throws Exception {
        // The system classloader is no longer a URLClassLoader as of JDK 9, so its
        // entries have to come from java.class.path rather than a cast + getURLs().
        List<URL> urls = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            urls.add(Paths.get(entry).toUri().toURL());
        }

        URL publicJar = StaticFilesFromArchiveTest.class.getResource("/public-jar.zip");
        urls.add(publicJar);

        // no parent classLoader because Spark and the static resources need to be loaded from the same classloader
        return new URLClassLoader(urls.toArray(new URL[0]), null);
    }

    @Test
    public void testCss() throws Exception {
        UrlResponse response = testUtil.doMethod("GET", "/css/style.css", null);

        String expectedContentType = response.headers.get("Content-Type");
        assertThat(expectedContentType).isEqualTo("text/css");

        String body = response.body;
        assertThat(body).isEqualTo("Content of css file");
    }
}
