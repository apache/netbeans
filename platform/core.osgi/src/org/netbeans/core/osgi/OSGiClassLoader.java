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

package org.netbeans.core.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import org.openide.util.Enumerations;
import org.openide.util.NbCollections;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Delegates to all loaded bundles, or one bundle only.
 */
class OSGiClassLoader extends ClassLoader {

    private final BundleContext context;
    private final Set<? extends Bundle> loadedBundles;
    private final Bundle bundle;

    public OSGiClassLoader(BundleContext context, Set<? extends Bundle> loadedBundles) {
        super(ClassLoader.getSystemClassLoader().getParent());
        this.context = context;
        this.loadedBundles = loadedBundles;
        bundle = null;
    }

    public OSGiClassLoader(Bundle bundle) {
        super(ClassLoader.getSystemClassLoader().getParent());
        context = null;
        loadedBundles = null;
        this.bundle = bundle;
    }

    private Iterable<Bundle> bundles() {
        if (context != null) {
            Bundle[] bundles;
            try {
                bundles = context.getBundles();
            } catch (IllegalStateException x) {
                // Thrown sometimes by Felix during shutdown. Not clear how to avoid this.
                return Collections.emptySet();
            }
            // Sort framework last so since in Felix 4 its loadClass will search app classpath, causing test failures.
            // (Tried to disable this using various framework config properties without success.)
            return NbCollections.iterable(Enumerations.concat(Enumerations.filter(Enumerations.array(bundles), new Enumerations.Processor<Bundle,Bundle>() {
                public @Override Bundle process(Bundle b, Collection<Bundle> c) {
                    if (b.getBundleId() == 0) {
                        return null;
                    }
                    if (b.getState() == Bundle.INSTALLED) {
                        return null;
                    }
                    if (!loadedBundles.contains(b)) {
                        return null;
                    }
                    return b;
                }
            }), Enumerations.singleton(context.getBundle(0))));
        } else {
            return Collections.singleton(bundle);
        }
    }

    protected @Override Class<?> findClass(String name) throws ClassNotFoundException {
        for (Bundle b : bundles()) {
            try {
                return b.loadClass(name);
            } catch (ClassNotFoundException x) {
                // normal, try next one
            }
        }
        return super.findClass(name);
    }

    protected @Override URL findResource(String name) {
        for (Bundle b : bundles()) {
            URL resource = b.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return super.findResource(name);
    }

    protected @Override Enumeration<URL> findResources(String name) throws IOException {
        List<Enumeration<URL>> resourcess = new ArrayList<Enumeration<URL>>();
        for (Bundle b : bundles()) {
            Enumeration<?> resourcesRaw = b.getResources(name);
            if (resourcesRaw == null) {
                // Oddly, this is permitted.
                continue;
            }
            Enumeration<URL> resources = NbCollections.checkedEnumerationByFilter(resourcesRaw, URL.class, true);
            if (resources != null) {
                resourcess.add(resources);
            }
        }
        return Enumerations.concat(Collections.enumeration(resourcess));
    }

    public @Override String toString() {
        if (context != null) {
            return "OSGiClassLoader[all bundles]"; // NOI18N
        } else {
            return "OSGiClassLoader[" + bundle.getSymbolicName() + "]"; // NOI18N
        }
    }

}
