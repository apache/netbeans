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

package org.netbeans.modules.server;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 */
public final class ServerRegistry {

    public static final String SERVERS_PATH = "Servers"; // NOI18N
    public static final String CLOUD_PATH = "Cloud"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(ServerRegistry.class.getName());

    private static ServerRegistry registry;
    private static ServerRegistry cloudRegistry;

    private static ProviderLookupListener l;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final Lookup.Result<ServerInstanceProvider> result;

    private final Lookup lookup;
    
    private final String path;
    
    private final boolean cloud;

    private ServerRegistry(String path, boolean cloud) {
        this.path = path;
        this.cloud = cloud;
        lookup = Lookups.forPath(path);
        result = lookup.lookupResult(ServerInstanceProvider.class);
    }

    public String getPath() {
        return path;
    }

    public boolean isCloud() {
        return cloud;
    }
    
    public static synchronized ServerRegistry getInstance() {
        if (registry == null) {
            registry = new ServerRegistry(SERVERS_PATH, false);
            registry.result.allItems();
            registry.result.addLookupListener(l = new ProviderLookupListener(registry.changeSupport));
        }
        return registry;
    }

    public static synchronized ServerRegistry getCloudInstance() {
        if (cloudRegistry == null) {
            cloudRegistry = new ServerRegistry(CLOUD_PATH, true);
            cloudRegistry.result.allItems();
            cloudRegistry.result.addLookupListener(l = new ProviderLookupListener(cloudRegistry.changeSupport));
        }
        return cloudRegistry;
    }

    public Collection<? extends ServerInstanceProvider> getProviders() {
        Collection<? extends ServerInstanceProvider> ret = result.allInstances();
        LOGGER.log(Level.FINE, "Returning providers {0}", ret);
        return ret;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private static class ProviderLookupListener implements LookupListener {

        private final ChangeSupport changeSupport;

        public ProviderLookupListener(ChangeSupport changeSupport) {
            this.changeSupport = changeSupport;
        }

        public void resultChanged(LookupEvent ev) {
            LOGGER.log(Level.FINE, "Provider lookup change {0}", ev);
            changeSupport.fireChange();
        }

    }
}
