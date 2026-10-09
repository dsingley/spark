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
package spark.staticfiles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.resource.AbstractResourceHandler;
import spark.resource.ClassPathResourceHandler;
import spark.resource.ExternalResource;
import spark.resource.ExternalResourceHandler;
import spark.utils.Assert;
import spark.utils.GzipUtils;
import spark.utils.IOUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * The static file settings of a {@link spark.Service}, and the code that serves the files.
 * <p>
 * It holds where the static files are (a folder on the classpath and/or an external folder), the
 * extra headers to send with them, such as Cache-Control, and the resource handlers that look
 * files up. For each request, {@code consume} serves the matching file if there is one, and tells
 * the caller whether it did, so that routes only handle the requests that were not for a static file.
 * <p>
 * It is safe to change the configuration, for example to add a header, while requests are being served.
 */
public class StaticFilesConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(StaticFilesConfiguration.class);

    // The format of an HTTP date, such as the value of the Expires header: for example
    // "Fri, 09 Oct 2026 02:45:51 GMT", always in English and in GMT
    private static final DateTimeFormatter HTTP_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    // HTTP/1.1 says servers should not send an Expires date more than a year in the future
    private static final long MAX_EXPIRES_SECONDS = Duration.ofDays(365).toSeconds();

    // Request threads read the handlers and the headers while they can be changed from other threads,
    // so the handlers are in a list that is safe to iterate while it changes, and the headers are an
    // immutable map that is replaced as a whole, which also makes changing several of them atomic.
    private final List<AbstractResourceHandler> staticResourceHandlers = new CopyOnWriteArrayList<>();
    private final AtomicReference<Map<String, String>> customHeaders = new AtomicReference<>(Map.of());

    private volatile boolean staticResourcesSet = false;
    private volatile boolean externalStaticResourcesSet = false;

    // Serializes the changes to the configuration. It is private, unlike the instance itself, which
    // anyone can lock on through the public servletInstance.
    private final Object lock = new Object();

    /** The configuration shared by the Spark applications that run from a servlet container. */
    public static final StaticFilesConfiguration servletInstance = new StaticFilesConfiguration();

    /**
     * Attempt consuming using either static resource handlers or jar resource handlers
     *
     * @param httpRequest  The HTTP servlet request.
     * @param httpResponse The HTTP servlet response.
     * @return true if consumed, false otherwise.
     * @throws IOException in case of IO error.
     */
    public boolean consume(HttpServletRequest httpRequest,
                           HttpServletResponse httpResponse) throws IOException {
        try {
            if (consumeWithFileResourceHandlers(httpRequest, httpResponse)) {
                return true;
            }

        } catch (DirectoryTraversal.DirectoryTraversalDetection directoryTraversalDetection) {
            httpResponse.setStatus(400);
            httpResponse.getWriter().write("Bad request");
            httpResponse.getWriter().flush();
            LOG.warn("{} directory traversal detection for path: {}",
                directoryTraversalDetection.getMessage(), httpRequest.getPathInfo());
        }
        return false;
    }


    private boolean consumeWithFileResourceHandlers(HttpServletRequest httpRequest,
                                                    HttpServletResponse httpResponse) throws IOException {
        for (var staticResourceHandler : staticResourceHandlers) {

            var resource = staticResourceHandler.getResource(httpRequest);

            if (resource != null && resource.isReadable()) {

                if (MimeType.shouldGuess()) {
                    httpResponse.setHeader(MimeType.CONTENT_TYPE, MimeType.fromResource(resource));
                }
                customHeaders.get().forEach(httpResponse::setHeader); //add all user-defined headers to response

                try (var inputStream = resource.getInputStream();
                     var wrappedOutputStream = GzipUtils.checkAndWrap(httpRequest, httpResponse, false)) {
                    IOUtils.copy(inputStream, wrappedOutputStream);
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Clears all static file configuration
     */
    public void clear() {
        synchronized (lock) {
            staticResourceHandlers.clear();
            staticResourcesSet = false;
            externalStaticResourcesSet = false;
        }
    }
    
    /**
     * @return true if a static files location on the classpath has been configured
     */
    public boolean isStaticResourcesSet() {
        return staticResourcesSet;
    }
    
    /**
     * @return true if an external static files location, outside the classpath, has been configured
     */
    public boolean isExternalStaticResourcesSet() {
        return externalStaticResourcesSet;
    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public void configure(String folder) {
        Assert.notNull(folder, "'folder' must not be null");

        synchronized (lock) {
            if (!staticResourcesSet) {
                staticResourceHandlers.add(new ClassPathResourceHandler(folder, "index.html"));
                LOG.info("StaticResourceHandler configured with folder = {}", folder);
                staticResourcesSet = true;
            }
        }
    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public void configureExternal(String folder) {
        Assert.notNull(folder, "'folder' must not be null");

        synchronized (lock) {
            if (!externalStaticResourcesSet) {
                try {
                    var resource = new ExternalResource(folder);
                    if (!resource.getFile().isDirectory()) {
                        LOG.error("External Static resource location must be a folder");
                        return;
                    }

                    staticResourceHandlers.add(new ExternalResourceHandler(folder, "index.html"));
                    LOG.info("External StaticResourceHandler configured with folder = {}", folder);
                } catch (IOException e) {
                    LOG.error("Error when creating external StaticResourceHandler", e);
                }

                externalStaticResourcesSet = true;
            }
        }
    }

    /**
     * Creates a new configuration with no static files location and no custom headers.
     *
     * @return the new configuration
     */
    public static StaticFilesConfiguration create() {
        return new StaticFilesConfiguration();
    }

    /**
     * Makes clients cache static files for the given time, by setting the Cache-Control and Expires
     * headers on responses for them. The Expires date is at most a year ahead, as HTTP recommends.
     *
     * @param expireTimeSeconds how long, in seconds, clients may cache static files
     */
    public void setExpireTimeSeconds(long expireTimeSeconds) {
        updateCustomHeaders(headers -> {
            headers.put("Cache-Control", "private, max-age=" + expireTimeSeconds);
            var expires = Instant.now().plusSeconds(Math.min(expireTimeSeconds, MAX_EXPIRES_SECONDS));
            headers.put("Expires", HTTP_DATE_FORMAT.format(expires));
        });
    }

    /**
     * Adds headers to add to responses for static files, replacing any that have the same name.
     *
     * @param headers the header names and values
     */
    public void putCustomHeaders(Map<String, String> headers) {
        updateCustomHeaders(current -> current.putAll(headers));
    }

    /**
     * Adds a header to add to responses for static files, replacing one with the same name.
     *
     * @param key   the header name
     * @param value the header value
     */
    public void putCustomHeader(String key, String value) {
        updateCustomHeaders(headers -> headers.put(key, value));
    }

    /**
     * Applies a change to a copy of the custom headers and then makes the copy the current headers, so
     * that requests being served see either all of the change or none of it. If another thread changes
     * the headers at the same time, the change is applied again to the newer headers, so no update is
     * lost; this is why the change must only modify the map it is given.
     */
    private void updateCustomHeaders(Consumer<Map<String, String>> change) {
        customHeaders.updateAndGet(current -> {
            var copy = new HashMap<>(current);
            change.accept(copy);
            return Collections.unmodifiableMap(copy);
        });
    }
}
