/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.projectimport.eclipse.core.wizard.ProjectSelectionPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 */
public class UpdateAllProjects {
    
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(UpdateAllProjects.class.getName());
    
    private List<UpgradableProject> getListOfUpdatableProjects() {
        List<UpgradableProject> projs = new ArrayList<UpgradableProject>();
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            UpgradableProject up = p.getLookup().lookup(UpgradableProject.class);
            if (up != null && up.isCreatedFromEclipse()) {
                projs.add(up);
            }
        }
        return projs;
    }
    
    private boolean ensureProjectsReachable(List<UpgradableProject> ups, boolean silent) {
        Map<String,String> resolvedEntries = new HashMap<String, String>();
        for (UpgradableProject up : ups) {
            if (!up.isEclipseProjectReachable()) {
                if (silent) {
                    LOG.info("eclipse link is broken for project: "+up.getProject().getProjectDirectory()); //NOI18N
                    return false;
                }
                if (!up.updateBrokenEclipseReference(resolvedEntries)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    static class ProjectsAndDestination {
        private Set<EclipseProject> eps;
        private File dest;

        public ProjectsAndDestination() {
            this.eps = new HashSet<EclipseProject>();
        }

        public File getDestination() {
            return dest;
        }

        public Set<EclipseProject> getEclipseProjects() {
            return eps;
        }
    }

    /**
     * 
     * @return null for abort; TRUE for new projects otherwise FALSE
     */
    private Boolean resolveNewRequiredProjects(List<UpgradableProject> ups, boolean silent, 
            List<String> importProblems, List<Project> createdProjects) throws IOException {
        Map<File, ProjectsAndDestination> workspaceProjectsMap = new HashMap<File, ProjectsAndDestination>();
        Set<File> workspaces = new HashSet<File>();
        for (UpgradableProject up : ups) {
            if (up.getWorkspace() != null) {
                workspaces.add(up.getWorkspace().getDirectory());
            }
        }
        for (File workspace : workspaces) {
            ProjectsAndDestination projAndDest = new ProjectsAndDestination();
            for (UpgradableProject up : ups) {
                if (up.getWorkspace() == null || !up.getWorkspace().getDirectory().equals(workspace)) {
                    continue;
                }
                EclipseProject ep = up.getEclipseProject();
                File nbProjectFolder = FileUtil.toFile(up.getProject().getProjectDirectory());
                // based on current eclipse project the required project should be located either
                // in the same folder as eclipse project or in the same base directory as current eclipse project
                File baseDestination = null;
                if (!nbProjectFolder.equals(ep.getDirectory())) {
                    baseDestination = nbProjectFolder.getParentFile();
                }
                projAndDest.dest = baseDestination;
                Set<EclipseProject> requiredProjs = ProjectSelectionPanel.getFlattenedRequiredProjects(Collections.<EclipseProject>singleton(ep));
                for (EclipseProject requiredEP : requiredProjs) {
                    File destination = baseDestination == null ? requiredEP.getDirectory() : new File(baseDestination, requiredEP.getDirectory().getName());
                    if (!projAndDest.eps.contains(requiredEP) && !findNetBeansProject(destination, requiredEP)) {
                        projAndDest.eps.add(requiredEP);
                    }
                }
            }
            if (projAndDest.eps.size() > 0) {
                workspaceProjectsMap.put(workspace, projAndDest);
            }
        }
        if (workspaceProjectsMap.values().size() == 0) {
            return Boolean.FALSE;
        }
        if (workspaceProjectsMap.values().size() > 0 && silent) {
            return null;
        }
        if (!RequiredProjectsPanel.showConfirmation(workspaceProjectsMap.values())) {
            return null;
        }
        for (Map.Entry<File, ProjectsAndDestination> entry : workspaceProjectsMap.entrySet()) {
            ImportProjectAction.performImport(ProjectSelectionPanel.getFlattenedProjects(entry.getValue().eps), 
                    entry.getValue().dest == null ? null : entry.getValue().dest.getPath(), null, 
                    entry.getValue().eps.size(), false, true, importProblems, createdProjects);
        }
        return Boolean.TRUE;
    }
    
    private boolean findNetBeansProject(File destination, EclipseProject requiredProject) throws IOException {
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (requiredProject.getName().equals(p.getLookup().lookup(ProjectInformation.class).getDisplayName())) {
                return true;
            }
        }
        if (!destination.exists()) {
            return false;
        }
        return ProjectManager.getDefault().findProject(FileUtil.toFileObject(destination)) != null;
    }

    private boolean updateExistingProjects(List<UpgradableProject> ups, List<String> importProblems, boolean silent) throws IOException {
        boolean changed = false;
        boolean deepTest = !silent;
        for (UpgradableProject up : ups) {
            if (!up.isUpToDate(deepTest)) {
                List<String> issues = new ArrayList<String>();
                changed = true;
                up.update(issues);
                if (issues.size() > 0) {
                    importProblems.add(org.openide.util.NbBundle.getMessage(UpdateAllProjects.class, "MSG_ProjectUpdateIssues", up.getEclipseProject().getName()));
                    for (String s : issues) {
                        importProblems.add(" "+s); //NOI18N
                    }
                }
            }
        }
        return changed;
    }
    
    /**
     * 
     * @param silent true for updates from project open hook - no UI is shown 
     *  and any problem aborts update
     */
    public void update(boolean silent) {
        LOG.fine("Eclipse resynchronize started ("+silent+")"); //NOI18N
        WorkspaceFactory.getInstance().resetCache();
        List<String> importProblems = new ArrayList<String>();
        List<UpgradableProject> projs = getListOfUpdatableProjects();
        if (projs.size() == 0 && !silent) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UpdateProjectAction.class, "UpdateProjectAction.nothing-to-synch")));
            return;
        }
        if (!ensureProjectsReachable(projs, silent)) {
            return;
        }
        try {
            Boolean res = resolveNewRequiredProjects(projs, silent, importProblems, null);
            if (res == null) {
                return;
            }
            boolean change = updateExistingProjects(projs, importProblems, silent);
            if (!change && res.equals(Boolean.FALSE) && !silent) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(UpdateProjectAction.class, "UpdateProjectAction.already-in-synch")));
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "synchronization with Eclipse failed", ex); // NOI18N
            importProblems.add(org.openide.util.NbBundle.getMessage(UpdateAllProjects.class, "MSG_UpdateFailed", ex.getMessage()));
        }
        if (importProblems.size() > 0) {
            importProblems.add(0,
                    NbBundle.getMessage(UpdateProjectAction.class, "UpdateProjectAction.problems-occurred-2"));
        }
        ImportProblemsPanel.showReport(
                NbBundle.getMessage(UpdateProjectAction.class, "UpdateProjectAction.update-issues"),
                importProblems);
    }

}
