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

package org.netbeans.modules.web.project.api;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.libraries.Library;

public interface WebProjectLibrariesModifier2 {
    
    /**
     * Adds libraries into the project. These libraries will not be added into the projects's classpath
     * but will be included in the created WAR file.
     * @param libraries to be added
     * @param path the libraries path in the WAR file
     * @return true in case the library was added (at least one library was added),
     * the value false is returned when all the libraries are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageLibraries(Library[] libraries, String path) throws IOException;

    /**
     * Removes libraries from the project. These libraries will be removed from the list of libraries which are expected
     * to be included in the created WAR file.
     * @param libraries to be removed
     * @param path the libraries path in the WAR file
     * @return true in case the library was removed (at least one library was removed),
     * the value false is returned when all the libraries were already removed.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removePackageLibraries(Library[] libraries, String path) throws IOException;

    /**
     * Adds artifacts (e.g. subprojects) into the project. These artifacts will not be added into the projects's classpath
     * but will be included in the created WAR file.
     * @param artifacts to be added
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param path the artifacts path in the WAR file
     * @return true in case the artifacts were added (at least one artifact was added),
     * the value false is returned when all the artifacts are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, String path) throws IOException;

    /**
     * Removes artifacts (e.g. subprojects) from the project. These artifacts will be removed from the list of artifacts which are expected
     * to be included in the created WAR file.
     * @param artifacts to be removed
     * @param artifactElements the URIs of the build output, the artifactElements has to have the same length
     * as artifacts.
     * (must be owned by the artifact and be relative to it)
     * @param path the artifacts path in the WAR file
     * @return true in case the artifacts were removed (at least one artifact was removed),
     * the value false is returned when all the artifacts were already removed.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removePackageAntArtifacts(AntArtifact[] artifacts, URI[] artifactElements, String path) throws IOException;

    /**
     * Adds archive files or folders into the project. These archive files or folders will not be added into
     * the projects's classpath but will be included in the created WAR file.
     * @param roots to be added
     * @param path the archive files or folders path in the WAR file
     * @return true in case the archive files or folders were added (at least one archive file or folder was added),
     * the value false is returned when all the archive files or folders are already included.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean addPackageRoots(URL[] roots, String path) throws IOException;

    /**
     * Removes archive files or folders from the project. These archive files or folders will be removed from the list
     * of archive files or folders which are expected to be included in the created WAR file.
     * @param roots to be removed
     * @param path the archive files or folders path in the WAR file
     * @return true in case the archive files or folders were removed (at least one archive file or folder was removed),
     * the value false is returned when all the archive files or folders were already removed.
     * @exception IOException in case the project metadata cannot be changed
     */
    public boolean removePackageRoots(URL[] roots, String path) throws IOException;

}
