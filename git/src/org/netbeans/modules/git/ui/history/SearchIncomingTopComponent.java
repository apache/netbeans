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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.git.ui.history;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.io.File;
import java.awt.BorderLayout;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.openide.util.Parameters;

@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_NEVER, preferredID="Git.SearchIncomingTopComponent")
@NbBundle.Messages({
    "ACSN_SearchIncomingT_Top_Component=Search Incoming",
    "ACSD_SearchIncomingT_Top_Component=Search Incoming"
})
public class SearchIncomingTopComponent extends TopComponent {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;
    private final File[] files;
    private final File repository;
    private final RepositoryInfo info;
    
    public SearchIncomingTopComponent (File repository, RepositoryInfo info, File[] files, boolean searchInContext) {
        this.repository = repository;
        this.info = info;
        this.files = files;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchIncomingTopComponent.class, "ACSN_SearchIncomingT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchIncomingTopComponent.class, "ACSD_SearchIncomingT_Top_Component")); // NOI18N
        initComponents();
        scp.setupRemoteSearch(SearchExecutor.Mode.REMOTE_IN, searchInContext);
    }

    public void search (boolean showCriteria) {
        shp.executeSearch();
        shp.setSearchCriteria(showCriteria);
    }
    
    void setBranch (String branch) {
        Parameters.notNull("branch", branch);
        shp.setBranch(branch);
        scp.setBranch(branch);
    }

    private void initComponents () {
        setLayout(new BorderLayout());
        scp = new SearchCriteriaPanel();
        shp = new SearchHistoryPanel(repository, info, files, scp);
        add(shp);
    }

    @Override
    protected void componentClosed () {
        shp.release();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
}
