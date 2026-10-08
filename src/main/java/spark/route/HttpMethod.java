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
package spark.route;

import java.util.HashMap;

/**
 * The HTTP methods that routes can be mapped for, along with {@code before}, {@code after} and
 * {@code afterafter}, which Spark's filters use internally so that they are matched in the same way
 * as routes. {@link #unsupported} stands for any other method.
 */
@SuppressWarnings("java:S115")  // ignore lowercase enum constants
public enum HttpMethod {
    /** HTTP GET. */
    get,
    /** HTTP POST. */
    post,
    /** HTTP PUT. */
    put,
    /** HTTP PATCH. */
    patch,
    /** HTTP DELETE. */
    delete,
    /** HTTP HEAD. */
    head,
    /** HTTP TRACE. */
    trace,
    /** HTTP CONNECT. */
    connect,
    /** HTTP OPTIONS. */
    options,
    /** Spark's "before" filter, matched like an HTTP method internally. */
    before,
    /** Spark's "after" filter, matched like an HTTP method internally. */
    after,
    /** Spark's "afterAfter" filter, run after matching routes even if one throws. */
    afterafter,
    /** Returned when a request's method string doesn't match any known value. */
    unsupported;

    private static final HashMap<String, HttpMethod> METHODS = new HashMap<>();

    static {
        for (var method : values()) {
            METHODS.put(method.toString(), method);
        }
    }

    /**
     * Gets the HttpMethod corresponding to the provided string. If no corresponding method can be found
     * {@link spark.route.HttpMethod#unsupported} will be returned.
     *
     * @param methodStr The string containing HTTP method name
     * @return          The HttpMethod corresponding to the provided string
     */
    public static HttpMethod get(String methodStr) {
        var method = METHODS.get(methodStr);
        return method != null ? method : unsupported;
    }
}
