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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.search.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.netbeans.spi.search.SubTreeSearchOptions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Base class for implementations of search scopes depending on the set
 * of open projects.
 *
 * @author  Marian Petras
 */
abstract class AbstractProjectSearchScope extends SearchScopeDefinition
                                          implements PropertyChangeListener {
    
    private final String interestingProperty;
    private PropertyChangeListener openProjectsWeakListener;

    protected AbstractProjectSearchScope(String interestingProperty) {
        super();
        this.interestingProperty = interestingProperty;
        OpenProjects openProjects = OpenProjects.getDefault();
        openProjectsWeakListener = WeakListeners.propertyChange(this,
                openProjects);
        openProjects.addPropertyChangeListener(openProjectsWeakListener);
    }

    @Override
    public void clean() {
        OpenProjects.getDefault().removePropertyChangeListener(
                openProjectsWeakListener);
        openProjectsWeakListener = null;
    }

    @Override
    public final void propertyChange(PropertyChangeEvent e) {
        if (interestingProperty.equals(e.getPropertyName())) {
            notifyListeners();
        }
    }
    
    static SearchInfo createSingleProjectSearchInfo(Project project) {

        SearchInfo prjSearchInfo = SearchInfoHelper.getSearchInfoForLookup(
                project.getLookup());
        if (prjSearchInfo != null) {
            return prjSearchInfo;
        } else {
            return createDefaultProjectSearchInfo(project);
        }
    }

    /**
     * Create default search info for a project.
     */
    static SearchInfo createDefaultProjectSearchInfo(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                Sources.TYPE_GENERIC);

        FileObject base = project.getProjectDirectory();
        List<FileObject> roots = new ArrayList<FileObject>();
        if (base != null) {
            roots.add(base);
        }
        for (SourceGroup sg : sourceGroups) {
            FileObject dir = sg.getRootFolder();
            if (dir != null && (base == null || !isUnderBase(base, dir))) {
                roots.add(dir);
            }
        }
        FileObject[] rootArray = new FileObject[roots.size()];
        for (int i = 0; i < roots.size(); i++) {
            rootArray[i] = roots.get(i);
        }
        SubTreeSearchOptions stso =
                project.getLookup().lookup(SubTreeSearchOptions.class);
        if (stso == null) {
            return SearchInfoUtils.createSearchInfoForRoots(rootArray);
        } else {
            return SearchInfoUtils.createSearchInfoForRoots(rootArray, false,
                    SearchInfoHelper.subTreeFilters(stso));
        }
    }

    /**
     * @return True if {@code dir} is under directory {@code base}, or is
     * identical. False otherwise.
     */
    private static boolean isUnderBase(FileObject base, FileObject dir) {
        return dir == base || FileUtil.isParentOf(base, dir);
    }
}
