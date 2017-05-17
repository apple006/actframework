package act.ws;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2017 ActFramework
 * %%
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
 * #L%
 */

import act.Destroyable;
import act.util.DestroyableBase;
import act.xio.WebSocketConnection;
import org.osgl.$;
import org.osgl.util.C;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Organize websocket connection by string typed keys. One key can be used to
 * locate a list of websocket connection
 */
public class WebSocketConnectionRegistry extends DestroyableBase {
    private ConcurrentMap<String, List<WebSocketConnection>> registry = new ConcurrentHashMap<>();

    public List<WebSocketConnection> get(String key) {
        final List<WebSocketConnection> retList = new ArrayList<>();
        accept(key, C.F.addTo(retList));
        return retList;
    }

    public void accept(String key, $.Function<WebSocketConnection, ?> visitor) {
        List<WebSocketConnection> connections = registry.get(key);
        if (!connections.isEmpty()) {
            List<WebSocketConnection> toBeCleared = null;
            for (WebSocketConnection conn : connections) {
                if (conn.closed()) {
                    if (null == toBeCleared) {
                        toBeCleared = new ArrayList<>();
                    }
                    toBeCleared.add(conn);
                }
                visitor.apply(conn);
            }
            if (null != toBeCleared) {
                List<WebSocketConnection> originalCopy = registry.get(key);
                originalCopy.removeAll(toBeCleared);
            }
        }
    }

    public void register(String key, WebSocketConnection connection) {
        List<WebSocketConnection> connections = registry.get(key);
        if (null == connections) {
            List<WebSocketConnection> newConnections = new Vector<>();
            connections = registry.putIfAbsent(key, newConnections);
            if (null == connections) {
                connections = newConnections;
            }
        }
        connections.add(connection);
    }

    @Override
    protected void releaseResources() {
        for (List<WebSocketConnection> connections : registry.values()) {
            Destroyable.Util.tryDestroyAll(connections, ApplicationScoped.class);
        }
        registry.clear();
    }
}
