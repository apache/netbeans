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

package org.netbeans.spi.project.support.ant;

import java.io.IOException;
import org.netbeans.api.project.Project;

/**
 * Plugin for an Ant project type.
 * Register one instance to default lookup in order to define an Ant project type.
 * @author Jesse Glick
 */
public interface AntBasedProjectType {

    /**
     * Get a unique type identifier for this kind of project.
     * No two registered {@link AntBasedProjectType} instances may share the same type.
     * The type is stored in <code>nbproject/project.xml</code> in the <code>type</code> element.
     * It is forbidden for the result of this method to change from call to call.
     * @return the project type
     */
    String getType();
    
    /**
     * Create the project object with a support class.
     * Normally the project should retain a reference to the helper object in
     * order to implement various required methods.
     * Do <em>not</em> do any caching here; the infrastructure will call this
     * method only when the project needs to be loaded into memory.
     * @param helper a helper object encapsulating the generic project structure
     * @return a project implementation
     * @throws IOException if there is some problem loading additional data
     */
    Project createProject(AntProjectHelper helper) throws IOException;
    
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
