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

package org.netbeans.modules.java.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.spi.java.project.classpath.ProjectClassPathModifierImplementation;
import org.openide.util.Exceptions;

/**
 *
 * @author tom
 */
public abstract class ProjectClassPathModifierAccessor {

    public static ProjectClassPathModifierAccessor INSTANCE;
    
    static {
        Class c = ProjectClassPathModifierImplementation.class;
        try {
            Class.forName (c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /** Creates a new instance of ProjectClassPathModifierAccessor */
    public ProjectClassPathModifierAccessor() {
    }

    public abstract SourceGroup[] getExtensibleSourceGroups (ProjectClassPathModifierImplementation m);
    
    public abstract String[] getExtensibleClassPathTypes (ProjectClassPathModifierImplementation m, SourceGroup sg);
    
    public abstract boolean addLibraries (Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
                
    public abstract boolean removeLibraries (Library[] libraries, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
        
    public abstract boolean addRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
       
    public abstract boolean addRoots (URI[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    public abstract boolean removeRoots (URL[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    public abstract boolean removeRoots (URI[] classPathRoots, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;
    
    public abstract boolean addAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;

    public abstract boolean removeAntArtifacts (AntArtifact[] artifacts, URI[] artifactElements, ProjectClassPathModifierImplementation m, SourceGroup sourceGroup, String type) throws IOException, UnsupportedOperationException;

    public abstract boolean addProjects(Project[] projects, ProjectClassPathModifierImplementation pcmi, SourceGroup sg, String classPathType) throws IOException, UnsupportedOperationException;

}
