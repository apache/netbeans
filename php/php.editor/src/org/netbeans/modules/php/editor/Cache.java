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
package org.netbeans.modules.php.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Cache<K, V> {

    // @GuardedBy(dataLock)
    private final Map<K, V> data;
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();

    public Cache() {
        this(new HashMap<K, V>());
    }

    public Cache(Map<K, V> data) {
        this.data = data;
    }

    public void save(K key, V value) {
        dataLock.writeLock().lock();
        try {
            data.put(key, value);
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    public V get(K key) {
        dataLock.readLock().lock();
        try {
            return data.get(key);
        } finally {
            dataLock.readLock().unlock();
        }
    }

    public boolean has(K token) {
        dataLock.readLock().lock();
        try {
            return data.containsKey(token);
        } finally {
            dataLock.readLock().unlock();
        }
    }

}