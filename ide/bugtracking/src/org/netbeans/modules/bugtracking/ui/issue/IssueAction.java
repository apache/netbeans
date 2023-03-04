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
package org.netbeans.modules.bugtracking.ui.issue;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.bugtracking.ui.issue.IssueAction", category = "Bugtracking")
@ActionRegistration(lazy = false, displayName = "#CTL_IssueAction")
@ActionReference(path = "Menu/Versioning", name = "org-netbeans-modules-bugtracking-ui-query-IssueAction", position = 300, separatorAfter = 301)
public class IssueAction extends SystemAction {

    private static final RequestProcessor rp = new RequestProcessor("Bugtracking IssueAction"); // NOI18N

    public IssueAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(IssueAction.class, "CTL_IssueAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(IssueAction.class);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        createIssue();
    }

    public static void openIssue(final IssueImpl issue) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UIUtils.setWaitCursor(true);
                IssueTopComponent tc = IssueTopComponent.find(issue);
                tc.open();
                tc.requestActive();
                issue.setSeen(true);
                UIUtils.setWaitCursor(false); // do we need this?
            }
        });
    }

    private static void createIssue() {
        createIssue(null);
    }

    public static void createIssue(final RepositoryImpl repository) {
        BugtrackingManager.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                final File file = BugtrackingUtil.getLargerSelection();
                final boolean repositoryGiven = repository != null;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final IssueTopComponent tc = new IssueTopComponent();
                        tc.initNewIssue(repository, !repositoryGiven, file);
                    }
                });
            }
        });
    }

    public static void openIssue(final FileObject file, final String issueId) {
        openIssueIntern(null, file, issueId);
    }

    public static void openIssue(final RepositoryImpl repository, final String issueId) {
        openIssueIntern(repository, null, issueId);
    }

    private static void openIssueIntern(final RepositoryImpl repositoryParam, final FileObject file, final String issueId) {
        assert issueId != null;
        assert file == null || repositoryParam == null && file != null;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UIUtils.setWaitCursor(true);
                final IssueTopComponent tc = IssueTopComponent.find(issueId, repositoryParam);
                final boolean tcOpened = tc.isOpened();
                final IssueImpl issue = tc.getIssue();
                if (issue == null) {
                    tc.initNoIssue(issueId);
                }
                if(!tcOpened) {
                    tc.open();
                }
                tc.requestActive();
                rp.post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressHandle handle = null;
                        try {
                            if (issue != null) {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(IssueAction.class, "LBL_REFRESING_ISSUE", new Object[]{issueId}));
                                handle.start();
                                issue.refresh();
                            } else {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(IssueAction.class, "LBL_OPENING_ISSUE", new Object[]{issueId}));
                                handle.start();

                                RepositoryImpl repository;
                                if(repositoryParam == null) {
                                    repository = BugtrackingOwnerSupport.getInstance().getRepository(file, true);
                                    if(repository == null) {
                                        // if no repository was known user was supposed to choose or create one
                                        // in scope of the previous getRepository() call. So null shoud stand
                                        // for cancel in this case.
                                        handleTC();
                                        return;
                                    }
                                    BugtrackingOwnerSupport.getInstance().setFirmAssociation(file, repository);

                                } else {
                                    repository = repositoryParam;
                                }
                                final Collection<IssueImpl> impls = repository.getIssueImpls(issueId);
                                if(impls == null || impls.isEmpty()) {
                                    // lets hope the repository was able to handle this
                                    // because whatever happend, there is nothing else
                                    // we can do at this point
                                    handleTC();
                                    return;
                                }
                                final IssueImpl impl = impls.iterator().next();
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        tc.setIssue(impl);
                                    }
                                });
                                impl.setSeen(true);
                            }
                        } finally {
                            if(handle != null) handle.finish();
                            UIUtils.setWaitCursor(false);
                        }
                    }

                    public void handleTC() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (!tcOpened) {
                                    tc.close();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public static void closeIssue(final IssueImpl issue) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueTopComponent tc = IssueTopComponent.find(issue);
                if(tc != null) {
                    tc.close();
                }
            }
        });
    }    
}
