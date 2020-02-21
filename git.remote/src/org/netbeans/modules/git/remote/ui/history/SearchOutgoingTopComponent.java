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
package org.netbeans.modules.git.remote.ui.history;

import java.awt.BorderLayout;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.windows.TopComponent;

@TopComponent.Description(persistenceType=TopComponent.PERSISTENCE_NEVER, preferredID="GitRemote.SearchOutgoingTopComponent")
@NbBundle.Messages({
    "ACSN_SearchOutgoingT_Top_Component=Search Incoming",
    "ACSD_SearchOutgoingT_Top_Component=Search Incoming"
})
public class SearchOutgoingTopComponent extends TopComponent {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;
    private final VCSFileProxy[] files;
    private final VCSFileProxy repository;
    private final RepositoryInfo info;
    
    public SearchOutgoingTopComponent (VCSFileProxy repository, RepositoryInfo info, VCSFileProxy[] files) {
        this.repository = repository;
        this.info = info;
        this.files = files;
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchOutgoingTopComponent.class, "ACSN_SearchOutgoingT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchOutgoingTopComponent.class, "ACSD_SearchOutgoingT_Top_Component")); // NOI18N
        initComponents();
        scp.setupRemoteSearch(SearchExecutor.Mode.REMOTE_OUT);
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
