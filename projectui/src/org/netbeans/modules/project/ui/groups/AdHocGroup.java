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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.RequestProcessor;

/**
 * Arbitrary collection of projects, with an optional main project.
 * @author Jesse Glick
 */
public class AdHocGroup extends Group {

    private static final Logger LOG = Logger.getLogger(AdHocGroup.class.getName());

    /** Preferences key for whether to automatically synchronize projects. */
    private static final String KEY_AUTO_SYNCH = "autoSynch"; // NOI18N

    static final String KIND = "adHoc"; // NOI18N
    
    private static final RequestProcessor RP = new RequestProcessor(AdHocGroup.class);
            

    /**
     * Create a new ad-hoc group of projects.
     */
    public static AdHocGroup create(String name, boolean autoSynch) {
        String sanitizedId = sanitizeNameAndUniquifyForId(name);
        LOG.log(Level.FINE, "Creating: {0}", sanitizedId);
        Preferences p = NODE.node(sanitizedId);
        p.put(KEY_NAME, name);
        p.put(KEY_KIND, KIND);
        p.putBoolean(KEY_AUTO_SYNCH, autoSynch);
        return new AdHocGroup(sanitizedId);
    }

    AdHocGroup(String id) {
        super(id);
    }

    @Override protected void findProjects(Set<Project> projects, ProgressHandle h, int start, int end) {
        List<String> paths = projectPaths();
        for (String path : paths) {
                Project p = projectForPath(path);
                if (p != null) {
                    if (h != null) {
                    h.progress(progressMessage(p), start += ((end - start) / paths.size()));
                    }
                    projects.add(p);
                }
            }
        }

    @Override protected List<String> projectPaths() {
        String paths = prefs().get(KEY_PATH, "");
        if (paths.length() > 0) { // "".split(...) -> [""]
            return Arrays.asList(paths.split(" "));
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Change the projects in the group.
     */
    public void setProjects(Set<Project> projects) {
        Set<String> projectPaths = new TreeSet<String>();
        for (Project prj : projects) {
            projectPaths.add(prj.getProjectDirectory().toURL().toExternalForm());
        }
        prefs().put(KEY_PATH, joinPaths(projectPaths));
        LOG.log(Level.FINE, "updating projects for {0} to {1}", new Object[] {id, projects});
    }

    /**
     * If true, group will automatically update its contents when open.
     */
    public boolean isAutoSynch() {
        return prefs().getBoolean(KEY_AUTO_SYNCH, false);
    }

    /**
     * @see #isAutoSynch
     */
    public void setAutoSynch(boolean b) {
        prefs().putBoolean(KEY_AUTO_SYNCH, b);
    }

    /**
     * Update a group's definition with the current list of open projects (and main project).
     */
    public void synch() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                OpenProjects op = OpenProjects.getDefault();
                setProjects(new HashSet<Project>(Arrays.asList(op.getOpenProjects())));
                setMainProject(op.getMainProject());
            }
        });
    }

    @Override
    public GroupEditPanel createPropertiesPanel() {
        return new AdHocGroupEditPanel(this);
    }

    @Override protected void openProjectsEvent(String propertyName) {
        if (propertyName.equals(OpenProjects.PROPERTY_OPEN_PROJECTS) && isAutoSynch()) {
            setProjects(new HashSet<Project>(Arrays.asList(OpenProjects.getDefault().getOpenProjects())));
        }
        // *After* setting projects - so that main project status correctly updated for new group.
        super.openProjectsEvent(propertyName);
    }

    @Override
    public boolean isPristine() {
        if (isAutoSynch()) {
            return true;
        } else {
            return super.isPristine();
        }
    }

    @Override
    protected String toString(boolean scrubPersonalInfo) {
        return super.toString(scrubPersonalInfo) + (isAutoSynch() ? "" : "[!autoSynch]");
    }

}
