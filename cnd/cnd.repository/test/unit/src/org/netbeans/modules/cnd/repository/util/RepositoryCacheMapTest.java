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
package org.netbeans.modules.cnd.repository.util;

import org.netbeans.modules.cnd.repository.disk.RepositoryCacheMap;
import org.netbeans.modules.cnd.test.CndBaseTestCase;

/**
 * test case for RepositoryCacheMap
 */
public class RepositoryCacheMapTest extends CndBaseTestCase {

    public RepositoryCacheMapTest(String testName) {
        super(testName);
    }

    public void testNoSuchElementException() {
        int capacity = 2;
        RepositoryCacheMap<String, String> map = new RepositoryCacheMap<String, String>(capacity);

        tryEqualKeys(map);
        tryEqualValues(map);

//        Filter<String> filter = new Filter<String>() {
//
//            @Override
//            public boolean accept(String value) {
//                return true;
//            }
//        };
//
//        map.remove(filter);
//
//        tryEqualKeys(map);
//        tryEqualValues(map);
//
//        map.remove(filter);
    }

    private void tryEqualKeys(RepositoryCacheMap<String, String> map) {
        String key = "key1";
        String value1 = "value1";
        String value2 = "value2";
        String value3 = "value3";
        String value4 = "value4";

        map.put(key, value1);
        map.put(key, value2);
        map.put(key, value3);
        map.put(key, value4);

        map.remove(key);

        map.put("a", "b");
    }

    private void tryEqualValues(RepositoryCacheMap<String, String> map) {
        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        String key4 = "key4";
        String val = "object";

        map.put(key1, val);
        map.put(key2, val);
        map.put(key3, val);
        map.put(key4, val);
        map.remove(key4);

        map.put("a", "b");
    }
}
