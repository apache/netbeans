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
package org.netbeans.modules.versioning.util;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.filesystems.FileObject;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Enumeration;
import java.util.Collections;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.versioning.util.projects.ProjectOpener;
import org.netbeans.spi.project.SubprojectProvider;

/**
 * Simpliied nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectUtilities.java,
 * nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectTab.java and
 * nb_all/ide/welcome/src/org/netbeans/modules/welcome/ui/TitlePanel.java copy.
 *
 * @author Petr Kuzel       
 */
public final class ProjectUtilities {

    private static final String ProjectTab_ID_LOGICAL = "projectTabLogical_tc"; // NOI18N
    public static final Logger LOG = Logger.getLogger(ProjectUtilities.class.getName());

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned checkout projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openExportedProjects(Map<Project, Set<Project>> checkedOutProjects, File workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.EXPORT, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned checkout projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openCheckedOutProjects(Map<Project, Set<Project>> checkedOutProjects, File workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.CHECKOUT, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned cloned projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openClonedOutProjects(Map<Project, Set<Project>> checkedOutProjects, File workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.CLONE, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    public static void selectAndExpandProject( final Project project ) {

        // invoke later to select the being opened project if the focus is outside ProjectTab
        SwingUtilities.invokeLater (new Runnable () {

            final TopComponent ptLogicalTC = findDefault(ProjectTab_ID_LOGICAL);
            final ExplorerManager.Provider ptLogicalExplorerManager = (ExplorerManager.Provider) ptLogicalTC;

            public void run () {
                if (ptLogicalExplorerManager == null) {
                    Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Cannot find Project widnow, aborting.");
                    return;
                }
                Node root = ptLogicalExplorerManager.getExplorerManager ().getRootContext ();
                for(Node projNode : root.getChildren().getNodes()) {
                    Project p = projNode.getLookup().lookup(Project.class);
                    if(p != null && p.getProjectDirectory().equals(project.getProjectDirectory())) {
                        try {
                            ptLogicalExplorerManager.getExplorerManager ().setSelectedNodes( new Node[] { projNode } );
                            ptLogicalTC.requestActive();
                        } catch (Exception ignore) {
                            // may ignore it
                        }    
                    }
                }                
            }
        });
    }

    /* Singleton accessor. As ProjectTab is persistent singleton this
     * accessor makes sure that ProjectTab is deserialized by window system.
     * Uses known unique TopComponent ID TC_ID = "projectTab_tc" to get ProjectTab instance
     * from window system. "projectTab_tc" is name of settings file defined in module layer.
     * For example ProjectTabAction uses this method to create instance if necessary.
     */
    private static synchronized TopComponent findDefault( String tcID ) {
        TopComponent tc = WindowManager.getDefault().findTopComponent( tcID );
        return tc;
    }

    /**
     * Runs <i>New Project...</i> wizard with redefined defaults:
     * <ul>
     * <li>default project directory to working folder to
     * capture creating new project in placeholder
     * directory prepared by CVS server admin
     * <li>CommonProjectActions.EXISTING_SOURCES_FOLDER
     * pointing to working folder to capture
     * typical <i>... from Existing Sources</i> panel
     * <i>Add</i> button behaviour.
     * </ul>
     * @param workingDirectory
     */
    public static void newProjectWizard(File workingDirectory) {
        Action action = CommonProjectActions.newProjectAction();
        if (action != null) {
            File original = ProjectChooser.getProjectsFolder();
            ProjectChooser.setProjectsFolder(workingDirectory);
            FileObject workingFolder = FileUtil.toFileObject(workingDirectory);
            action.putValue(CommonProjectActions.EXISTING_SOURCES_FOLDER, workingFolder);
            performAction(action);
        }
    }

    /**
     * Scans given folder (and subfolder into deep 5) for projects.
     * @param scanRoot root folder to scan
     * @param foundProjects found projects sorted by its parents, root folders are under <code>null</code> key.
     * <strong>The key for root folder must be present and its value must be initialized.</strong>
     */
    public static void scanForProjects(FileObject scanRoot, Map<Project, Set<Project>> foundProjects) {
        ProjectManager.getDefault().clearNonProjectCache();
        assert foundProjects.get(null) != null;
        scanForProjectsRecursively(scanRoot, foundProjects, null, 5);
    }

    private static void scanForProjectsRecursively(FileObject scanRoot, Map<Project, Set<Project>> projects, Project parentProject, int deep) {
        if (deep <= 0) return;
        ProjectManager projectManager = ProjectManager.getDefault();
        if (scanRoot.isFolder() && projectManager.isProject(scanRoot)) {
            try {
                Project prj = projectManager.findProject(scanRoot);
                if(prj != null) {
                    Set<Project> siblings = projects.get(parentProject);
                    assert siblings != null;
                    parentProject = prj; // set parent for recursion
                    siblings.add(prj);
                    projects.put(prj, new HashSet<Project>()); // insert project with no children so far
                }
            } catch (IOException e) {
                // it happens for all apisupport projects unless
                // checked out into directory that contains nbbuild and openide folders
                // apisupport project is valid only if placed in defined directory structure
                LOG.log(Level.INFO, " ignoring suspicious project folder...", e);    // NOI18N
            }
        }
        Enumeration en = scanRoot.getChildren(false);
        while (en.hasMoreElements()) {
            FileObject fo = (FileObject) en.nextElement();
            if (fo.isFolder()) {
                scanForProjectsRecursively(fo, projects, parentProject, deep - 1);  // RECURSION
            }
        }
    }

    private static boolean performAction (Action a) {
        if (a == null) {
            return false;
        }
        ActionEvent ae = new ActionEvent(ProjectUtilities.class, ActionEvent.ACTION_PERFORMED, "command");  // NOI18N
        try {
            a.actionPerformed(ae);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
            return false;
        }
    }

    private ProjectUtilities() {
    }

    /**
     * Returns direct subprojects
     * @param p parent project
     * @return collection of direct subprojects
     */
    public static Set<? extends Project> getSubProjects (Project p) {
        Set<? extends Project> subprojects = null;
        SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
        if (spp != null) {
            subprojects = spp.getSubprojects();
        } else {
            subprojects = Collections.emptySet();
        }
        return subprojects;
    }

    /**
     * Gets all subprojects recursively
     * @param p project to scan
     * @param result all subprojects
     * @param cache temporary cache of projects
     */
    public static void addSubprojects(Project p, Set<Project> result, Map<Project, Set<? extends Project>> cache) {
        // original in ProjectUI API
        Set<? extends Project> subprojects = cache.get(p);
        if (subprojects == null) {
            // p's subprojects have not yet been searched
            SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
            if (spp != null) {
                subprojects = spp.getSubprojects();
            } else {
                subprojects = Collections.emptySet();
            }
            cache.put(p, subprojects);
            for (Project sp : subprojects) {
                result.add(sp);
                addSubprojects(sp, result, cache);
            }
        }
    }

    /**
     * Sorts the initial list of projects into sortedProjects with keys as their parent projects
     * @param rootProjects initial projects. All projects but the root ones will be removed.
     * @param sortedProjects sorted projects
     */
    public static void sortProjectsByParents (List<Project> rootProjects, Map<Project, Set<Project>> sortedProjects) {
        ArrayList<Project> sortedList = new ArrayList<Project>(rootProjects.size());
        sortedList.sort(new Comparator<Project>() {
            public int compare(Project o1, Project o2) {
                String p1 = o1.getProjectDirectory().getPath();
                String p2 = o2.getProjectDirectory().getPath();
                return p1.compareTo(p2);
            }
        });

        
    }
}
