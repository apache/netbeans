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

package org.netbeans.api.server.properties;

/**
 * The set of properties to persist. Every property set is persisted unless
 * the whole object is not removed by the {@link #remove()} call.
 * <p>
 * In the scope of namespace used in {@link InstancePropertiesManager}
 * the object has assigned unique id identifying it.
 *
 * @author Petr Hejl
 */
public abstract class InstanceProperties {

    private final String id;

    /**
     * Creates the new InstanceProperties.
     *
     * @param id id of the properties, unique in the scope of the namespace
     * @see InstancePropertiesManager
     */
    public InstanceProperties(String id) {
        this.id = id;
    }

    /**
     * Returns unique id of these properties. It is guaranteed that this id is
     * unique in the scope of single namespace used in manager (however it
     * is not related directly to it).
     * <p>
     * Client may use it for its own purposes (don't have to), but client
     * can't influence the actual value of id in any way.
     *
     * @return id of the properties unique in the scope of the property set
     * @see InstancePropertiesManager
     * @see InstancePropertiesManager#createProperties(String)
     */
    public final String getId() {
        return id;
    }

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property default value is returned. This method is designed to be
     * used in conjuction with {@link #putString(String, String)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract String getString(String key, String def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getString(String, String)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putString(String key, String value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid integer the default value is returned.
     * Valid stored values are "true" and "false" (case insensitive). This
     * method is designed to be used in conjuction with
     * {@link #putBoolean(String, boolean)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract boolean getBoolean(String key, boolean def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getBoolean(String, boolean)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putBoolean(String key, boolean value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid integer the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Integer#parseInt(String)}. However this method
     * is designed to be used in conjuction with {@link #putInt(String, int)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract int getInt(String key, int def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getInt(String, int)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putInt(String key, int value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid long the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Long#parseLong(String)}. However this method
     * is designed to be used in conjuction with {@link #putLong(String, long)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract long getLong(String key, long def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getLong(String, long)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putLong(String key, long value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid float the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Float#parseFloat(String)}. However this method
     * is designed to be used in conjuction with
     * {@link #putFloat(String, float)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract float getFloat(String key, float def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getFloat(String, float)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putFloat(String key, float value);

    /**
     * Returns the value of the given property. If the value was not assigned
     * to the property or it is not valid double the default value is returned.
     * Valid string values associated with the property are values parseable
     * with {@link java.lang.Double#parseDouble(String)}. However this method
     * is designed to be used in conjuction with
     * {@link #putDouble(String, double)}.
     *
     * @param key name of the property
     * @param def default value
     * @return the value of the property or defined default value
     */
    public abstract double getDouble(String key, double def);

    /**
     * Associates the specified value with the specified property. This
     * method is expected to be used in conjuction with
     * {@link #getDouble(String, double)}.
     *
     * @param key name of the property
     * @param value value to set
     */
    public abstract void putDouble(String key, double value);

    /**
     * Removes the value of the given property, if any.
     *
     * @param key name of the property
     */
    public abstract void removeKey(String key);

    /**
     * Removes this instance from the persistent space. All values of
     * previously set are lost. The result of call to
     * {@link InstancePropertiesManager#getProperties(String)} with appropriate
     * parameter will not contain this set of properties anymore.
     * <p>
     * Return value of any method after removal is not defined and most
     * likely will lead to {@link java.lang.IllegalStateException}.
     */
    public abstract void remove();

}
