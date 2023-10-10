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

package org.netbeans.spi.project.support.ant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.project.Project;

/** Registers a {@link Project} implementation as an
 * {@link org.netbeans.spi.project.support.ant.AntBasedProjectType} extension.
 * Just write a project with constructor that takes one
 * {@link AntProjectHelper} argument and annotate the class with this annotation.
 * As an alternative you can annotate a factory method with the same parameter.
 *
 * @author Jaroslav Tulach
 * @since 1.30
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface AntBasedProjectRegistration {
    /**
     * icon of the project type represented by the given implementation of the interface.
     * @return the {@link ClassLoader#getResource(java.lang.String)} path to the icon
     */
    public String iconResource();


    /**
     * Get a unique type identifier for this kind of project.
     * No two registered {@link AntBasedProjectType} instances may share the same type.
     * The type is stored in <code>nbproject/project.xml</code> in the <code>type</code> element.
     * It is forbidden for the result of this method to change from call to call.
     * @return the project type
     */
    String type();

    /**
     * Get the simple name of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>).
     * 
     * @return a simple name; <em>data</em> is recommended but not required
     */
    String sharedName() default "data";

    /**
     * Get the namespace of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/project.xml</code>
     * (inside <code>&lt;configuration&gt;</code>).
     * 
     * @return an XML namespace, e.g. <em>http://www.netbeans.org/ns/j2se-project</em>
     *         or <em>http://www.netbeans.org/ns/j2se-project-private</em>
     */
    String sharedNamespace();

    /**
     * Get the simple name of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * 
     * @return a simple name; <em>data</em> is recommended but not required
     */
    String privateName() default "data";

    /**
     * Get the namespace of the XML element that should be used to store
     * the project's specific configuration data in <code>nbproject/private/private.xml</code>
     * (inside <code>&lt;project-private&gt;</code>).
     * 
     * @return an XML namespace, e.g. <em>http://www.netbeans.org/ns/j2se-project</em>
     *         or <em>http://www.netbeans.org/ns/j2se-project-private</em>
     */
    String privateNamespace();
}
