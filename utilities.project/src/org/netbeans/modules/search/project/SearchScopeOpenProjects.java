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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
