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

package org.netbeans.modules.cnd.utils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class Antiloop<T> {
    
    public static final int MAGIC_PLAIN_TYPE_RESOLVING_CONST = 4;
    
    
    private final Set<T> objects = new HashSet<T>();
    
    /**
     * Enters antiloop.
     * @param obj - obj to add to the set of objects
     * @return true if success, false otherwise
     */
    public boolean enter(T obj) {
        if (objects.contains(obj)) {
            return false;
        }
        objects.add(obj);
        return true;
    }
    
    /**
     * Exists antiloop
     * @param obj to remove from the set of objects
     */
    public void exit(T obj) {
        objects.remove(obj);
    }    
}
