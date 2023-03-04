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

import org.netbeans.api.project.ProjectUtils;
import org.w3c.dom.Element;

/**
 * Ability for a project to permit other modules to insert arbitrary metadata
 * into the project storage area.
 * <p class="nonnormative">
 * For example, the debugger may wish to store a list of breakpoints in the
 * project private settings area without relying on the exact structure of
 * the project. Similarly, the editor may wish to keep a parser database
 * associated with a project without direct support from the project type.
 * </p>
 * <p>
 * A module is only permitted to read and write its own metadata fragments
 * unless it is explicitly given permission to read and/or write other fragments
 * owned by another module. XML namespaces should be used to scope the data
 * to avoid accidental clashes.
 * </p>
 * <p>
 * <strong>Do not look for this object directly in project lookup.</strong>
 * Instead use {@link ProjectUtils#getAuxiliaryConfiguration}.
 * </p>
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface AuxiliaryConfiguration {
    
    /**
     * Retrieve a custom fragment of the project's unstructured configuration data
     * as a portion of a DOM tree.
     * This fragment should not have a parent node, to prevent unauthorized access
     * to other data; it may be modified by the caller, but {@link #putConfigurationFragment}
     * is required to insert any changes back into the project settings.
     * @param elementName the simple name of the element expected
     * @param namespace an XML namespace that <code>elementName</code> is qualified with
     *                  (may not be empty)
     * @param shared true to look in a sharable settings area, false to look in a private
     *               settings area
     * @return a configuration fragment, or null if none such was found
     */
    Element getConfigurationFragment(String elementName, String namespace, boolean shared);
    
    /**
     * Insert a custom fragment into the project's unstructured configuration data
     * as a portion of a DOM tree.
     * <p>
     * This fragment may have a parent node, but the implementor should ignore that,
     * and clone the fragment so as to be insulated from any further modifications.
     * <p>
     * If a fragment with the same name already exists, it is overwritten with the
     * new fragment.
     * <p>Implementations ought to acquires write access from
     * {@link org.netbeans.api.project.ProjectManager#mutex}.
     * However, from client code you are well advised to explicitly enclose a
     * <em>complete</em> operation within write access, starting with
     * {@link #getConfigurationFragment}, to prevent race conditions.
     * @param fragment a DOM tree fragment; the root element must have a defined namespace
     * @param shared true to save in a sharable settings area, false to save in a private
     *               settings area
     * @throws IllegalArgumentException if the fragment does not have a namespace or the element name
     *                                  and namespace is already reserved by the project type for its
     *                                  own purposes
     */
    void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException;
    
    /**
     * Remove a custom fragment from the project's unstructured configuration data
     * as a portion of a DOM tree.
     * @param elementName the simple name of the element which should be removed
     * @param namespace an XML namespace that <code>elementName</code> is qualified with
     *                  (may not be empty)
     * @param shared true to save in a sharable settings area, false to save in a private
     *               settings area
     * @return true if the requested fragment was actually removed, false if not
     * @throws IllegalArgumentException if the element name and namespace is already reserved
     *                                  by the project type for its own purposes
     */
    boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException;
    
}
