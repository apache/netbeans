/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import javax.swing.JPanel;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryQuery;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.Format;
import org.netbeans.modules.bugtracking.vcs.VCSHooksConfig.PushOperation;
import org.netbeans.modules.versioning.hooks.HgQueueHook;
import org.netbeans.modules.versioning.hooks.HgQueueHookContext;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Mercurial queue hook implementation
 * @author Tomas Stupka
 */
public class HgQueueHookImpl extends HgQueueHook {

    private static final String[] SUPPORTED_ISSUE_INFO_VARIABLES = new String[] {"id", "summary"};                        // NOI18N
    private static final String[] SUPPORTED_REVISION_VARIABLES = new String[] {"changeset", "author", "date", "message"}; // NOI18N
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");// NOI18N

    private HgQueueHookPanel panel;
    private final VCSQueueHooksConfig config;

    private final String name;
    private static final String HOOK_NAME = "HG"; //NOI18N
    private final VCSHooksConfig globalConfig;
    private static final Set<Issue> cachedIssues = Collections.newSetFromMap(new WeakHashMap<>());
    private Format issueMessageTemplate;

    public HgQueueHookImpl() {
        name = NbBundle.getMessage(HgQueueHookImpl.class, "LBL_VCSHook"); //NOI18N
        globalConfig = VCSHooksConfig.getInstance(VCSHooksConfig.HookType.HG);
        config = VCSQueueHooksConfig.getInstance(VCSQueueHooksConfig.HookType.HG);
    }

    @Override
    public String getDisplayName () {
        return name;
    }

    @Override
    public HgQueueHookContext beforePatchRefresh (HgQueueHookContext context) throws IOException {
        File[] files = context.getFiles();
        if(files.length == 0) {
            HookImpl.LOG.warning("calling beforePatchRefresh for zero files"); //NOI18N
            return null;
        }
        String msg = context.getMessage();
        File file = files[0];
        if (isLinkSelected()) {
            Format format = globalConfig.getIssueInfoTemplate();
            String formatString = format.getFormat();
            formatString = HookUtils.prepareFormatString(formatString, SUPPORTED_ISSUE_INFO_VARIABLES);
            
            Issue issue = getIssue();
            if (issue == null) {
                HookImpl.LOG.log(Level.FINE, " no issue set for {0}", file);             // NOI18N
                return null;
            }
            cacheIssue(issue);
            String issueInfo = new MessageFormat(formatString).format(
                    new Object[] {issue.getID(), issue.getSummary()},
                    new StringBuffer(),
                    null).toString();

            HookImpl.LOG.log(Level.FINER, " commit hook issue info ''{0}''", issueInfo); // NOI18N
            if(format.isAbove()) {
                msg = issueInfo + "\n" + msg;                                   // NOI18N
            } else {
                msg = msg + "\n" + issueInfo;                                   // NOI18N
            }                        
            return new HgQueueHookContext(context.getFiles(), msg, context.getPatchId());
        }
        return null;
    }

    @Override
    public void afterPatchRefresh (HgQueueHookContext context) {
        clearSettings(context.getPatchId());
        File[] files = context.getFiles();
        if (panel == null) {
            HookImpl.LOG.fine("no settings for afterPatchRefresh");                            // NOI18N
            return;
        }

        if(files.length == 0) {
            HookImpl.LOG.warning("calling afterPatchRefresh for zero files");               // NOI18N
            return;
        }
        String patchId = context.getPatchId();
        if (patchId == null || patchId.isEmpty()) {
            HookImpl.LOG.warning("calling afterPatchRefresh with no patchId");               // NOI18N
            return;
        }

        File file = files[0];
        HookImpl.LOG.log(Level.FINE, "afterPatchRefresh start for {0}", file);              // NOI18N

        Issue issue = getIssue();
        if (issue == null) {
            HookImpl.LOG.log(Level.FINE, " no issue set for {0}", file);                 // NOI18N
            return;
        }

        cacheIssue(issue);
        globalConfig.setLink(isLinkSelected());
        globalConfig.setResolve(isResolveSelected());
        config.setAfterRefresh(isCommitSelected());

        if ((isLinkSelected() || isResolveSelected())) {
            HookImpl.LOG.log(Level.FINER, " commit hook message will be set after qfinish");     // NOI18N            
            if (isCommitSelected()) {
                config.setFinishPatchAction(context.getPatchId(), new VCSQueueHooksConfig.FinishPatchOperation(issue.getID(),
                    issueMessageTemplate.getFormat(), isResolveSelected(), isLinkSelected(), false));
                HookImpl.LOG.log(Level.FINE, "scheduling issue {0} for file {1} after qfinish", new Object[] { issue.getID(), file }); // NOI18N
            } else {
                config.setFinishPatchAction(context.getPatchId(), new VCSQueueHooksConfig.FinishPatchOperation(issue.getID(),
                    issueMessageTemplate.getFormat(), isResolveSelected(), isLinkSelected(), true));
                HookImpl.LOG.log(Level.FINE, "scheduling push preparations for issue {0} for file {1} after qfinish", new Object[] { issue.getID(), file }); // NOI18N
            }
        } else {
            HookImpl.LOG.log(Level.FINER, " nothing to do in afterPatchRefresh for {0}", file);   // NOI18N
            return;
        }

        HookImpl.LOG.log(Level.FINE, "afterCommit end for {0}", file); // NOI18N
        VCSHooksConfig.logHookUsage(HOOK_NAME, getSelectedRepository()); // NOI18N
    }
    
