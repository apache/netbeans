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
package org.netbeans.modules.projectapi;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

public class LazyLookupTest {
    
    public LazyLookupTest() {
    }
    
    @Test
    public void testChangesAreNotifiedWithoutHoldingALock() {
        
        Map<String,Object> data = new HashMap<>();
        data.put("service", String.class.getName());
        data.put("class", LazyLookupTest.class.getName());
        data.put("method", "testFactory");
        LazyLookup lkp = new LazyLookup(data, Lookup.EMPTY);

        class LL implements LookupListener {
            int cnt;
            
            @Override
            public void resultChanged(LookupEvent ev) {
                cnt++;
                assertFalse("Internal lock isn't held", lkp.isInitializing());
            }
        }
        LL listener = new LL();

        Lookup.Result<String> res = lkp.lookupResult(String.class);
        res.addLookupListener(listener);
        
        String value = lkp.lookup(String.class);
        assertEquals("Hello", value);
        
        assertEquals("One change", 1, listener.cnt);
    }
    
    public static String testFactory() {
        return "Hello";
    }
}
