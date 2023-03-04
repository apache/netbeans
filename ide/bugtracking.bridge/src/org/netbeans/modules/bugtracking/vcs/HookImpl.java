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

package org.netbeans.modules.bugtracking.vcs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryQuery;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.Format;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.PushOperation;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
class HookImpl {
    
    private HookPanel panel;
    static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.vcshooks");        // NOI18N
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");// NOI18N
    private final VCSHooksConfig config;
    private final String[] supportedIssueInfoVariables;
    private final String[] supportedRevisionVariables;

    HookImpl(VCSHooksConfig config, String[] supportedIssueInfoVariables, String[] supportedRevisionVariables) {
        this.config = config;
        this.supportedIssueInfoVariables = supportedIssueInfoVariables;
        this.supportedRevisionVariables = supportedRevisionVariables;
    }
    
    public String beforeCommit(File[] files, String msg) throws IOException {
        if(files.length == 0) {
            LOG.warning("calling beforeCommit for zero files");              // NOI18N
            return null;
        }

        File file = files[0];
        LOG.log(Level.FINE, "beforeCommit start for {0}", file);             // NOI18N

        if (isLinkSelected()) {

            Format format = config.getIssueInfoTemplate();
            String formatString = format.getFormat();
            formatString = HookUtils.prepareFormatString(formatString, supportedIssueInfoVariables);
            
            Issue issue = getIssue();
            if (issue == null) {
                LOG.log(Level.FINE, " no issue set for {0}", file);             // NOI18N
                return null;
            }
            String issueInfo = new MessageFormat(formatString).format(
                    new Object[] {issue.getID(), issue.getSummary()},
                    new StringBuffer(),
                    null).toString();

            LOG.log(Level.FINER, " commit hook issue info ''{0}''", issueInfo); // NOI18N
            if(format.isAbove()) {
                msg = issueInfo + "\n" + msg;                                   // NOI18N
            } else {
                msg = msg + "\n" + issueInfo;                                   // NOI18N
            }                        
            return msg;
        }
        return null;
    }
            
    public void afterCommit(File[] files, String author, String revision, Date date, String message, String hookUsageName, boolean applyPush) {
        if(panel == null) {
            LOG.fine("no settings for afterCommit");                            // NOI18N
            return;
        }

        if(files.length == 0) {
            LOG.warning("calling afterCommit for zero files");               // NOI18N
            return;
        }

        File file = files[0];
        LOG.log(Level.FINE, "afterCommit start for {0}", file);              // NOI18N

        Issue issue = getIssue();
        if (issue == null) {
            LOG.log(Level.FINE, " no issue set for {0}", file);                 // NOI18N
            return;
        }

        config.setLink(isLinkSelected());
        config.setResolve(isResolveSelected());
        config.setAfterCommit(isCommitSelected());

        if (!isLinkSelected() &&
            !isResolveSelected())
        {
            LOG.log(Level.FINER, " nothing to do in afterCommit for {0}", file);   // NOI18N
            return;
        }

        String msg = null;
        if(isLinkSelected()) {
            String formatString = config.getRevisionTemplate().getFormat();
            formatString = HookUtils.prepareFormatString(formatString, supportedRevisionVariables); // NOI18N

            msg = new MessageFormat(formatString).format(
                    new Object[] {
                        revision,
                        author,
                        date != null ? CC_DATE_FORMAT.format(date) : "",        // NOI18N
                        message},
                    new StringBuffer(),
                    null).toString();

            LOG.log(Level.FINER, " afterCommit message ''{0}''", msg);       // NOI18N
        }        
        
        LOG.log(Level.FINER, " commit hook message ''{0}'', resolved {1}", new Object[]{msg, isResolveSelected()});     // NOI18N
        if((isLinkSelected() || isResolveSelected() ) && isCommitSelected()) {
            issue.addComment(msg, isResolveSelected());
            issue.open();
        } else if(applyPush) {
            LOG.log(Level.FINER, " commit hook message will be set after push");     // NOI18N            
            config.setPushAction(revision, new PushOperation(issue.getID(), msg, isResolveSelected()));
            LOG.log(Level.FINE, "schedulig issue {0} for file {1}", new Object[]{issue.getID(), file}); // NOI18N
        }
        LOG.log(Level.FINE, "afterCommit end for {0}", file);                // NOI18N
        VCSHooksConfig.logHookUsage(hookUsageName, getSelectedRepository());             // NOI18N
    }
        
