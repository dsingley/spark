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

import static java.util.Objects.requireNonNull;

import org.eclipse.jetty.ee11.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee11.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee11.websocket.server.JettyWebSocketCreator;

/**
 * Factory class to create {@link JettyWebSocketCreator} implementations that
 * delegate to the given handler class.
 */
public class WebSocketCreatorFactory {

    private WebSocketCreatorFactory() {
    }

    /**
     * Creates a {@link JettyWebSocketCreator} that gets the handler for each WebSocket connection from the
     * given wrapper: a new instance of the handler class for every connection, or the one handler instance
     * for all of them, depending on what the wrapper wraps.
     *
     * @param handlerWrapper The wrapped handler to use to manage WebSocket connections.
     * @return The JettyWebSocketCreator.
     */
    public static JettyWebSocketCreator create(WebSocketHandlerWrapper handlerWrapper) {
        return new SparkWebSocketCreator(handlerWrapper);
    }

    // Package protected to be visible to the unit tests
    static class SparkWebSocketCreator implements JettyWebSocketCreator {
        private final WebSocketHandlerWrapper handlerWrapper;

        private SparkWebSocketCreator(WebSocketHandlerWrapper handlerWrapper) {
            this.handlerWrapper = requireNonNull(handlerWrapper, "handlerWrapper cannot be null");
        }

        @Override
        public Object createWebSocket(JettyServerUpgradeRequest request, JettyServerUpgradeResponse response) {
            return handlerWrapper.getHandler();
        }
    }
}
