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
package org.netbeans;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Keeps the coverage of various packages by existing ProxyClassLoaders.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ProxyClassPackages {
    private ProxyClassPackages() {
    }

    /** A shared map of all packages known by all classloaders. Also covers META-INF based resources.
     * It contains two kinds of keys: dot-separated package names and slash-separated
     * META-INF resource names, e.g. {"org.foobar", "/services/org.foobar.Foo"}
     */
    private static final ConcurrentMap<String, Set<ProxyClassLoader>> packageCoverage = new ConcurrentHashMap<>();

    static void addCoveredPackages( ProxyClassLoader loader, Iterable<String> coveredPackages) {
        synchronized(packageCoverage) {
            for (String pkg : coveredPackages) {
                Set<ProxyClassLoader> delegates = ProxyClassPackages.packageCoverage.get(pkg); 
                if (delegates == null) { 
                    delegates = Collections.<ProxyClassLoader>singleton(loader);
                    ProxyClassPackages.packageCoverage.put(pkg, delegates);
                } else if (delegates.size() == 1) {
                    delegates = new HashSet<ProxyClassLoader>(delegates);
                    ProxyClassPackages.packageCoverage.put(pkg, delegates);
                    delegates.add(loader);
                } else {
                    delegates.add(loader);
                }
            }
        }
    }
    
    static void removeCoveredPakcages(ProxyClassLoader loader) {
        synchronized (packageCoverage) {
            packageCoverage.values().removeIf( (Set<ProxyClassLoader> set) -> {
                if (set.contains(loader) && set.size() == 1) {
                    return true;
                } else {
                    set.remove(loader);
                    return false;
                }
            } );
        }
    }

    static Set<ProxyClassLoader> findCoveredPkg(String pkg) {
        return packageCoverage.computeIfPresent(pkg, (k, v) -> {
            return v;
        });
    }
    
}
