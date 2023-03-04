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
package org.netbeans.lib.v8debug.connection;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;

/**
 * An implementation is {@link JSONObject} that keeps the order of added elements.
 * 
 * @author Martin Entlicher
 */
public class LinkedJSONObject extends JSONObject {

    Map<Object, Object> linkedMap = new LinkedHashMap<>();

    public LinkedJSONObject() {
    }

    @Override
    public void clear() {
        linkedMap.clear();
    }

    @Override
    public Object clone() {
        LinkedJSONObject ljo = new LinkedJSONObject();
        ljo.linkedMap = new LinkedHashMap<>(linkedMap);
        return ljo;
    }

    @Override
    public boolean containsKey(Object key) {
        return linkedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return linkedMap.containsValue(value);
    }

    @Override
    public Set entrySet() {
        return linkedMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkedJSONObject)) {
            return false;
        }
        return ((LinkedJSONObject) o).linkedMap.equals(linkedMap);
    }

    @Override
    public Object get(Object key) {
        return linkedMap.get(key);
    }

    @Override
    public int hashCode() {
        return linkedMap.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return linkedMap.isEmpty();
    }

    @Override
    public Set keySet() {
        return linkedMap.keySet();
    }

    @Override
    public Object put(Object key, Object value) {
        return linkedMap.put(key, value);
    }

    @Override
    public void putAll(Map m) {
        linkedMap.putAll(m);
    }

    @Override
    public Object remove(Object key) {
        return linkedMap.remove(key);
    }

    @Override
    public int size() {
        return linkedMap.size();
    }

    @Override
    public Collection values() {
        return linkedMap.values();
    }
    
}
