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
