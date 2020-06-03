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

package org.netbeans.modules.cnd.api.project;

/**
 * Native project type.
 * Register one instance to default lookup in order to define an Native project type.
 *
 */
public interface NativeProjectType {

    /**
     * Get a unique type identifier for this kind of project.
     * No two registered {@link NativeProjectType} instances may share the same type.
     * The type is stored in <code>nbproject/project.xml</code> in the <code>type</code> element.
     * It is forbidden for the result of this method to change from call to call.
     * @return the project type
     */
    String getType();
    
    /**
     * Get the simple name of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>) or <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * It is forbidden for the result of this method to change from call to call.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return a simple name; <samp>data</samp> is recommended but not required
     */
    String getPrimaryConfigurationDataElementName(boolean shared);
    
    /**
     * Get the namespace of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>) or <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * It is forbidden for the result of this method to change from call to call.
     * @param shared if true, refers to <code>project.xml</code>, else refers to
     *               <code>private.xml</code>
     * @return an XML namespace, e.g. <samp>http://www.netbeans.org/ns/j2se-project</samp>
     *         or <samp>http://www.netbeans.org/ns/j2se-project-private</samp>
     */
    String getPrimaryConfigurationDataElementNamespace(boolean shared);

}
