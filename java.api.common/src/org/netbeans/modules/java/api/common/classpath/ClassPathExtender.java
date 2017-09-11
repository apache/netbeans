/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
