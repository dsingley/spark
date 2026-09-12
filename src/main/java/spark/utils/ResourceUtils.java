/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spark.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utility methods for resolving resource locations to files in the
 * file system. Mainly for internal use within the framework.
 * <p>Consider using Spring's Resource abstraction in the core package
 * for handling all kinds of file resources in a uniform manner.
 * org.springframework.core.io.ResourceLoader @code getResource()
 * method can resolve any location to a org.springframework.core.io.Resource
 * object, which in turn allows one to obtain a {@code java.io.File} in the
 * file system through its {@code getFile()} method.
 * <p>The main reason for these utility methods for resource location handling
 * is to support Log4jConfigurer, which must be able to resolve
 * resource locations <i>before the logging system has been initialized</i>.
 * Spring's {@code Resource} abstraction in the core package, on the other hand,
 * already expects the logging system to be available.
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 *
 * Code copied from Spring source. Modifications made (mostly removal of methods) by Per Wendel.
 *
 */
public abstract class ResourceUtils {

    private ResourceUtils() {
    }

    /**
     * Pseudo URL prefix for loading from the class path: "classpath:"
     */
    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * URL prefix for loading from the file system: "file:"
     */
    public static final String FILE_URL_PREFIX = "file:";

    /**
     * URL protocol for a file in the file system: "file"
     */
    public static final String URL_PROTOCOL_FILE = "file";

    /**
     * URL protocol for an entry from a jar file: "jar"
     */
    public static final String URL_PROTOCOL_JAR = "jar";

    /**
     * URL protocol for an entry from a zip file: "zip"
     */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /**
     * URL protocol for an entry from a WebSphere jar file: "wsjar"
     */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /**
     * Separator between JAR URL and file path within the JAR
     */
    public static final String JAR_URL_SEPARATOR = "!/";


    /**
     * Return whether the given resource location is a URL:
     * either a special "classpath" pseudo URL or a standard URL.
     *
     * @param resourceLocation the location String to check
     * @return whether the location qualifies as a URL
     * @see #CLASSPATH_URL_PREFIX
     * @see java.net.URL
     */
    public static boolean isUrl(String resourceLocation) {
        if (resourceLocation == null) {
            return false;
        }
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            return true;
        }
        try {
            toURI(resourceLocation).toURL();
            return true;
        } catch (URISyntaxException | IllegalArgumentException | MalformedURLException ex) {
            return false;
        }
    }

    /**
     * Resolve the given resource URL to a {@code java.io.File},
     * i.e. to a file in the file system.
     *
     * @param resourceUrl the resource URL to resolve
     * @param description a description of the original resource that
     *                    the URL was created for (for example, a class path location)
     * @return a corresponding File object
     * @throws FileNotFoundException if the URL cannot be resolved to
     *                               a file in the file system
     */
    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        Assert.notNull(resourceUrl, "Resource URL must not be null");
        if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(
                    description + " cannot be resolved to absolute file path " +
                            "because it does not reside in the file system: " + resourceUrl
            );
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            // Fallback for URLs that are not valid URIs (should hardly ever happen).
            return new File(resourceUrl.getFile());
        }
    }

    /**
     * Determine whether the given URL points to a resource in the file system,
     * that is, has protocol "file" or "vfs".
     *
     * @param url the URL to check
     * @return whether the URL has been identified as a file system URL
     */
    public static boolean isFileURL(URL url) {
        var protocol = url.getProtocol();
        return (URL_PROTOCOL_FILE.equals(protocol));
    }

    /**
     * Determine whether the given URL points to a resource in a jar file,
     * that is, has protocol "jar", "zip", "wsjar" or "code-source".
     * <p>"zip" and "wsjar" are used by WebLogic Server and WebSphere, respectively,
     * but can be treated like jar files.
     *
     * @param url the URL to check
     * @return whether the URL has been identified as a JAR URL
     */
    public static boolean isJarURL(URL url) {
        var protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol));
    }

    /**
     * Extract the URL for the actual jar file from the given URL
     * (which may point to a resource in a jar file or to a jar file itself).
     *
     * @param jarUrl the original URL
     * @return the URL for the actual jar file
     * @throws MalformedURLException if no valid jar file URL could be extracted
     */
    public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
        if (separatorIndex != -1) {
            var jarFile = urlFile.substring(0, separatorIndex);
            try {
                return toURI(jarFile).toURL();
            } catch (URISyntaxException | IllegalArgumentException | MalformedURLException ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                try {
                    return toURI(FILE_URL_PREFIX + jarFile).toURL();
                } catch (URISyntaxException fallbackEx) {
                    throw new MalformedURLException(fallbackEx.getMessage());
                }
            }
        } else {
            return jarUrl;
        }
    }

    /**
     * Create a URI instance for the given URL,
     * replacing spaces with "%20" URI encoding first.
     * <p>Furthermore, this method works on JDK 1.4 as well,
     * in contrast to the {@code URL.toURI()} method.
     *
     * @param url the URL to convert into a URI instance
     * @return the URI instance
     * @throws URISyntaxException if the URL wasn't a valid URI
     * @see java.net.URL#toURI()
     */
    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    /**
     * Create a URI instance for the given location String,
     * replacing spaces with "%20" URI encoding first.
     *
     * @param location the location String to convert into a URI instance
     * @return the URI instance
     * @throws URISyntaxException if the location wasn't a valid URI
     */
    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    /**
     * Set the {@link URLConnection#setUseCaches "useCaches"} flag on the
     * given connection, preferring {@code false} but leaving the
     * flag at {@code true} for JNLP based resources.
     *
     * @param con the URLConnection to set the flag on
     */
    public static void useCachesIfNecessary(URLConnection con) {
        con.setUseCaches(con.getClass().getSimpleName().startsWith("JNLP"));
    }

}
