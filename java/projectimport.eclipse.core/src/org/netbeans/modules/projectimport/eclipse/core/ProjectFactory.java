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


