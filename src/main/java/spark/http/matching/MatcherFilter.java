/*
 * Copyright 2011- Per Wendel
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
package spark.http.matching;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.CustomErrorPages;
import spark.ExceptionMapper;
import spark.HaltException;
import spark.RequestResponseFactory;
import spark.embeddedserver.jetty.HttpRequestWrapper;
import spark.route.HttpMethod;
import spark.serialization.SerializerChain;
import spark.staticfiles.StaticFilesConfiguration;

import java.io.IOException;

/**
 * Matches Spark routes and filters.
 *
 * @author Per Wendel
 */
public class MatcherFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(MatcherFilter.class);

    private static final String ACCEPT_TYPE_REQUEST_MIME_HEADER = "Accept";
    private static final String HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";

    private final StaticFilesConfiguration staticFiles;

    private final spark.route.Routes routeMatcher;
    private final SerializerChain serializerChain;
    private final ExceptionMapper exceptionMapper;

    private final UnmatchedRequestHandling unmatchedRequestHandling;

    /**
     * What this filter should do with a request that Spark itself didn't consume (no route
     * matched, and no before/after filter produced a body).
     */
    public enum UnmatchedRequestHandling {

        /**
         * Leave the request marked as not consumed and return without responding, so that
         * other handlers/filters further down the chain get a chance to process it. Used when
         * Spark isn't the only thing handling requests, e.g. embedded mode with additional
         * Jetty handlers, or a servlet filter chain with other filters/servlets after this one.
         */
        DELEGATE_TO_OTHER_HANDLERS,

        /**
         * Have Spark respond directly with a 404 (or a registered custom 404 page). Used when
         * Spark is the sole handler of the request, so there's nothing else to delegate to.
         */
        RESPOND_NOT_FOUND
    }

    /**
     * Constructor
     *
     * @param routeMatcher      The route matcher
     * @param staticFiles       The static files configuration object
     * @param exceptionMapper   The exception mapper
     * @param externalContainer unused; kept only so existing callers keep compiling and running unchanged
     * @param hasOtherHandlers  If true, do nothing if request is not consumed by Spark in order to let others handlers process the request.
     * @deprecated replaced by {@link #MatcherFilter(spark.route.Routes, StaticFilesConfiguration, ExceptionMapper, UnmatchedRequestHandling)},
     *             which drops the unused {@code externalContainer} parameter and replaces the
     *             {@code hasOtherHandlers} boolean with the more descriptive {@link UnmatchedRequestHandling}
     */
    @Deprecated(since = "3.0.0")
    public MatcherFilter(spark.route.Routes routeMatcher,
                         StaticFilesConfiguration staticFiles,
                         ExceptionMapper exceptionMapper,
                         boolean externalContainer,
                         boolean hasOtherHandlers) {
        this(routeMatcher, staticFiles, exceptionMapper,
             hasOtherHandlers ? UnmatchedRequestHandling.DELEGATE_TO_OTHER_HANDLERS : UnmatchedRequestHandling.RESPOND_NOT_FOUND);
    }

    /**
     * Constructor
     *
     * @param routeMatcher              The route matcher
     * @param staticFiles               The static files configuration object
     * @param exceptionMapper           The exception mapper
     * @param unmatchedRequestHandling  What to do with a request Spark itself didn't consume
     */
    public MatcherFilter(spark.route.Routes routeMatcher,
                         StaticFilesConfiguration staticFiles,
                         ExceptionMapper exceptionMapper,
                         UnmatchedRequestHandling unmatchedRequestHandling) {

        this.routeMatcher = routeMatcher;
        this.staticFiles = staticFiles;
        this.exceptionMapper = exceptionMapper;
        this.unmatchedRequestHandling = unmatchedRequestHandling;
        this.serializerChain = new SerializerChain();
    }

    @Override
    public void init(FilterConfig config) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) servletRequest;
        var httpResponse = (HttpServletResponse) servletResponse;

        // handle static resources
        boolean consumedByStaticFile = staticFiles.consume(httpRequest, httpResponse);

        if (consumedByStaticFile) {
            return;
        }

        var method = getHttpMethodFrom(httpRequest);

        var httpMethodStr = method.toLowerCase();
        var uri = httpRequest.getRequestURI();
        var acceptType = httpRequest.getHeader(ACCEPT_TYPE_REQUEST_MIME_HEADER);

        var body = Body.create();

        var requestWrapper = RequestWrapper.create();
        var responseWrapper = ResponseWrapper.create();

        var response = RequestResponseFactory.create(httpResponse);

        var httpMethod = HttpMethod.get(httpMethodStr);

        var context = RouteContext.create()
                .withMatcher(routeMatcher)
                .withHttpRequest(httpRequest)
                .withUri(uri)
                .withAcceptType(acceptType)
                .withBody(body)
                .withRequestWrapper(requestWrapper)
                .withResponseWrapper(responseWrapper)
                .withResponse(response)
                .withHttpMethod(httpMethod);

        try {
            try {

                BeforeFilters.execute(context);
                Routes.execute(context);
                AfterFilters.execute(context);

            } catch (HaltException halt) {

                Halt.modify(httpResponse, body, halt);

            } catch (Exception generalException) {

                GeneralError.modify(
                        httpRequest,
                        httpResponse,
                        body,
                        requestWrapper,
                        responseWrapper,
                        exceptionMapper,
                        generalException);

            }

            // If redirected and content is null set to empty string to not throw NotConsumedException
            if (body.notSet() && responseWrapper.isRedirected()) {
                body.set("");
            }

            if (body.notSet() && unmatchedRequestHandling == UnmatchedRequestHandling.DELEGATE_TO_OTHER_HANDLERS
                    && servletRequest instanceof HttpRequestWrapper servletRequestWrapper) {
                servletRequestWrapper.notConsumed(true);
                return;
            }

            if (body.notSet()) {
                LOG.info("The requested route [{}] has not been mapped in Spark for {}: [{}]",
                         uri, ACCEPT_TYPE_REQUEST_MIME_HEADER, acceptType);
                httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);

                if (CustomErrorPages.existsFor(404)) {
                    requestWrapper.setDelegate(RequestResponseFactory.create(httpRequest));
                    responseWrapper.setDelegate(RequestResponseFactory.create(httpResponse));
                    body.set(CustomErrorPages.getFor(404, requestWrapper, responseWrapper));
                } else {
                    body.set(String.format(CustomErrorPages.NOT_FOUND));
                }
            }
        } finally {
            try {
                AfterAfterFilters.execute(context);
            } catch (Exception generalException) {
                GeneralError.modify(
                        httpRequest,
                        httpResponse,
                        body,
                        requestWrapper,
                        responseWrapper,
                        exceptionMapper,
                        generalException);
            }
        }

        if (body.isSet()) {
            body.serializeTo(httpResponse, serializerChain, httpRequest);
        } else if (chain != null) {
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    private String getHttpMethodFrom(HttpServletRequest httpRequest) {
        String method = httpRequest.getHeader(HTTP_METHOD_OVERRIDE_HEADER);

        if (method == null) {
            method = httpRequest.getMethod();
        }
        return method;
    }

    @Override
    public void destroy() {
        // no-op
    }
}
