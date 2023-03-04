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

package org.netbeans.modules.java.api.common.classpath;

import java.io.IOException;
import java.net.URI;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.ant.AntArtifact;

@Deprecated
@SuppressWarnings("deprecation")
public final class ClassPathExtender implements org.netbeans.spi.java.project.classpath.ProjectClassPathExtender {
    
    private final ClassPathModifier delegate;
    private final String elementName;
    private final String classPathProperty;

    public ClassPathExtender (final ClassPathModifier delegate, String classPathProperty, String elementName) {
        assert delegate != null;
        this.delegate = delegate;
        this.elementName = elementName;
        this.classPathProperty = classPathProperty;
    }
    
    public boolean addLibrary(final Library library) throws IOException {
        return addLibraries(classPathProperty, new Library[] { library }, elementName);
    }
    
    public boolean addLibraries(final String classPathId, final Library[] libraries, final String projectXMLElementName) throws IOException {
        return this.delegate.handleLibraries(libraries, classPathId, projectXMLElementName, ClassPathModifier.ADD);
    }

    public boolean addArchiveFile(final FileObject archiveFile) throws IOException {
        return addArchiveFiles(classPathProperty, new FileObject[] { archiveFile }, elementName);
    }

    public boolean addArchiveFiles(final String classPathId, FileObject[] archiveFiles, final String projectXMLElementName) throws IOException {
        for (int i = 0; i < archiveFiles.length; i++) {
            FileObject archiveFile = archiveFiles[i];
            if (FileUtil.isArchiveFile(archiveFile)) {
                archiveFiles[i] = FileUtil.getArchiveRoot(archiveFile);
            }           
        }
        URI[] archiveFileURIs = new URI[archiveFiles.length];
        for (int i = 0; i < archiveFiles.length; i++) {
            archiveFileURIs[i] = archiveFiles[i].toURI();
        }        
        return this.delegate.handleRoots(archiveFileURIs, classPathId, projectXMLElementName, ClassPathModifier.ADD);
    }
    
    // TODO: AB: AntArtifactItem should not be in LibrariesChooser
    
    public boolean addAntArtifact (AntArtifact artifact, URI artifactElement) throws IOException {
        return addAntArtifacts(classPathProperty, new AntArtifact[]{artifact}, new URI[]{artifactElement}, elementName);
    }

    public boolean addAntArtifacts(final String classPathId, final AntArtifact[] artifacts, URI[] artifactElements, final String projectXMLElementName) throws IOException {
        return this.delegate.handleAntArtifacts(artifacts, artifactElements, classPathId, projectXMLElementName, ClassPathModifier.ADD);
    }
    
}
