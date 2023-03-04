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

import javax.swing.Icon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Defines search scope across all open projects.
 *
 * @author  Marian Petras
 */
final class SearchScopeOpenProjects extends AbstractProjectSearchScope {

    @StaticResource
    private static final String ALL_PROJECTS_ICON =
            "org/netbeans/modules/search/project/resources/all_projects.png"; //NOI18N
    private static final Icon ICON;

    static {
        ICON = ImageUtilities.loadImageIcon(ALL_PROJECTS_ICON, false);
    }

    SearchScopeOpenProjects() {
        super(OpenProjects.PROPERTY_OPEN_PROJECTS);
    }

    @Override
    public String getTypeId() {
        return "open projects";                                         //NOI18N
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(),
                                   "SearchScopeNameOpenProjects");      //NOI18N
    }
        
    private boolean checkIsApplicable() {
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }

    @Override
    public SearchInfo getSearchInfo() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        if (openProjects.length == 0) {
            /*
             * We cannot prevent this situation. The action may be invoked
             * between moment the last project had been removed and the removal
             * notice was distributed to the open projects listener (and this
             * action disabled). This may happen if the the last project
             * is being removed in another thread than this action was
             * invoked from.
             */
            return SearchInfoUtils.createEmptySearchInfo();
        }
        
        if (openProjects.length == 1) {
            return createSingleProjectSearchInfo(openProjects[0]);
        }
        
        SearchInfo[] prjSearchInfos = new SearchInfo[openProjects.length];
        for (int i = 0; i < prjSearchInfos.length; i++) {
            prjSearchInfos[i] = createSingleProjectSearchInfo(openProjects[i]);
        }
        return SearchInfoUtils.createCompoundSearchInfo(prjSearchInfos);
    }

    @Override
    public boolean isApplicable() {
        return checkIsApplicable();
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }
}
