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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: consider ETag support. Static files are served with Cache-Control and Expires headers (see
// setExpireTimeSeconds), but nothing sets a validator such as ETag, so clients cannot revalidate a
// cached file with a conditional request.
/**
 * The static file settings of a {@link spark.Service}, and the code that serves the files.
 * <p>
 * It holds where the static files are (a folder on the classpath and/or an external folder), the
 * extra headers to send with them, such as Cache-Control, and the resource handlers that look
 * files up. For each request, {@code consume} serves the matching file if there is one, and tells
 * the caller whether it did, so that routes only handle the requests that were not for a static file.
 */
public class StaticFilesConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(StaticFilesConfiguration.class);

    private List<AbstractResourceHandler> staticResourceHandlers = null;

    private boolean staticResourcesSet = false;
    private boolean externalStaticResourcesSet = false;

    /** The configuration shared by the Spark applications that run from a servlet container. */
    public static final StaticFilesConfiguration servletInstance = new StaticFilesConfiguration();

    private final Map<String, String> customHeaders = new HashMap<>();

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
        if (staticResourceHandlers != null) {

            for (var staticResourceHandler : staticResourceHandlers) {

                var resource = staticResourceHandler.getResource(httpRequest);

                if (resource != null && resource.isReadable()) {

                    if (MimeType.shouldGuess()) {
                        httpResponse.setHeader(MimeType.CONTENT_TYPE, MimeType.fromResource(resource));
                    }
                    customHeaders.forEach(httpResponse::setHeader); //add all user-defined headers to response

                    try (var inputStream = resource.getInputStream();
                         var wrappedOutputStream = GzipUtils.checkAndWrap(httpRequest, httpResponse, false)) {
                        IOUtils.copy(inputStream, wrappedOutputStream);
                    }

                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Clears all static file configuration
     */
    public void clear() {

        if (staticResourceHandlers != null) {
            staticResourceHandlers.clear();
            staticResourceHandlers = null;
        }

        staticResourcesSet = false;
        externalStaticResourcesSet = false;
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
    public synchronized void configure(String folder) {
        Assert.notNull(folder, "'folder' must not be null");

        if (!staticResourcesSet) {

            if (staticResourceHandlers == null) {
                staticResourceHandlers = new ArrayList<>();
            }

            staticResourceHandlers.add(new ClassPathResourceHandler(folder, "index.html"));
            LOG.info("StaticResourceHandler configured with folder = {}", folder);
            staticResourcesSet = true;
        }
    }

    /**
     * Configures location for static resources
     *
     * @param folder the location
     */
    public synchronized void configureExternal(String folder) {
        Assert.notNull(folder, "'folder' must not be null");

        if (!externalStaticResourcesSet) {
            try {
                var resource = new ExternalResource(folder);
                if (!resource.getFile().isDirectory()) {
                    LOG.error("External Static resource location must be a folder");
                    return;
                }

                if (staticResourceHandlers == null) {
                    staticResourceHandlers = new ArrayList<>();
                }
                staticResourceHandlers.add(new ExternalResourceHandler(folder, "index.html"));
                LOG.info("External StaticResourceHandler configured with folder = {}", folder);
            } catch (IOException e) {
                LOG.error("Error when creating external StaticResourceHandler", e);
            }

            externalStaticResourcesSet = true;
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
     * headers on responses for them.
     *
     * @param expireTimeSeconds how long, in seconds, clients may cache static files
     */
    public void setExpireTimeSeconds(long expireTimeSeconds) {
        customHeaders.put("Cache-Control", "private, max-age=" + expireTimeSeconds);
        customHeaders.put("Expires", new Date(System.currentTimeMillis() + (expireTimeSeconds * 1000)).toString());
    }

    /**
     * Adds headers to add to responses for static files, replacing any that have the same name.
     *
     * @param headers the header names and values
     */
    public void putCustomHeaders(Map<String, String> headers) {
        customHeaders.putAll(headers);
    }

    /**
     * Adds a header to add to responses for static files, replacing one with the same name.
     *
     * @param key   the header name
     * @param value the header value
     */
    public void putCustomHeader(String key, String value) {
        customHeaders.put(key, value);
    }
}
