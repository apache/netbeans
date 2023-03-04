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
package org.netbeans.modules.gradle.loaders;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.gradle.tooling.model.Model;
import org.openide.util.Pair;

/**
 *
 * @author lkishalmi
 */
public class ModelCacheManager {

    static Map<File, List<Pair<Class, ModelCache>>> CACHES = new ConcurrentHashMap<>();

    public static <T extends Model> ModelCache<T> getModelCache(File rootDir, Class<T> clazz, Supplier<ModelCache<T>> supplier) {
        List<Pair<Class, ModelCache>> caches = CACHES.get(rootDir);
        if (caches == null) {
            caches = new LinkedList<>();
            CACHES.put(rootDir, caches);
        }
        ModelCache<T> c = null;
        for (Pair<Class, ModelCache> cache : caches) {
            if (cache.first().isAssignableFrom(clazz)) {
                c = cache.second();
            }
        }
        if (c == null) {
            c = supplier.get();
            caches.add(Pair.of(clazz, c));
        }
        return c;
    }
}
