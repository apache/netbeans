/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.remote.api.ui;

import org.netbeans.modules.remote.ui.spi.AutocompletionProviderFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 */
public final class AutocompletionSupport implements ConnectionListener {

    private final static WeakHashMap<Key, AutocompletionProvider> cache =
            new WeakHashMap<Key, AutocompletionProvider>();

    private AutocompletionSupport() {
        ConnectionManager.getInstance().addConnectionListener(
                WeakListeners.create(ConnectionListener.class,
                this, ConnectionManager.getInstance()));
    }

    public static AutocompletionProvider getProvider(final ExecutionEnvironment env) {
        if (env == null || !ConnectionManager.getInstance().isConnectedTo(env)) {
            return null;
        }

        AutocompletionProvider result;
        Key key = new Key(env);

        synchronized (cache) {
            result = cache.get(key);

            if (result == null) {
                Collection<? extends AutocompletionProviderFactory> factories = Lookup.getDefault().lookupAll(AutocompletionProviderFactory.class);
                ArrayList<AutocompletionProvider> providers = new ArrayList<AutocompletionProvider>();
                for (AutocompletionProviderFactory factory : factories) {
                    if (factory.supports(env)) {
                        AutocompletionProvider provider = factory.newInstance(env);
                        providers.add(provider);
                    }
                }

                result = new ProxyProvider(providers);
                cache.put(key, result);
            }
        }


        return result;
    }

    public void connected(ExecutionEnvironment env) {
        synchronized (cache) {
            cache.remove(new Key(env));
        }
    }

    public void disconnected(ExecutionEnvironment env) {
        synchronized (cache) {
            cache.remove(new Key(env));
        }
    }

    private final static class ProxyProvider implements AutocompletionProvider {

        private final ArrayList<AutocompletionProvider> providers;

        private ProxyProvider(ArrayList<AutocompletionProvider> providers) {
            this.providers = new ArrayList<AutocompletionProvider>(providers);
        }

        public List<String> autocomplete(String str) {
            SortedSet<String> set = new TreeSet<String>();

            for (AutocompletionProvider provider : providers) {
                set.addAll(provider.autocomplete(str));
            }

            return new ArrayList<String>(set);
        }
    }

    private final static class Key {

        final ExecutionEnvironment env;
        final boolean connected;

        public Key(ExecutionEnvironment env) {
            this.env = env;
            this.connected = ConnectionManager.getInstance().isConnectedTo(env);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }

            Key that = (Key) obj;
            return this.connected == that.connected && this.env.equals(that.env);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.env != null ? this.env.hashCode() : 0);
            hash = 97 * hash + (this.connected ? 1 : 0);
            return hash;
        }
    }
}
