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
