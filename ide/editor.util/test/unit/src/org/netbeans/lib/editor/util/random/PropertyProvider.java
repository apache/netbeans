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

package org.netbeans.lib.editor.util.random;


/**
 * Provider of properties extended by {@link RandomTestContainer} and some of its subclasses.
 *
 * @author Miloslav Metelka
 */
public abstract class PropertyProvider {

    /**
     * Get value of property or return null.
     * @param key key.
     * @return value or null.
     */
    public abstract Object getPropertyOrNull(Object key);

    /**
     * Put new value of a property or instance value.
     * @param key key (or a class for instance values).
     * @param value new value.
     */
    public abstract void putProperty(Object key, Object value);

    /**
     * Get non-null value of property.
     * @param key key.
     * @return non-null value.
     * @throws IllegalStateException in case the property is null.
     */
    public final Object getProperty(Object key) {
        Object value = getPropertyOrNull(key);
        if (value == null) {
            throw new IllegalStateException("No value for property " + key); // NOI18N
        }
        return value;
    }

    /**
     * Get value of property or a default value.
     * @param key key.
     * @return value or default value if it would be null.
     */
    public final <V> V getProperty(Object key, V defaultValue) {
        @SuppressWarnings("unchecked")
        V value = (V) getPropertyOrNull(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Get instance value of property or null.
     * @param cls class which acts as key to {@link #getPropertyOrNull(Object)}.
     * @return instance value or null.
     */
    public final <C> C getInstanceOrNull(Class<C> cls) {
        @SuppressWarnings("unchecked")
        C instance = (C) getPropertyOrNull(cls);
        return instance;
    }

    /**
     * Get non-null instance value of property.
     * @param cls class which acts as key to {@link #getPropertyOrNull(Object)}.
     * @return non-null value.
     * @throws IllegalStateException in case the property is null.
     */
    public final <C> C getInstance(Class<C> cls) {
        C instance = getInstanceOrNull(cls);
        if (instance == null) {
            throw new IllegalStateException("No value for instance of class " + cls); // NOI18N
        }
        return instance;
    }

    public final boolean isLogOp() {
        return (Boolean.TRUE.equals(getPropertyOrNull(RandomTestContainer.LOG_OP)));
    }

}
