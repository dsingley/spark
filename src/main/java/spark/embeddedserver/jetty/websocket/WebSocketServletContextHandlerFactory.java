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
package spark.embeddedserver.jetty.websocket;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;

/**
 * Creates websocket servlet context handlers.
 *
 * <p><b>Temporarily stubbed during the Jetty 9 → 12 migration.</b> {@link WebSocketHandlerWrapper}
 * now fails fast on any attempt to register a WebSocket handler, so {@code webSocketHandlers} is
 * always {@code null} here in practice. See the Phase 3 tracking issue for the EE11 restoration.
 */
public class WebSocketServletContextHandlerFactory {

    /**
     * Creates a new websocket servlet context handler.
     *
     * @param webSocketHandlers          webSocketHandlers
     * @param webSocketIdleTimeoutMillis webSocketIdleTimeoutMillis
     * @return {@code null} — WebSocket support is temporarily unavailable; see class javadoc.
     */
    public static ServletContextHandler create(Map<String, WebSocketHandlerWrapper> webSocketHandlers,
                                               Optional<Long> webSocketIdleTimeoutMillis) {
        if (webSocketHandlers != null) {
            throw new UnsupportedOperationException(
                    "WebSocket support is being migrated to Jetty 12's EE11 websocket API and is "
                            + "temporarily unavailable. See the Phase 3 tracking issue in the Jetty 12 migration.");
        }
        return null;
    }

}
