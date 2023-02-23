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
package org.netbeans.lib.v8debug;

/**
 * An optional long property.
 * Use {@link #hasValue()} to test whether the property has the value or not.
 * 
 * @author Martin Entlicher
 */
public final class PropertyLong {
    
    private final Long l;
    
    /**
     * Create the long property.
     * @param l when <code>null</code>, then the property is undefined.
     */
    public PropertyLong(Long l) {
        this.l = l;
    }
    
    /**
     * Test whether the property has a value.
     * @return whether the property has a value.
     */
    public boolean hasValue() {
        return l != null;
    }
    
    /**
     * Get the property value. If the property does not have the value set,
     * it returns <code>0</code>.
     * @return the property value, or <code>0</code> when not set.
     */
    public long getValue() {
        if (l == null) {
            return 0;
        } else {
            return l;
        }
    }
    
    /**
     * Get the property value or the provided value when the property does not have one.
     * @param defaultValue The default value to return when the property is undefined.
     * @return the property value, or defaultValue when not set
     */
    public long getValueOr(long defaultValue) {
        if (l == null) {
            return defaultValue;
        } else {
            return l;
        }
    }
    
}
