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
