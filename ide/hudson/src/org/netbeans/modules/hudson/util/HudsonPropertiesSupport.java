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

package org.netbeans.modules.hudson.util;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Hudson properties support
 *
 * @author Michal Mocnak
 */
public final class HudsonPropertiesSupport {
    
    private Map<String, Object> properties = new HashMap<String, Object>();
    
    public HudsonPropertiesSupport() {}
    
    public @CheckForNull <T> T getProperty(String name, Class<T> clazz) {
        Object o = properties.get(name);
        return clazz.isInstance(o) ? clazz.cast(o) : null;
    }
        
    public @NonNull <T> T getProperty(String name, Class<T> clazz, @NonNull T fallback) {
        T t = getProperty(name, clazz);
        return t != null ? t : fallback;
    }
    
    public void putProperty(String name, Object o) {
        properties.put(name, o);
    }
}
