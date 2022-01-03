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

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;

/**
 *
 */
public class ResolverResultsCache {
    private static final Map<String, Map<APTIncludeResolverImpl, ResolvedPath>> cache = new HashMap<String, Map<APTIncludeResolverImpl, ResolvedPath>>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ResolverResultsCache(){
    }
    
    static ResolvedPath getResolvedPath(String include, APTIncludeResolverImpl resolver) {
        lock.readLock().lock();
        try {
            Map<APTIncludeResolverImpl, ResolvedPath> results = cache.get(include);
            if (results != null) {
                return results.get(resolver);
            }
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    static void putResolvedPath(String include, APTIncludeResolverImpl resolver, ResolvedPath resolvedPath) {
        lock.writeLock().lock();
        try {
            Map<APTIncludeResolverImpl, ResolvedPath> results = cache.get(include);
            if (results == null) {
                results = new WeakHashMap<APTIncludeResolverImpl, ResolvedPath>();
                cache.put(include, results);
            }
            results.put(resolver, resolvedPath);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public static void clearCache() {
        lock.writeLock().lock();
        try {
            cache.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
