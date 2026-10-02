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
package spark.embeddedserver;

import spark.ExceptionMapper;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.route.Routes;
import spark.staticfiles.StaticFilesConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds and uses the factories for creating different Embedded servers.
 */
public class EmbeddedServers {

    /** Default alternatives. */
    public enum Identifiers {
        /** The default, Jetty-backed embedded server. */
        JETTY
    }

    private static final Map<Object, EmbeddedServerFactory> FACTORIES = new HashMap<>();

    /**
     * Registers the default (Jetty) embedded server factory if one isn't already registered.
     */
    public static void initialize() {
        if (!FACTORIES.containsKey(Identifiers.JETTY)) {
            add(Identifiers.JETTY, new EmbeddedJettyFactory());
        }
    }

    /**
     * @return the default embedded server identifier
     */
    public static Identifiers defaultIdentifier() {
        return Identifiers.JETTY;
    }

    /**
     * @param identifier               the identifier
     * @param routeMatcher             the route matcher
     * @param staticFilesConfiguration the static files configuration object
     * @param multipleHandlers         true if other handlers exist
     * @return the created EmbeddedServer object
     * @deprecated use {@link #create(Object, Routes, ExceptionMapper, StaticFilesConfiguration, boolean)}
     */
    @Deprecated(since = "2.9.0")
    public static EmbeddedServer create(Object identifier,
                                        Routes routeMatcher,
                                        StaticFilesConfiguration staticFilesConfiguration,
                                        boolean multipleHandlers) {

        return create(identifier,
                      routeMatcher,
                      ExceptionMapper.getServletInstance(),
                      staticFilesConfiguration,
                      multipleHandlers);
    }

    /**
     * Creates an embedded server of type corresponding to the provided identifier.
     *
     * @param identifier               the identifier
     * @param routeMatcher             the route matcher
     * @param exceptionMapper          the exception mapper
     * @param staticFilesConfiguration the static files configuration object
     * @param multipleHandlers         true if other handlers exist
     * @return the created EmbeddedServer object
     */
    public static EmbeddedServer create(Object identifier,
                                        Routes routeMatcher,
                                        ExceptionMapper exceptionMapper,
                                        StaticFilesConfiguration staticFilesConfiguration,
                                        boolean multipleHandlers) {

        var factory = FACTORIES.get(identifier);

        if (factory != null) {
            return factory.create(routeMatcher, staticFilesConfiguration, exceptionMapper, multipleHandlers);
        } else {
            throw new IllegalStateException("No embedded server matching the identifier");
        }
    }

    /**
     * Adds an Embedded server factory for the provided identifier.
     *
     * @param identifier the identifier
     * @param factory    the factory
     */
    public static void add(Object identifier, EmbeddedServerFactory factory) {
        FACTORIES.put(identifier, factory);
    }

}
