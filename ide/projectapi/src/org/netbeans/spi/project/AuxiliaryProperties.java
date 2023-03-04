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

package org.netbeans.spi.project;

/**<p>Allow to store arbitrary properties in the project, similarly as {@link AuxiliaryConfiguration}.
 * Used as backing store for {@link org.netbeans.api.project.ProjectUtils#getPreferences(org.netbeans.api.project.Project, java.lang.Class, boolean)}.
 * </p>
 * 
 * <p>Note to API clients: do not use this interface directly, use
 * {@link org.netbeans.api.project.ProjectUtils#getPreferences(org.netbeans.api.project.Project, java.lang.Class, boolean)} instead.
 * </p>
 * 
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jan Lahoda
 * @since 1.16
 */
public interface AuxiliaryProperties {

    /**
     * Get a property value.
     * 
     * @param key name of the property
     * @param shared true to look in a sharable settings area, false to look in a private
     *               settings area
     * @return value of the selected property, or null if not set.
     */
    public String get(String key, boolean shared);
    
    /**
     * Put a property value.
     * 
     * @param key name of the property
     * @param value value of the property. <code>null</code> will remove the property.
     * @param shared true to look in a sharable settings area, false to look in a private
     *               settings area
     */
    public void put(String key, String value, boolean shared);
    
    /**
     * List all keys of all known properties.
     * 
     * @param shared true to look in a sharable settings area, false to look in a private
     *               settings area
     * @return known keys.
     */
    public Iterable<String> listKeys(boolean shared);
    
}
