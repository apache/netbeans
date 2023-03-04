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
