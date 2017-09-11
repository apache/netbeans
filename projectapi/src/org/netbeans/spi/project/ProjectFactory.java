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

package org.netbeans.spi.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;

/**
 * Create in-memory projects from disk directories.
 * Instances should be registered into default lookup.
 * @author Jesse Glick
 */
public interface ProjectFactory {

    /**
     * Test whether a given directory probably refers to a project recognized by this factory
     * without actually trying to create it.
     * <p>Should be as fast as possible as it might be called sequentially on a
     * lot of directories.</p>
     * <p>Need not be definite; it is permitted to return null or throw an exception
     * from {@link #loadProject} even when returning <code>true</code> from this
     * method, in case the directory looked like a project directory but in fact
     * had something wrong with it.</p>
     * <p>Will be called inside read access by {@link ProjectManager#isProject}
     * or {@link ProjectManager#isProject2}.</p>
     * @param projectDirectory a directory which might refer to a project
     * @return true if this factory recognizes it
     */
    boolean isProject(FileObject projectDirectory);
    
    /**
     * Create a project that resides on disk.
     * If this factory does not
     * in fact recognize the directory, it should just return null.
     * <p>Will be called inside read access by {@link ProjectManager#findProject}.
     * <p>Do not do your own caching! The project manager caches projects for you, properly.
     * <p>Do not attempt to recognize subdirectories of your project directory (just return null),
     * unless they are distinct nested projects.
     * @param projectDirectory some directory on disk
     * @param state a callback permitting the project to indicate when it is modified
     * @return a matching project implementation, or null if this factory does not recognize it
     */
    Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException;

    /**
     * Save a project to disk.
     * <p>Will be called inside write access, by {@link ProjectManager#saveProject}
     * or {@link ProjectManager#saveAllProjects}.
     * @param project a project created with this factory's {@link #loadProject} method
     * @throws IOException if there is a problem saving
     * @throws ClassCastException if this factory did not create this project
     */
    void saveProject(Project project) throws IOException, ClassCastException;
    
}
