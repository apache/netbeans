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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.spi.java.project.classpath;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;
import org.openide.filesystems.FileObject;

/**
 * Interface for project's compile classpath extension.
 * A project can provide this interface in its {@link org.netbeans.api.project.Project#getLookup lookup} to
 * allow clients to extend its compilation classpath
 * by a new classpath element (JAR, folder, dependent project, or library).
 * @since org.netbeans.modules.java.project/1 1.3
 * @deprecated As a caller, use {@link ProjectClassPathModifier} instead.
 *             As an implementor, use {@link ProjectClassPathModifier#extenderForModifier}.
 */
@Deprecated
public interface ProjectClassPathExtender {

    /**
     * Adds a library into the project's compile classpath if the
     * library is not already included.
     * @param library to be added
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata cannot be changed
     * @deprecated Please use {@link ProjectClassPathModifier#addLibraries} instead.
     */
    @Deprecated
    boolean addLibrary(Library library) throws IOException;

    /**
     * Adds an archive file or folder into the project's compile classpath if the
     * entry is not already there.
     * @param archiveFile ZIP/JAR file to be added
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata cannot be changed
     * @deprecated Please use {@link ProjectClassPathModifier#addRoots(URL[], FileObject, String)} instead.
     */
    @Deprecated
    boolean addArchiveFile(FileObject archiveFile) throws IOException;

    /**
     * Adds an artifact (e.g. subproject) into project's compile classpath if the
     * artifact is not already on it.
     * @param artifact to be added
     * @param artifactElement the URI of the build output
     *                        (must be owned by the artifact and be relative to it)
     * @return true in case the classpath was changed
     * @exception IOException in case the project metadata cannot be changed
     * @deprecated Please use {@link ProjectClassPathModifier#addAntArtifacts} instead.
     */
    @Deprecated
    boolean addAntArtifact(AntArtifact artifact, URI artifactElement) throws IOException;

}
