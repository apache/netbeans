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
package org.netbeans.modules.bugtracking.ui.query;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Maros Sandor
 */
@ActionID(id = "org.netbeans.modules.bugtracking.ui.query.QueryAction", category = "Bugtracking")
@ActionRegistration(lazy = false, displayName = "#CTL_QueryAction")
@ActionReference(path = "Menu/Versioning", position = 200)
public class QueryAction extends SystemAction {

    public QueryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(QueryAction.class, "CTL_QueryAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(QueryAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        createNewQuery(null);
    }

    public  static void createNewQuery(RepositoryImpl repositoryToSelect) {
        openQuery(null, repositoryToSelect, repositoryToSelect != null, QueryMode.EDIT, true);
    }
    
    public static void createNewQuery(RepositoryImpl repositoryToSelect, boolean suggestedSelectionOnly) {
        openQuery(null, repositoryToSelect, suggestedSelectionOnly, QueryMode.EDIT, true);
    }

    public static void openQuery(QueryImpl query, RepositoryImpl repository, QueryMode mode) {
        openQuery(query, repository, false, mode, false);
    }
    
    private static void openQuery(final QueryImpl query, final RepositoryImpl repository, final boolean suggestedSelectionOnly, final QueryMode mode, final boolean isNew) {
        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                final File file = BugtrackingUtil.getLargerSelection();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        BugtrackingManager.LOG.log(Level.FINE, "QueryAction.openQuery start. query [{0}]", new Object[] {query != null ? query.getDisplayName() : null});
                        UIUtils.setWaitCursor(true);
                        try {
                            QueryTopComponent tc = null;
                            if(query != null) {
                                tc = QueryTopComponent.find(query);
                            }
                            if(tc == null) {
                                tc = new QueryTopComponent();
                                tc.init(query, repository, file, suggestedSelectionOnly, mode, isNew);
                            } else {
                                tc.setMode(mode);
                            }
                            if(!tc.isOpened()) {
                                tc.open();
                            }
                            tc.requestActive();
                            BugtrackingManager.LOG.log(Level.FINE, "QueryAction.openQuery finnish. query [{0}]", new Object[] {query != null ? query.getDisplayName() : null});
                        } finally {
                            UIUtils.setWaitCursor(false);
                        }
                    }
                });
            }
        });
    }

    public static void closeQuery(final QueryImpl query) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent tc = null;
                if(query != null) {
                    tc = WindowManager.getDefault().findTopComponent(query.getDisplayName());
                }
                if(tc != null) {
                    tc.close();
                }
            }
        });
    }
}
