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

package org.netbeans.modules.parsing.lucene;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class LRUCacheTest extends NbTestCase {

    public LRUCacheTest(final String name) {
        super(name);
    }

    private Set<Integer> used = new HashSet<>();

    @Test
    public void testLRU() {
        final LRUCache<Integer,Evictable> ev = new LRUCache<>(new TestEvictionPolicy());
        final Set<Integer> golden = new HashSet<>();
        for (int i=0; i<10; i++) {
            used.add(i);
            ev.put(i, new EvictableInt(i));
        }
        for (int i=0; i<5; i++) {
            used.add(i);
            golden.add(i);
            ev.put(i, new EvictableInt(i));
        }
        for (int i=10; i<15; i++) {
            used.add(i);
            golden.add(i);
            ev.put(i, new EvictableInt(i));
        }
        assertEquals(golden, used);
    }

    public void testClear() {
        final LRUCache<Integer,Evictable> ev = new LRUCache<>(new TestEvictionPolicy());
        for (int i=0; i<10; i++) {
            ev.put(i, new EvictableInt(i));
        }
        Collection<? extends Evictable> removed = ev.clear();
        assertEquals(10, removed.size());
        removed = ev.clear();
        assertEquals(0, removed.size());
    }

    private static class TestEvictionPolicy implements EvictionPolicy<Integer,Evictable> {
        @Override
        public boolean shouldEvict(int size, Integer key, Evictable value) {
            return size > 10;
        }
    }

    private class EvictableInt implements Evictable {

        private final Integer value;
        
        public EvictableInt(final int i) {
            this.value = i;
        }

        @Override
        public void evicted() {            
            used.remove(value);
        }
    }

}
