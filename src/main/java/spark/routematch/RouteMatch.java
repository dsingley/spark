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
package spark.routematch;

import spark.route.HttpMethod;

/**
 * A route or filter that matched a request, along with how it matched. It holds the matched
 * {@link #getTarget() target}, the path pattern it was mapped with, the URI that was requested,
 * the accepted media type and the HTTP method.
 * <p>
 * Routes find these when handling a request, and {@link spark.Service#routes()} returns one for
 * every route and filter that has been mapped, which is handy for listing the routes at startup.
 *
 * @author Per Wendel
 */
public class RouteMatch {

    private final Object target;
    private final String matchUri;
    private final String requestURI;
    private final String acceptType;
    private final HttpMethod httpMethod;

    /**
     * Creates a match that has no HTTP method.
     *
     * @param target     the matched route or filter
     * @param matchUri   the path pattern of the matched route or filter
     * @param requestUri the URI of the request
     * @param acceptType the accepted media type
     */
    public RouteMatch(Object target, String matchUri, String requestUri, String acceptType) {
        this(target, matchUri, requestUri, acceptType, null);
     }

    /**
     * Creates a match.
     *
     * @param target     the matched route or filter
     * @param matchUri   the path pattern of the matched route or filter
     * @param requestUri the URI of the request
     * @param acceptType the accepted media type
     * @param httpMethod the HTTP method of the matched route or filter, which may be null
     */
    public RouteMatch(Object target, String matchUri, String requestUri, String acceptType, HttpMethod httpMethod) {
        super();
        this.target = target;
        this.matchUri = matchUri;
        this.requestURI = requestUri;
        this.acceptType = acceptType;
        this.httpMethod = httpMethod;
    }

    /**
     * @return the HTTP method
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * @return the accepted media type
     */
    public String getAcceptType() {
        return acceptType;
    }

    /**
     * @return the target
     */
    public Object getTarget() {
        return target;
    }


    /**
     * @return the matchUri
     */
    public String getMatchUri() {
        return matchUri;
    }


    /**
     * @return the requestUri
     */
    public String getRequestURI() {
        return requestURI;
    }


}
