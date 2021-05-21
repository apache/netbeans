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
package org.netbeans.modules.java.source.parsing;

import java.util.LinkedHashMap;
import org.netbeans.junit.NbTestCase;

public class ParameterNameProviderImplTest extends NbTestCase {
    
    public ParameterNameProviderImplTest(String name) {
        super(name);
    }

    public void testCapCache() {
        LinkedHashMap<String, ?> map = new LinkedHashMap<>();
        String key = "";
        for (int i = 0; i < 2 * ParameterNameProviderImpl.MAX_CACHE_SIZE; i++) {
            map.put(key, null);
            key += "x";
        }
        ParameterNameProviderImpl.capCache(map);
        
        assertEquals(ParameterNameProviderImpl.MAX_CACHE_SIZE, map.size());
    }    
}