    @Override
    public HgQueueHookContext beforePatchFinish (HgQueueHookContext context) throws IOException {
        return super.beforePatchFinish(context);
    }

    @Override
    public void afterPatchFinish (HgQueueHookContext context) {
        String patchId = context.getPatchId();
        if (patchId == null) {
            HookImpl.LOG.fine("no patchId in afterPatchFinish");                            // NOI18N
            return;
        }

        File[] files = context.getFiles();
        if(files.length == 0) {
            HookImpl.LOG.warning("calling afterPatchFinish for zero files");               // NOI18N
            return;
        }

        VCSQueueHooksConfig.FinishPatchOperation op = config.popFinishPatchAction(patchId, true);
        if (op == null || !(op.isAddInfo() || op.isClose())) {
            HookImpl.LOG.fine("no settings for afterPatchFinish");                            // NOI18N
            return;
        }
        File file = files[0];
        HookImpl.LOG.log(Level.FINE, "afterPatchFinish start for {0}", file);              // NOI18N

        Repository repository = RepositoryQuery.getRepository(FileUtil.toFileObject(file), true); 
        if (repository == null) {
            HookImpl.LOG.log(Level.FINE, " no issue repository for {0}:{1}", new Object[] { op.getIssueID(), file }); //NOI18N
            return;
        }
        Issue issue = getIssue(repository, op.getIssueID());
        if (issue == null) {
            HookImpl.LOG.log(Level.FINE, " no issue found for {0}", op.getIssueID());                 // NOI18N
            return;
        }

        String msg = null;
        String changeset = context.getLogEntries()[0].getChangeset();
        if (op.isAddInfo()) {
            String formatString = op.getMsg();
            formatString = HookUtils.prepareFormatString(formatString, SUPPORTED_REVISION_VARIABLES);
            Date date = context.getLogEntries()[0].getDate();
            msg = new MessageFormat(formatString).format(
                    new Object[] {
                        changeset,
                        context.getLogEntries()[0].getAuthor(),
                        date == null ? "" : CC_DATE_FORMAT.format(date),        // NOI18N
                        context.getLogEntries()[0].getMessage()},
                    new StringBuffer(),
                    null).toString();
            HookImpl.LOG.log(Level.FINER, " afterPatchFinish message ''{0}''", msg);       // NOI18N
        }
        
        HookImpl.LOG.log(Level.FINER, " commit hook message ''{0}'', resolved {1}", new Object[] { msg, op.isClose() }); //NOI18N
        if (op.isAfterPush()) {
            HookImpl.LOG.log(Level.FINER, " commit hook message will be set after push"); //NOI18N            
            globalConfig.setPushAction(changeset, new PushOperation(issue.getID(), msg, op.isClose()));
            HookImpl.LOG.log(Level.FINE, "schedulig issue {0} for file {1}", new Object[] { issue.getID(), file } ); //NOI18N
        } else {
            issue.addComment(msg, isResolveSelected());
            issue.open();
        }
        HookImpl.LOG.log(Level.FINE, "afterPatchFinish end for {0}", file);                // NOI18N
        VCSHooksConfig.logHookUsage(HOOK_NAME, getSelectedRepository());             // NOI18N
    }

