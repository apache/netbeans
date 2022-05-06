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
package org.openide.util;

import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/*******************************************************************************
 * ClassLoader whose special trick is inserting debug information
 * into any *.properties files it loads.
 ******************************************************************************/
final class DebugLoader extends ClassLoader {

    // global bundle index, each loaded bundle gets its own 
    private static int count = 0;

    // indices of known bundles; needed since DebugLoader's can be collected
    // when softly reachable, but this should be transparent to the user
    private static final Map<String, Integer> knownIDs = new HashMap<>();

    // cache of existing debug loaders for regular loaders 
    private static final Map<ClassLoader, Reference<ClassLoader>> existing = new WeakHashMap<>();

    //--------------------------------------------------------------------------
    private DebugLoader(final ClassLoader parent) {

        super(parent);
    }

    //--------------------------------------------------------------------------
    private static int getID(final String name) {

        synchronized (knownIDs) {
            return knownIDs.computeIfAbsent(name, n -> ++count);
        }
    }

    //--------------------------------------------------------------------------
    static ClassLoader get(final ClassLoader parent) {

        synchronized (existing) {
            final Reference<ClassLoader> ref = existing.get(parent);
            if (ref != null && ref.get() != null) {
                return ref.get();
            } else {
                final ClassLoader loader = new DebugLoader(parent);
                existing.put(parent, new WeakReference<>(loader));
                return loader;
            }
        }
    }

    //--------------------------------------------------------------------------
    @Override
    public InputStream getResourceAsStream(final String name) {

        final InputStream base = super.getResourceAsStream(name);

        if (base != null && name.endsWith(".properties")) { // NOI18N
            final boolean localizable = name.contains("Bundle"); // NOI18N
            return new AnnotatedResourceInputStream(base, getID(name), localizable);
        } else {
            return base;
        }
    }

    // [PENDING] getResource not overridden; but ResourceBundle uses getResourceAsStream anyhow
}
