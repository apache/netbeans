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

package org.netbeans.spi.java.project.support;

import org.netbeans.api.project.Project;
import org.netbeans.modules.java.project.ExtraProjectSourceForBinaryQueryImpl;
import org.netbeans.modules.java.project.ExtraProjectJavadocForBinaryQueryImpl;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Utility factory class for ant-based project implementors, the created instances, once
 * put into project's lookup, make sure the javadoc and source references in project properties
 * are correctly recognized and used by the java infrastructure.
 * The format of project properties used is as follows:
 * <ul>
 *      <li>file.reference.__jarName__ - the binary file reference used to point to files on project classpath. </li>
 *      <li>javadoc.reference.__jarName__ - the file reference pointing to javadoc for file.reference.__jarName__ </li>
 *      <li>source.reference.__jarName__ - the file reference pointing to sources for file.reference.__jarName__ </li>
 * </ul>
 * @author mkleint
 * @since org.netbeans.modules.java.project 1.14
 */
public class ExtraSourceJavadocSupport {

    /**
     * Create project's SourceForBinaryQueryImplementation object for handling 
     * property based sources
     * @param helper project's AntProjectHelper instance
     * @param eval project's PropertyEvaluator instance
     * @return object to use in project's lookup.
     */
    public static SourceForBinaryQueryImplementation createExtraSourceQueryImplementation (Project project, AntProjectHelper helper, PropertyEvaluator eval) {
        return new ExtraProjectSourceForBinaryQueryImpl(project, helper, eval);
    }
    
    /**
     * Create project's JavadocForBinaryQueryImplementation object for handling 
     * property based javadoc
     * @param helper project's AntProjectHelper instance
     * @param eval project's PropertyEvaluator instance
     * @return object to use in project's lookup.
     */
    public static JavadocForBinaryQueryImplementation createExtraJavadocQueryImplementation (Project project, AntProjectHelper helper, PropertyEvaluator eval) {
        return new ExtraProjectJavadocForBinaryQueryImpl(project, helper, eval);
    }


}
