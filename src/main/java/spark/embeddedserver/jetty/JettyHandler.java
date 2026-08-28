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
package spark.embeddedserver.jetty;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.SessionCookieConfig;

import org.eclipse.jetty.ee11.servlet.FilterHolder;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;

/**
 * Jetty {@link Handler} that dispatches every request through Spark's {@link Filter}
 * (typically a {@code MatcherFilter}). Wraps a {@link ServletContextHandler} so the
 * filter runs inside a real servlet context (sessions, request wrapping, etc.) while
 * still behaving as a single top-level {@link Handler} in the embedded server's chain.
 *
 * @author Per Wendel
 */
public class JettyHandler extends Handler.Wrapper {

    private final ServletContextHandler context;

    public JettyHandler(Filter filter) {
        super(newContext(filter));
        this.context = (ServletContextHandler) getHandler();
    }

    private static ServletContextHandler newContext(Filter filter) {
        ServletContextHandler context = new ServletContextHandler("/", ServletContextHandler.SESSIONS);
        context.addFilter(new FilterHolder(filter), "/*", EnumSet.of(DispatcherType.REQUEST));
        // Jetty 12 rejects an encoded slash (%2F) within a path segment by default at the
        // servlet layer, independently of the connector's UriCompliance (see
        // SocketConnectorFactory). Spark has always allowed it — route params/splats are built
        // from URL-decoded segments — so opt back in here too to preserve that behavior.
        context.getServletHandler().setDecodeAmbiguousURIs(true);
        return context;
    }

    public SessionCookieConfig getSessionCookieConfig() {
        return context.getSessionHandler().getSessionCookieConfig();
    }

}
