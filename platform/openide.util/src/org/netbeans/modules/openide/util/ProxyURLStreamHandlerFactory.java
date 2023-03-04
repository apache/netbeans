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

package org.netbeans.modules.openide.util;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * @see URLStreamHandlerRegistration
 */
@ServiceProvider(service=URLStreamHandlerFactory.class)
public final class ProxyURLStreamHandlerFactory implements URLStreamHandlerFactory {
    /** prevents GC only */
    private final Map<String, Lookup.Result<URLStreamHandler>> results = new HashMap<String, Lookup.Result<URLStreamHandler>>();
    private final Map<String, URLStreamHandler> handlers = new HashMap<String, URLStreamHandler>();
    private static final Set<String> STANDARD_PROTOCOLS = new HashSet<String>(Arrays.asList("jar", "file", "http", "https", "resource")); // NOI18N

    public @Override synchronized URLStreamHandler createURLStreamHandler(final String protocol) {
        if (STANDARD_PROTOCOLS.contains(protocol)) {
            // Well-known handlers in JRE. Do not try to initialize lookup.
            return null;
        }
        if (!results.containsKey(protocol)) {
            final Lookup.Result<URLStreamHandler> result = Lookups.forPath("URLStreamHandler/" + protocol).lookupResult(URLStreamHandler.class);
            LookupListener listener = new LookupListener() {
                public @Override void resultChanged(LookupEvent ev) {
                    synchronized (ProxyURLStreamHandlerFactory.this) {
                        Collection<? extends URLStreamHandler> instances = result.allInstances();
                        handlers.put(protocol, instances.isEmpty() ? null : instances.iterator().next());
                    }
                }
            };
            result.addLookupListener(listener);
            listener.resultChanged(null);
            results.put(protocol, result);
        }
        return handlers.get(protocol);
    }
}
