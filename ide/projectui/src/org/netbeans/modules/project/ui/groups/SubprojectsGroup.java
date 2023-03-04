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

package org.netbeans.modules.project.ui.groups;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * One project and all (recursive) subprojects.
 * @author Jesse Glick
 */
public class SubprojectsGroup extends Group {

    private static final Logger LOG = Logger.getLogger(SubprojectsGroup.class.getName());

    static final String KIND = "subprojects"; // NOI18N

    /**
     * Create a new group based on a superproject.
     * The display name is by default that of the superproject.
     */
    public static SubprojectsGroup create(String name, Project project) {
        String path = project.getProjectDirectory().toURL().toExternalForm();
        String id = sanitizeNameAndUniquifyForId(name);
        LOG.log(Level.FINE, "Creating: {0}", id);
        Preferences p = NODE.node(id);
        p.put(KEY_NAME, name);
        p.put(KEY_KIND, KIND);
        p.put(KEY_PATH, path);
        return new SubprojectsGroup(id);
    }

    SubprojectsGroup(String id) {
        super(id);
    }

    @Override
    protected String getNameOrNull() {
        String n = super.getNameOrNull();
        if (n == null) {
            Project p = projectForPath(prefs().get(KEY_PATH, null));
            if (p != null) {
                return ProjectUtils.getInformation(p).getDisplayName();
            }
        }
        return n;
    }

    @Override
    protected void findProjects(Set<Project> projects, ProgressHandle h, int start, int end) {
        assert !SwingUtilities.isEventDispatchThread();
        Project p = projectForPath(prefs().get(KEY_PATH, null));
        if (p != null) {
            visitSubprojects(p, projects, true, h, new int[] {start, end});
        }
    }

    private static void visitSubprojects(Project p, Set<Project> projects, boolean main, ProgressHandle h, int[] startEnd) {
        if ((main || !ProjectConvertors.isConvertorProject(p)) && projects.add(p)) {
            if (h != null) {
                h.progress(progressMessage(p), Math.min(++startEnd[0], startEnd[1]));
            }
            ProjectContainerProvider pcp = p.getLookup().lookup(ProjectContainerProvider.class);
            if (pcp != null) {
                ProjectContainerProvider.Result res = pcp.getContainedProjects();
                projects.addAll(res.getProjects());
                if (!res.isRecursive()) {
                    visitSubprojects(p, projects, false, h, startEnd);
                }
            } else {
                //fallback to semi-deprecated subprojectprovider
                SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
                if (spp != null) {
                    for (Project p2 : spp.getSubprojects()) {
                        visitSubprojects(p2, projects, false, h, startEnd);
                    }
                }
            }
        }
    }

    public FileObject getMasterProjectDirectory() {
        String p = prefs().get(KEY_PATH, null);
        if (p != null && p.length() > 0) {
            try {
                return URLMapper.findFileObject(new URL(p));
            } catch (MalformedURLException x) {
                LOG.log(Level.WARNING, null, x);
            }
        }
        return null;
    }

    @Override
    public GroupEditPanel createPropertiesPanel() {
        return new SubprojectsGroupEditPanel(this);
    }

}