    public void afterPush(File[] files, String[] changesets, String hookUsageName) {
        if(files.length == 0) {
            LOG.warning("calling after push for zero files");                   // NOI18N
            return;
        }
        File file = files[0];
        LOG.log(Level.FINE, "push hook start for {0}", file);                   // NOI18N

        Repository repo = null;
        for (String changeset : changesets) {

            PushOperation operation = config.popPushAction(changeset);
            if(operation == null) {
                LOG.log(Level.FINE, " no push hook scheduled for {0}", file);   // NOI18N
                continue;
            }

            if(repo == null) { // don't go for the repository until we really need it
                repo = RepositoryQuery.getRepository(FileUtil.toFileObject(file), true); // true -> ask user if repository unknown
                                                                                                       // might have deleted in the meantime
                if(repo == null) {
                    LOG.log(Level.WARNING, " could not find issue tracker for {0}", file);      // NOI18N
                    break;
                }
            }
            
            Issue[] issues = repo.getIssues(operation.getIssueID());
            if(issues == null || issues.length == 0) {
                LOG.log(Level.FINE, " no issue found with id {0}", operation.getIssueID());  // NOI18N
                continue;
            }

            issues[0].addComment(operation.getMsg(), operation.isClose());
        }
        LOG.log(Level.FINE, "push hook end for {0}", file);                     // NOI18N
        VCSHooksConfig.logHookUsage(hookUsageName, getSelectedRepository());             // NOI18N
    }
    
    public void afterChangesetReplace (File[] files, Map<String, String> changesets, String hookUsageName) {
        if(files.length == 0) {
            LOG.warning("calling afterChangesetReplace for zero files");                   // NOI18N
            return;
        }
        File file = files[0];
        LOG.log(Level.FINE, "afterChangesetReplace hook start for {0}", file);                   // NOI18N

        for (Map.Entry<String, String> changesetMapping : changesets.entrySet()) {
            String original = changesetMapping.getKey();
            String replace = changesetMapping.getValue();
            PushOperation operation = config.popPushAction(original);
            if (operation != null) {
                if (replace == null) {
                    // shouldn't we delete the original push operation?
                    LOG.log(Level.FINE, "afterChangesetReplace hook found a deleted changeset {0}", original); //NOI18N
                    config.setPushAction(original, operation);
                } else {
                    LOG.log(Level.FINE, "afterChangesetReplace hook found a replaced changeset {0}->{1}", //NOI18N
                            new Object[] { original, replace });
                    config.setPushAction(replace, new PushOperation(operation.getIssueID(), 
                            operation.getMsg().replaceAll(original, replace), // replace also all links to the old changeset
                            operation.isClose()));
                }
            }
            
        }
        LOG.log(Level.FINE, "afterChangesetReplace hook end for {0}", file); // NOI18N
    }

    public HookPanel createComponent(File[] files) {
        return createComponent(files, null);
    }
    public HookPanel createComponent(File[] files, Boolean afterCommit) {
        LOG.finer("HookImpl.createComponent()");                              // NOI18N
        File referenceFile;
        if(files.length == 0) {
            referenceFile = null;
            LOG.warning("creating hook component for zero files");           // NOI18N
        } else {
            referenceFile = files[0];
        }
        
        panel = new HookPanel(
                        FileUtil.toFileObject(referenceFile),
                        config.getLink(),
                        config.getResolve(),
                        afterCommit != null ? afterCommit : config.getAfterCommit());
        
        panel.changeFormatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onShowFormat();
            }
        });
        return panel;
    }

    private void onShowFormat() {
        FormatPanel p = 
                new FormatPanel(
                    config.getRevisionTemplate(),
                    config.getDefaultRevisionTemplate(),
                    supportedRevisionVariables,
                    config.getIssueInfoTemplate(),
                    config.getDefaultIssueInfoTemplate(),
                    supportedIssueInfoVariables);
        if(HookUtils.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"), new HelpCtx(panel.getClass()))) {  // NOI18N
            config.setRevisionTemplate(p.getIssueFormat());
            config.setIssueInfoTemplate(p.getCommitFormat());
        }
    }

    private boolean isLinkSelected() {
        return (panel != null) && panel.linkCheckBox.isSelected();
    }

    private boolean isResolveSelected() {
        return (panel != null) && panel.resolveCheckBox.isSelected();
    }

    private boolean isCommitSelected() {
        return (panel != null) && panel.commitRadioButton.isSelected();
    }

    private Repository getSelectedRepository() {
        return (panel != null) ? panel.getSelectedRepository() : null;
    }

    private Issue getIssue() {
        return (panel != null) ? panel.getIssue() : null;
    }
}
