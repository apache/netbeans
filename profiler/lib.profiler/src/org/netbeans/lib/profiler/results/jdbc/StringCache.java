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
package org.netbeans.lib.profiler.results.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches Strings so that there is only one instance of particular string.
 * Functionality is similar to String.intern().
 * 
 * @author Tomas Hurka
 */
class StringCache {

    Map <String,String> cache;
    
    StringCache() {
        cache = new HashMap(2048);
    }
    
    String intern(String s) {
        String unique = cache.get(s);
        if (unique == null) {
            cache.put(s, s);
            return s;
        }
        return unique;
    }
}
