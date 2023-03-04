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

package org.netbeans.modules.groovy.grailsproject;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.project.ProjectFactory.class, position=661)
public final class GrailsProjectFactory implements ProjectFactory {

    public static final String GRAILS_APP_DIR = "grails-app"; // NOI18N
    public static final String APPLICATION_PROPERTIES = "application.properties"; // NOI18N
    public static final String GRADLE_PROPERTIES = "gradle.properties"; // NOI18N
    
    public GrailsProjectFactory() {
    }

    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject(GRAILS_APP_DIR) != null &&
                (   projectDirectory.getFileObject(APPLICATION_PROPERTIES) != null ||
                    projectDirectory.getFileObject(GRADLE_PROPERTIES) != null );
    }

    public Project loadProject(FileObject projectDirectory, ProjectState projectState) throws IOException {
        return isProject(projectDirectory) ? new GrailsProject(projectDirectory, projectState) : null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
