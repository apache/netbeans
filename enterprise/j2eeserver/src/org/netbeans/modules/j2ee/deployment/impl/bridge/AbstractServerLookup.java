/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.deployment.impl.bridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.openide.util.Lookup.Template;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Petr Hejl
 */
public abstract class AbstractServerLookup<T> extends AbstractLookup implements ServerRegistry.PluginListener {

    private static final Logger LOGGER = Logger.getLogger(AbstractServerLookup.class.getName());

    private static final Object LOCK = new Object();

    private final InstanceContent content;

    /** <i>GuardedBy(LOCK)</i> */
    private final Map<Server, T> serversMap = new HashMap<Server, T>();

    /** <i>GuardedBy(LOCK)</i> */
    private boolean initialized;

    protected AbstractServerLookup(InstanceContent content) {
        super(content);
        this.content = content;
    }

    public final void serverAdded(Server server) {
        stateChanged();
    }

    public final void serverRemoved(Server server) {
        stateChanged();
    }

    /**
     * May return null.
     * @param server
     * @return
     */
    protected abstract T createBridgingInstance(Server server);

    /**
     *
     * @param instance can be null
     */
    protected abstract void afterAddition(T instance);

    /**
     *
     * @param instance can be null
     */
    protected abstract void beforeFinish(T instance);

    /**
     *
     * @param server
     * @param instance can be null
     */
    protected abstract void finishBridgingInstance(Server server, T instance);

    @Override
    protected final void beforeLookup(Template<?> template) {
        if (!Thread.currentThread().getName().equals("Folder recognizer")) { // NOI18N
            init();
        }
        super.beforeLookup(template);
    }

    private void init() {
        synchronized (LOCK) {
            if (!initialized) {
                final ServerRegistry registry = ServerRegistry.getInstance();
                registry.addPluginListener(WeakListeners.create(
                        ServerRegistry.PluginListener.class, this, registry));

                LOGGER.log(Level.FINE, "Registered bridging listener"); // NOI18N

                initialized = true;
            } else {
                return;
            }
        }

        stateChanged();
    }

    private void stateChanged() {
        LOGGER.log(Level.FINE, "Updating the lookup content"); // NOI18N
        Set servers = new HashSet(ServerRegistry.getInstance().getServers());
        synchronized (LOCK) {
            for (Iterator<Map.Entry<Server, T>> it = serversMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Server, T> entry = it.next();
                Server server = entry.getKey();
                if (!servers.contains(server)) {
                    beforeFinish(entry.getValue());
                    content.remove(serversMap.get(server));
                    it.remove();

                    finishBridgingInstance(server, entry.getValue());
                } else {
                    servers.remove(server);
                }
            }

            for (Iterator it = servers.iterator(); it.hasNext();) {
                Server server = (Server) it.next();
                T instance = createBridgingInstance(server);
                if (instance != null) {
                    content.add(instance);
                    serversMap.put(server, instance);
                    afterAddition(instance);
                }
            }
        }
        LOGGER.log(Level.FINE, "Lookup content updated"); // NOI18N
    }
}
