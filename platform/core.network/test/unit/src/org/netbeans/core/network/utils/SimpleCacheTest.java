/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.utils;

import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lbruun
 */
public class SimpleCacheTest {
    
    private final SimpleObjCache<String,String> cache;
    public SimpleCacheTest() {
        cache = new SimpleObjCache<>(10);
    }
    
    

    @Test
    public void testCache() {
        System.out.println("testCache");
        
        cache.put("01", UUID.randomUUID().toString());
        cache.put("02", UUID.randomUUID().toString());
        cache.put("03", UUID.randomUUID().toString());
        cache.put("04", UUID.randomUUID().toString());
        cache.put("05", UUID.randomUUID().toString());
        cache.put("06", UUID.randomUUID().toString());
        cache.put("07", UUID.randomUUID().toString());
        cache.put("08", UUID.randomUUID().toString());
        cache.put("09", UUID.randomUUID().toString());
        cache.put("10", UUID.randomUUID().toString());
 
        shortWait();
        assertNotNull(cache.get("01"));   // reset last usage timestamp for this element

        // We've reached max capacity. Elements will start
        // to get evicted.

        shortWait();
        cache.put("11", UUID.randomUUID().toString());
        assertTrue(cache.getCacheSize() == 10);
        
        
        assertNull(cache.get("02"));  // "02" will have been evicted by now

        shortWait();
        cache.put("12", UUID.randomUUID().toString());
        assertTrue(cache.getCacheSize() == 10);
        
        assertNull(cache.get("03"));  // "03" will have been evicted by now
        assertNotNull(cache.get("01")); // "01" is still there.
        
    }

    @Test
    public void testCacheConcurrency() {
        System.out.println("testCache - concurrency ");
        
        // See if we can provoke ConcurrentModificationException.
        // That should not be the case, because of use of ConcurrentHashMap.
        
        // However, the use of ConcurrentHashMap is not immune to
        // NoSuchElementException being thrown from the Optional#get() in the
        // findEvictionCandidate() method.

        SimpleObjCache<Integer, String> intCache = new SimpleObjCache<>(3);    // very low capacity cache so that we get a lot of contention
        
        int noOfThreads = 15;
        CacheTesterThread[] threads = new CacheTesterThread[noOfThreads];
        // Prepare threads
        for(int i = 0; i<noOfThreads; i++) {
            threads[i] = new CacheTesterThread(intCache);
        }
        // Start threads
        for(int i = 0; i<noOfThreads; i++) {
            threads[i].start();
        }

    }
    
        
    
    private void shortWait() {
        // The cache only tracks usage of elements down to the millisecond. 
        // So, potentially, two elements can have exactly the same timestamp. 
        // For this reason, it is necessary to wait a bit in order to 
        // accurately predict which element will be evicted, thus only needed
        // if we need to predict which element will be evicted.
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
        }
    }
    
    
    private static class CacheTesterThread extends Thread {

        private final SimpleObjCache<Integer, String> cache;

        public CacheTesterThread(SimpleObjCache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                // Do a bit of both writing and reading
                cache.put(i % 10, "foo");
                String val = cache.get(i);
                cache.put((i % 10) + 1, val);
            }
        }
    }
}
