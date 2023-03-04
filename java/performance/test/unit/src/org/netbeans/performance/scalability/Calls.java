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

package org.netbeans.performance.scalability;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openide.windows.TopComponent;

/**
 * Workaround for profiler incapabilities - #129336
 * 
 * @author Jaroslav Tulach
 */
class Calls implements Map<String,Object> {
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object get(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object put(String key, Object value) {
        if ("requestActive".equals(key)) {
            TopComponent tc = (TopComponent)value;
            tc.requestActive();
            return null;
        }
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Object> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
