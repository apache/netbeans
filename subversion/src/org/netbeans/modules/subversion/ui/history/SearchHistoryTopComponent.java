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
package org.netbeans.modules.subversion.ui.history;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.ui.diff.DiffSetupSource;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import java.util.*;
import java.io.File;
import java.awt.BorderLayout;

/**
 * @author Maros Sandor
 */
public class SearchHistoryTopComponent extends TopComponent implements DiffSetupSource {

    private SearchHistoryPanel shp;

    public SearchHistoryTopComponent() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSN_SearchHistoryT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSD_SearchHistoryT_Top_Component")); // NOI18N
    }
    
    public SearchHistoryTopComponent(Context context) {
        this(context, null, null);
    }

    SearchHistoryTopComponent(File[] files) {
        this();
        initComponents(files, null, null);
    }
    
    public SearchHistoryTopComponent(Context context, Date from, Date to) {
        this();
        initComponents(context.getRootFiles(), from, to);
    }

    /**
     * Support for openning file history with a specific DiffResultsView
     * @param file it's history shall be shown
     * @param fac factory creating a specific DiffResultsView - just override its createDiffResultsView method
     */
    SearchHistoryTopComponent(File file, DiffResultsViewFactory fac) {
        this();
        initComponents(new File[] {file}, null, null);
        shp.setDiffResultsViewFactory(fac);
    }

    public SearchHistoryTopComponent(SVNUrl repositoryUrl, File localRoot, long revision) {
        this();
        initComponents(repositoryUrl, localRoot, revision);
    }

    public void search (boolean showCriteria) {        
        shp.executeSearch();
        shp.setSearchCriteria(showCriteria);
    }

    void activateDiffView (boolean selectFirstRevision) {
        shp.activateDiffView(selectFirstRevision);
    }
    
    private void initComponents(SVNUrl repositoryUrl, File localRoot, long revision) {
        setLayout(new BorderLayout());
        SearchCriteriaPanel scp = new SearchCriteriaPanel(repositoryUrl);
        scp.setFrom(Long.toString(revision));
        scp.setTo(Long.toString(revision));
        shp = new SearchHistoryPanel(repositoryUrl, localRoot, scp);
        add(shp);
    }

    private void initComponents(File[] roots, Date from, Date to) {
        setLayout(new BorderLayout());
        SearchCriteriaPanel scp = new SearchCriteriaPanel(roots);
        if (from != null) scp.setFrom(SearchExecutor.simpleDateFormat.format(from));
        if (to != null) scp.setTo(SearchExecutor.simpleDateFormat.format(to));
        shp = new SearchHistoryPanel(roots, scp);
        add(shp);
    }

    public int getPersistenceType(){
       return TopComponent.PERSISTENCE_NEVER;
    }
    
    protected void componentClosed() {
       shp.windowClosed();
       super.componentClosed();
    }
    
    protected String preferredID(){
       return "Svn.SearchHistoryTopComponent";    // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public Collection getSetups() {
        return shp.getSetups();
    }

    public String getSetupDisplayName() {
        return getDisplayName();
    }

    /**
     * Provides an initial diff view. To display a specific one, override createDiffResultsView.
     */
    public static class DiffResultsViewFactory {
        DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
            return new DiffResultsView(panel, results);
        }
    }
}
