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
package org.netbeans;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Encapsulates operations on parents.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ProxyClassParents {
    /** All parents of this classloader, including their parents recursively */
    private final Set<ProxyClassLoader> parentSet;
    private final boolean transitive;
    private final ClassLoader systemCL;

    private ProxyClassParents(
        ClassLoader systemCL, 
        Set<ProxyClassLoader> parentSet, 
        boolean transitive
    ) {
        this.systemCL = systemCL;
        this.parentSet = parentSet;
        // = new LinkedHashSet<ProxyClassLoader>();
        this.transitive = transitive;
    }
    
    
    /** Coalesce parent classloaders into an optimized set.
     * This means that all parents of the specified classloaders
     * are also added recursively, removing duplicates along the way.
     * Search order should be preserved (parents before children, stable w.r.t. inputs).
     * @param loaders list of suggested parents (no nulls or duplicates permitted)
     * @return optimized list of parents (no nulls or duplicates)
     * @throws IllegalArgumentException if there are cycles
     */
    static ProxyClassParents coalesceParents(
        ClassLoader root,
        ClassLoader[] loaders, ClassLoader systemCL, boolean transitive
    ) throws IllegalArgumentException {
        final ClassLoader[] arr = { systemCL };
        final Set<ProxyClassLoader> parents = coalesceAppend(
            root, Collections.<ProxyClassLoader>emptySet(), loaders, arr
        );
        return new ProxyClassParents(arr[0], parents,transitive);
    }
    
    /** Coalesce a new set of loaders into the existing ones.
     */
    private static Set<ProxyClassLoader> coalesceAppend(
        ClassLoader root,
        Set<ProxyClassLoader> existing, ClassLoader[] appended, ClassLoader[] systemCL
    ) throws IllegalArgumentException {
        int likelySize = existing.size() + appended.length;
        
        LinkedHashSet<ClassLoader> uniq = new LinkedHashSet<ClassLoader>(likelySize);
        uniq.addAll(existing);

        if (uniq.containsAll(Arrays.asList(appended))) {
            return existing;
        } // No change required.

        for (ClassLoader l : appended) {
            addRec(root, uniq, l);
        } // add all loaders (maybe recursively)
        
        // validate the configuration
        // it is valid if all heading non-ProxyClassLoaders are parents of the last one
        boolean head = true;
        Set<ProxyClassLoader> pcls = new LinkedHashSet<ProxyClassLoader>(uniq.size());
        for (ClassLoader l : uniq) {
            if (head) {
                if (l instanceof ProxyClassLoader) {
                    // only PCLs after this point
                    head = false; 
                    pcls.add((ProxyClassLoader)l);
                } else {
                    if (isParentOf(systemCL[0], l)) {
                        systemCL[0] = l;
                    } else {
                        throw new IllegalArgumentException("Bad ClassLoader ordering: " + Arrays.asList(appended));
                    }
                }
            } else {
                if (l instanceof ProxyClassLoader) {
                    pcls.add((ProxyClassLoader)l);
                } else {
                        throw new IllegalArgumentException("Bad ClassLoader ordering: " + Arrays.asList(appended));
                    
                }
            }
        }
        return pcls;
    }

    private static void addRec(
        ClassLoader root, 
        Set<ClassLoader> resultingUnique, 
        ClassLoader loader
    ) throws IllegalArgumentException {
        if (loader == root) {
            throw new IllegalArgumentException("cycle in parents");// NOI18N
        } 
        if (resultingUnique.contains(loader)) {
            return;
        }
        if (loader instanceof ProxyClassLoader && ((ProxyClassLoader)loader).parents.transitive) {
            for (ProxyClassLoader lpar : ((ProxyClassLoader)loader).parents.loaders()) {
                addRec(root, resultingUnique, lpar);
            }
        }
        resultingUnique.add(loader);
    }

    boolean contains(ProxyClassLoader pcl) {
        return parentSet.contains(pcl);
    }

    Iterable<ProxyClassLoader> loaders() {
        boolean assertOn = false;
        assert assertOn = true;
        return assertOn ? Collections.unmodifiableSet(parentSet) :  parentSet;
    }
    
    int size() {
        return parentSet.size();
    }

    ProxyClassParents append(ClassLoader root, ClassLoader[] nueparents) {
        ClassLoader[] arr = { systemCL };
        Set<ProxyClassLoader> parents = coalesceAppend(root, parentSet, nueparents, arr);
        return new ProxyClassParents(arr[0], parents, transitive);
    }
    
    private static boolean isParentOf(ClassLoader parent, ClassLoader child) {
        while (child != null) {
            if (child == parent) {
                return true;
            }
            child = child.getParent();
        }
        return false;
    }

    ClassLoader systemCL() {
        return systemCL;
    }

    boolean isTransitive() {
        return transitive;
    }

    ProxyClassParents changeSystemClassLoader(ClassLoader s) {
        return new ProxyClassParents(s, parentSet, transitive);
    }
}
