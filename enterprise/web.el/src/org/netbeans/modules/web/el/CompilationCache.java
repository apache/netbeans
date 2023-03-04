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
package org.netbeans.modules.web.el;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author marekfukala
 */
public class CompilationCache {

    private Map<Key, Object> map = new HashMap<>();
    
    public synchronized Object getOrCache(Key key, ValueProvider<?> valueProvider) {
        Object cached = map.get(key);
        if(cached == null) {
            cached = valueProvider.get();
            map.put(key, cached);
        }
        return cached;
    }
    
    public static Key createKey(Object... items) {
        return new Key(items);
    }
    
    public static interface ValueProvider<T> {
        public T get();
    }
    
    public static class Key {

        private Object[] keys;
        
        private Key(Object... keys) {
            this.keys = keys;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;

            if (other.keys.length != keys.length) {
                return false;
            }

            for (int i = 0; i < keys.length; i++) {
                if (!keys[i].equals(other.keys[i])) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            for (Object key : keys) {
                hash = 13 * key.hashCode();
            }
            return hash;
        }
    }
}
