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