    @Override
    public JPanel createComponent (HgQueueHookContext context) {
        HookImpl.LOG.finer("HookImpl.createComponent()");                              // NOI18N
        File[] files = context.getFiles();
        final File referenceFile;
        if(files.length == 0) {
            referenceFile = null;
            HookImpl.LOG.warning("creating hook component for zero files");           // NOI18N
        } else {
            referenceFile = files[0];
        }

        panel = new HgQueueHookPanel(
                        FileUtil.toFileObject(referenceFile),
                        globalConfig.getLink(),
                        globalConfig.getResolve(),
                        config.getAfterRefresh());
        panel.commitRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(HgQueueHookImpl.class, "CTL_HgQueueHookImpl.commitRadioButton.ACSD")); //NOI18N
        Mnemonics.setLocalizedText(panel.commitRadioButton, NbBundle.getMessage(HgQueueHookImpl.class, "CTL_HgQueueHookImpl.commitRadioButton.text")); //NOI18N

        String patchId = context.getPatchId();
        issueMessageTemplate = globalConfig.getRevisionTemplate();
        if (patchId != null) {
            final VCSQueueHooksConfig.FinishPatchOperation op = config.popFinishPatchAction(patchId, false);
            if (referenceFile != null && op != null) {
                issueMessageTemplate = new Format(false, op.getMsg());
                panel.putClientProperty("prop.requestOpened", true); //NOI18N
                final String issueId = op.getIssueID();
                if (issueId != null) {
                    panel.enableIssueField(false);
                    RequestProcessor.getDefault().post(new Runnable() {
                        @Override
                        public void run () {
                            Issue issue = null;
                            Repository repository = null;
                            try {
                                repository = RepositoryQuery.getRepository(FileUtil.toFileObject(referenceFile), false); 
                                if (repository == null) {
                                    issue = null;
                                } else {
                                    issue = getIssue(repository, issueId);
                                }
                            } finally {
                                final Issue fIssue = issue;
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run () {
                                        panel.enableIssueField(true);
                                        if (fIssue != null) {
                                            panel.setIssue(fIssue);
                                            panel.pushRadioButton.setSelected(op.isAfterPush());
                                            panel.commitRadioButton.setSelected(!op.isAfterPush());
                                            panel.linkCheckBox.setSelected(op.isAddInfo());
                                            panel.resolveCheckBox.setSelected(op.isClose());
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
        panel.changeFormatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FormatPanel p = new FormatPanel(
                            issueMessageTemplate,
                            globalConfig.getDefaultRevisionTemplate(),
                            SUPPORTED_REVISION_VARIABLES,
                            globalConfig.getIssueInfoTemplate(),
                            globalConfig.getDefaultIssueInfoTemplate(),
                            SUPPORTED_ISSUE_INFO_VARIABLES);
                if(HookUtils.show(p, NbBundle.getMessage(HookPanel.class, "LBL_FormatTitle"), NbBundle.getMessage(HookPanel.class, "LBL_OK"), new HelpCtx(panel.getClass()))) {  // NOI18N
                    issueMessageTemplate = p.getIssueFormat();
                    globalConfig.setRevisionTemplate(p.getIssueFormat());
                    globalConfig.setIssueInfoTemplate(p.getCommitFormat());
                }
            }
        });
        return panel;
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
        Issue issue = getIssue();
        return (issue == null) ? null : issue.getRepository();
    }

    private Issue getIssue() {
        return (panel != null) ? panel.getIssue() : null;
    }

    private void clearSettings (String patchId) {
        config.clearFinishPatchAction(patchId);
    }

    private Issue getIssue (Repository repository, String issueID) {
        // we can get issue only via repository.getIssue which access the server, so we need to cache issues
        synchronized (cachedIssues) {
            for (Issue issue : cachedIssues) {
                if (repository.equals(issue.getRepository()) && issueID.equals(issue.getID())) {
                    return issue;
                }
            }
        }
        Issue[] issues = repository.getIssues(issueID);
        if (issues != null && issues.length > 0) {
            synchronized (cachedIssues) {
                cachedIssues.add(issues[0]);
            }
        }
        return issues[0];
    }

    private void cacheIssue (Issue issue) {
        synchronized (cachedIssues) {
            cachedIssues.add(issue);
        }
    }
}
