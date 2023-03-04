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
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.openide.util.NbBundle;

/**
 * Defines search scope across the main project.
 *
 * @author  Marian Petras
 */
final class SearchScopeMainProject extends AbstractProjectSearchScope {
    
    SearchScopeMainProject() {
        super(OpenProjects.PROPERTY_MAIN_PROJECT);
    }

    @Override
    public String getTypeId() {
        return "main project";                                          //NOI18N
    }
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(),
                                   "SearchScopeNameMainProject");       //NOI18N
    }

    protected boolean checkIsApplicable() {
        return OpenProjects.getDefault().getMainProject() != null;
    }

    @Override
    public SearchInfo getSearchInfo() {
        Project mainProject = OpenProjects.getDefault().getMainProject();
        if (mainProject == null) {
            /*
             * We cannot prevent this situation. The action may be invoked
             * between moment the main project had been closed and the removal
             * notice was distributed to the main project listener (and this
             * action disabled). This may happen if the the main project
             * is being closed in another thread than this action was
             * invoked from.
             */
            return SearchInfoUtils.createEmptySearchInfo();
        }
        
        return createSingleProjectSearchInfo(mainProject);
    }

    @Override
    public boolean isApplicable() {
        return checkIsApplicable();
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Icon getIcon() {
        Project p = OpenProjects.getDefault().getMainProject();
        if (p != null) {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            if (pi != null) {
                return pi.getIcon();
            }
        }
        return null;
    }
}
