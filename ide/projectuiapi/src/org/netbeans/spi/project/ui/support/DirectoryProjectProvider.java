/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.project.ui.support;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Provides a project for a directory which is about to become a group
 * in the projects view.
 *
 * <p>When the user creates a new "directory" project group, the group
 * machinery asks the default lookup for an implementation of this
 * service before falling back to the stock group, which passively lists
 * the projects found beneath the directory. An implementation may
 * <em>promote</em> the directory to a project of its own type — for
 * example by marking it in a per-user, non-intrusive way — and return
 * the canonical project instance; the group is then shown as the stock
 * subprojects group of that project, so nested projects are discovered
 * through the project's own lookup instead of a hard-coded directory
 * scan.
 *
 * <p>Implementations must be conservative:
 * <ul>
 * <li>a directory the implementation does not understand must be
 *     answered with {@code null}, never with an exception or a project
 *     of an unrelated type;</li>
 * <li>if the directory already is a project (of any type), the existing
 *     canonical project instance must be returned — never a new one.</li>
 * </ul>
 * @since 1.125
 */
public interface DirectoryProjectProvider {

    /**
     * Returns the project for the given directory, creating it if this
     * provider can make the directory into a project.
     * @param dir a directory (never {@code null})
     * @return the project for the directory — the existing project if
     *         the directory already is a project, a newly promoted one
     *         if this provider made it so — or {@code null} if the
     *         provider cannot handle the directory
     * @throws IOException if a project could not be determined
     */
    Project createProject(FileObject dir) throws IOException;

}

