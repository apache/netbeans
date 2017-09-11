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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.eclipse.core.Workspace.Variable;

/**
 * Able to load and fill up an <code>EclipseProject</code> from Eclipse project
 * directory using a .project and .classpath file and eventually passed
 * workspace. It is also able to load the basic information from workspace.
 *
 * @author mkrauskopf
 */
public final class ProjectFactory {
    
    /** Logger for this class. */
    private static final Logger logger = Logger.getLogger(ProjectFactory.class.getName());
    
    /** singleton */
    private static ProjectFactory instance = new ProjectFactory();
    
    private ProjectFactory() {/*empty constructor*/}
    
    /** Returns ProjectFactory instance. */
    public static ProjectFactory getInstance() {
        return instance;
    }
    
    /**
     * Loads a project contained in the given <code>projectDir</code> and tries
     * if there is workspace in the parent directory (which works only for
     * eclipse internal projects)
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    public EclipseProject load(File projectDir) throws
            ProjectImporterException {
        return load(projectDir, projectDir.getParentFile());
    }
    
    public EclipseProject load(File projectDir, File workspaceDir) throws
            ProjectImporterException {
        Workspace workspace = null;
        if (workspaceDir != null) {
            workspace = WorkspaceFactory.getInstance().load(workspaceDir);
        }
        return load(projectDir, workspace);
    }
    
    /**
     * Loads a project contained in the given <code>projectDir</code>.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    private EclipseProject load(File projectDir, Workspace workspace) throws
            ProjectImporterException {
        
        if (workspace != null) {
            EclipseProject project = workspace.getProjectByProjectDir(projectDir);
            if (project != null) {
                return project;
            }
        }
        EclipseProject project = EclipseProject.createProject(projectDir);
        if (project != null) {
            project.setWorkspace(workspace);
            loadDotProject(project);
            loadDotClassPath(project);
        }
        return project;
    }
    
    /**
     * Fullfill given <code>project</code> with information from .project file.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    void loadDotProject(EclipseProject project) throws ProjectImporterException {
        logger.finest("Loading .project for project: " + project.getDirectory().getAbsolutePath()); // NOI18N
        try {
            Set<String> natures = new HashSet<String>();
            List<Link> links = new ArrayList<Link>();
            Set<Variable> variables = new HashSet<Variable>();
            if (project.getWorkspace() != null) {
                variables = project.getWorkspace().getResourcesVariables();
            }
            String projName = ProjectParser.parse(project.getProjectFile(), natures, links, variables);
            project.setNatures(natures);
            project.setName(projName);
            project.setFacets(ProjectParser.readProjectFacets(project.getDirectory(), natures));
            project.setLinks(links);
        } catch (IOException ex) {
            throw new ProjectImporterException(ex);
        }
    }

    /**
     * Fullfill given <code>project</code> with information from .classpath file.
     * Should be called always after {@link #loadDotProject}.
     *
     * @throws ProjectImporterException if project in the given
     *     <code>projectDir</code> is not a valid Eclipse project.
     */
    void loadDotClassPath(EclipseProject project) throws ProjectImporterException {
        assert project.getNatures() != null; // is initialized by loadDotProject()
        logger.finest("Loading .classpath for project: " + project.getDirectory().getAbsolutePath()); // NOI18N
        try {
            DotClassPath dotClassPath;
            if (project.getClassPathFile() != null) {
                dotClassPath = DotClassPathParser.parse(project.getClassPathFile(), project.getLinks());
            } else {
                dotClassPath = DotClassPathParser.empty();
            }
            project.setClassPath(dotClassPath);
        } catch (IOException ex) {
            throw new ProjectImporterException(ex);
        }
    }
}


