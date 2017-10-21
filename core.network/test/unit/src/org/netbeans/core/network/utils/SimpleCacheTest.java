/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }
        
        assertNotNull(cache.get("01"));

        // We've reached max capicity. Elements will start
        // to get evicted.

        cache.put("11", UUID.randomUUID().toString());
        assertTrue(cache.getCacheSize() == 10);
        
        
        assertNull(cache.get("02"));  // "02" will have been evicted by now

        cache.put("12", UUID.randomUUID().toString());
        assertTrue(cache.getCacheSize() == 10);
        
        assertNull(cache.get("03"));  // "03" will have been evicted by now
        assertNotNull(cache.get("01")); // "01" is still there.
        
    }

    
}
