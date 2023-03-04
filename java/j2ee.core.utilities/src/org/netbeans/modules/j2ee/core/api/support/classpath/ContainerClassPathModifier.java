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

package org.netbeans.modules.j2ee.core.api.support.classpath;

import org.openide.filesystems.FileObject;



/**
 * Contract provided by some project types that don't put the entire application
 * server content on classpath (eg Maven) in the project's lookup.
 * Intended to be called by code that generates content into the project and assumes
 * certain j2ee api binaries to be on the project's classpath.
 *
 *<p>
 *
 * The usual workflow is to identify what APis the file template or framework (or anything else)
 * needs, then look up this interface from project's lookup. If present, then call the <code>extendClassPath()</code>
 * method with the list of symbolic names of the APIs in question.
 *
 * @author mkleint
 * @since org.netbeans.modules.j2ee.core.utilities 1.3
 */
public interface ContainerClassPathModifier {

    public final String API_SERVLET = "servlet-api"; //NOI18N
    public final String API_JSP = "jsp-api"; //NOI18N
    public final String API_JSF = "jsf-api"; //NOI18N
    /**
     * a fallback solution, adds the complete j2ee stack on classpath
     */
    public final String API_J2EE = "j2ee-api"; //NOI18N
    public final String API_PERSISTENCE = "persistence"; //NOI18N
    public final String API_ANNOTATION = "annotation"; //NOI18N
    public final String API_TRANSACTION = "transaction"; //NOI18N
    public final String API_EJB = "ejb"; //NOI18N


    /**
     * Extend the project's classpath with the application container's API required
     * by the functionality added to the project.
     * To be called outside of the Swing Event (AWT) thread as the processing can take a long time under some conditions.
     *
     * @param relevantFile a file from the project that was either created or is
     * from the project and is on correct classpath.
     * @param symbolicNames array of constant value for apis required. The list of supported
     * constants is included in this interface declaration.
     */
    void extendClasspath(FileObject relevantFile, String[] symbolicNames);
}
