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
package org.netbeans.modules.maven.embedder.impl;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.RepositoryCache;

/**
 *
 * @author mkleint
 */
public class NbRepositoryCache implements RepositoryCache {
    
    private static final Object LOCK = new Object();
    
    //org.eclipse.aether.internal.impl.ObjectPool instances, containing a weakhashmap with weak value references.
    //to be considered harmless..
    private static Object artifacts;
    private static Object dependencies;
    
    //as in DataPool class..
    //TODO mkleint: the constants have wrong values, the caching is not taking effect,
    //interestingly both the Dependency and Artifact instances are bigger in live IDE when correct value is used..
    
    private static final String ARTIFACT_POOL = "org.eclipse.org.eclipse.aether.DataPool$Artifact";
    private static final String DEPENDENCY_POOL = "org.eclipse.org.eclipse.aether.DataPool$Dependency";    

    private final DefaultRepositoryCache superDelegate;
    public NbRepositoryCache() {
        superDelegate = new DefaultRepositoryCache();
    }
    
    @Override
    public Object get(RepositorySystemSession session, Object key) {
        if (ARTIFACT_POOL.equals(key)) {
            synchronized (LOCK) {
                return artifacts;
            }
        }
        if (DEPENDENCY_POOL.equals(key)) {
            synchronized (LOCK) {
                return dependencies;
            }
        }
        return superDelegate.get(session, key);
    }

    @Override
    public void put(RepositorySystemSession session, Object key, Object data) {
        //we just let the pools to get overriden to new value, the worst that can happen is that
        //2 pools will coexist
        if (ARTIFACT_POOL.equals(key)) {
            synchronized (LOCK) {
                artifacts = data;
            }
            return;
        }
        if (DEPENDENCY_POOL.equals(key)) {
            synchronized (LOCK) {
                dependencies = data;
            }
            return;
        }
        superDelegate.put(session, key, data);
    }
    
}
