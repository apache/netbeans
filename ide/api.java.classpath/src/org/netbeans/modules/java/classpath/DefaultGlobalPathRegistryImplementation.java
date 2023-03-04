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
package org.netbeans.modules.java.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.GlobalPathRegistryImplementation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
//@NotThreadSafe
@ServiceProvider(service = GlobalPathRegistryImplementation.class, position = 10_000)
public final class DefaultGlobalPathRegistryImplementation extends GlobalPathRegistryImplementation {

    private final Map<String,List<ClassPath>> paths = new HashMap<>();

    @Override
    @NonNull
    protected Set<ClassPath> getPaths(@NonNull final String id) {
        List<ClassPath> l = paths.get(id);
        if (l != null && !l.isEmpty()) {
            return Collections.unmodifiableSet(new HashSet<ClassPath>(l));
        } else {
            return Collections.<ClassPath>emptySet();
        }
    }

    @Override
    @NonNull
    protected Set<ClassPath> register(
            @NonNull final String id,
            @NonNull final ClassPath[] paths) {
        final Set<ClassPath> added = new HashSet<>();
        List<ClassPath> l = this.paths.get(id);
        if (l == null) {
            l = new ArrayList<>();
            this.paths.put(id, l);
        }
        for (ClassPath path : paths) {
            if (path == null) {
                throw new NullPointerException("Null path encountered in " + Arrays.asList(paths) + " of type " + id); // NOI18N
            }
            if (!added.contains(path) && !l.contains(path)) {
                added.add(path);
            }
            l.add(path);
        }
        return added;
    }

    @Override
    @NonNull
    protected Set<ClassPath> unregister(
            @NonNull final String id,
            @NonNull final ClassPath[] paths) throws IllegalArgumentException {
        final Set<ClassPath> removed = new HashSet<>();
        List<ClassPath> l = this.paths.get(id);
        if (l == null) {
            l = new ArrayList<>();
        }
        List<ClassPath> l2 = new ArrayList<>(l); // in case IAE thrown below
        for (ClassPath path : paths) {
            if (path == null) {
                throw new NullPointerException();
            }
            if (!l2.remove(path)) {
                throw new IllegalArgumentException("Attempt to remove nonexistent path [" + path +
                        "] from list of registered paths ["+l2+"] for id "+id+". All paths: "+this.paths); // NOI18N
            }
            if (!removed.contains(path) && !l2.contains(path)) {
                removed.add(path);
            }
        }
        this.paths.put(id, l2);
        return removed;
    }

    @Override
    @NonNull
    protected Set<ClassPath> clear() {
        final Set<ClassPath> removed = new HashSet<>();
        for (Iterator<Map.Entry<String,List<ClassPath>>> it = paths.entrySet().iterator();
            it.hasNext();) {
            final Map.Entry<String, List<ClassPath>> e = it.next();
            removed.addAll(e.getValue());
            it.remove();
        }
        assert paths.isEmpty();
        return removed;
    }
}
