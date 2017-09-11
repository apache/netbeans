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
