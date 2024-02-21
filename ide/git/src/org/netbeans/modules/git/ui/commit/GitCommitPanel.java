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

package org.netbeans.modules.git.ui.commit;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitFileNode;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.git.ui.diff.MultiDiffPanelController;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider;
import org.netbeans.modules.versioning.util.common.VCSCommitFilter;
import org.netbeans.modules.versioning.util.common.VCSCommitPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitParameters.DefaultCommitParameters;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class GitCommitPanel extends VCSCommitPanel<GitLocalFileNode> {

    static final GitCommitFilter FILTER_HEAD_VS_WORKING = new GitCommitFilter(
                "HEAD_VS_WORKING", 
                org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/head_vs_working.png", false),
                NbBundle.getMessage(GitCommitPanel.class, "ParametersPanel.tgbHeadVsWorking.toolTipText"),
                true); 
    static final GitCommitFilter FILTER_HEAD_VS_INDEX = new GitCommitFilter(
                "HEAD_VS_INDEX", 
                org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/head_vs_index.png", false),
                NbBundle.getMessage(GitCommitPanel.class, "ParametersPanel.tgbHeadVsIndex.toolTipText"),
                false);
    
    private final Collection<GitHook> hooks;
    private final File[] roots;
    private final File repository;
    private final boolean fromGitView;
    private final DiffProvider diffProvider;

    private GitCommitPanel(GitCommitTable table, final File[] roots, final File repository, DefaultCommitParameters parameters, Preferences preferences, Collection<GitHook> hooks, 
            VCSHookContext hooksContext, DiffProvider diffProvider, boolean fromGitView, List<VCSCommitFilter> filters) {
        super(table, parameters, preferences, hooks, hooksContext, filters, diffProvider);
        this.diffProvider = diffProvider;
        this.roots = roots;
        this.repository = repository;
        this.hooks = hooks;
        this.fromGitView = fromGitView;
        diffProvider.setMode(getAcceptedMode(getSelectedFilter()));
    }

    public static GitCommitPanel create(final File[] roots, final File repository, GitUser user, boolean fromGitView) {
        Preferences preferences = GitModuleConfig.getDefault().getPreferences();
        String lastCanceledCommitMessage = GitModuleConfig.getDefault().getLastCanceledCommitMessage();

        GitCommitParameters parameters = new GitCommitParameters(preferences, lastCanceledCommitMessage, user);
        
        Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
        GitHookContext hooksCtx = new GitHookContext(roots, null, new GitHookContext.LogEntry[] {});        
        
        DiffProvider diffProvider = new DiffProvider();
        final GitCommitTable gitCommitTable = new GitCommitTable();
        final CommitPanel panel = parameters.getPanel();
        panel.amendCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                gitCommitTable.setAmend(panel.amendCheckBox.isSelected());
            }
        });
        return new GitCommitPanel(gitCommitTable, roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider, fromGitView, createFilters(fromGitView));
    }

    private static void disableFilters () {
        for (GitCommitFilter f : Arrays.asList(FILTER_HEAD_VS_INDEX, FILTER_HEAD_VS_WORKING)) {
            f.setSelected(false);
        }
    }
    
    private static List<VCSCommitFilter> createFilters (boolean gitViewStoredMode) {
        // synchronize access to this static field
        assert EventQueue.isDispatchThread();
        disableFilters();
        Mode mode = gitViewStoredMode ? GitModuleConfig.getDefault().getLastUsedModificationContext() : GitModuleConfig.getDefault().getLastUsedCommitViewMode();
        (Mode.HEAD_VS_INDEX.equals(mode) ? GitCommitPanel.FILTER_HEAD_VS_INDEX : GitCommitPanel.FILTER_HEAD_VS_WORKING).setSelected(true);
        return Arrays.<VCSCommitFilter>asList(FILTER_HEAD_VS_INDEX, FILTER_HEAD_VS_WORKING);
    }
    
    @Override
    public GitCommitParameters getParameters() {
        return (GitCommitParameters) super.getParameters();
    }

    public Collection<GitHook> getHooks() {
        return hooks;
    }

    @Override
    protected void computeNodes() {      
        computeNodesIntern();
    }

    @Override
    public boolean open (VCSContext context, HelpCtx helpCtx, String title) {
        // synchronize access to this static field
        assert EventQueue.isDispatchThread();
        boolean ok = super.open(context, helpCtx, title);
        GitProgressSupport supp = support;
        if (supp != null) {
            supp.cancel();
        }
        if (ok && !fromGitView) {
            GitModuleConfig.getDefault().setLastUsedCommitViewMode(getSelectedFilter() == GitCommitPanel.FILTER_HEAD_VS_INDEX ? Mode.HEAD_VS_INDEX : Mode.HEAD_VS_WORKING_TREE);
        }
        diffProvider.componentClosed();
        return ok;
    }
    
    /** used by unit tests */
    GitProgressSupport support;
    RequestProcessor.Task computeNodesIntern() {      
        final boolean refreshFinnished[] = new boolean[] { false };
        RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);

        GitProgressSupport supp = this.support;
        if (supp != null) {
            supp.cancel();
        }
        support = getProgressSupport(refreshFinnished);
        final String preparingMessage = NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit"); //NOI18N
        setupProgress(preparingMessage, support.getProgressComponent());
        if (diffProvider.isOpen()) {
            diffProvider.refreshFiles(new GitFileNode[0], getAcceptedMode(getSelectedFilter()));
        }
        Task task = support.start(rp, repository, preparingMessage);
        
        // do not show progress in dialog if task finnished early        
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!(refreshFinnished[0])) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showProgress();                            
                        }
                    });                     
                }
            }
        }, 1000);
        return task;
    }

    // merge-type commit dialog can hook into this method
    protected GitProgressSupport getProgressSupport (final boolean[] refreshFinished) {
        return new GitCommitDialogProgressSupport(refreshFinished, getSelectedFilter());
    }

    private class GitCommitDialogProgressSupport extends GitProgressSupport {

        private final boolean[] refreshFinished;
        private final VCSCommitFilter filter;

        public GitCommitDialogProgressSupport (boolean[] refreshFinished, VCSCommitFilter filter) {
            this.refreshFinished = refreshFinished;
            this.filter = filter;
        }

        @Override
        public void perform() {
            try {
                loadFiles();
                if (RepositoryInfo.getInstance(repository).getActiveBranch() != GitBranch.NO_BRANCH_INSTANCE) {
                    loadHeadLogMessage();
                }
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
            } finally {
                refreshFinished[0] = true;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stopProgress();
                    }
                });
            }
        }

        private void loadHeadLogMessage() throws IllegalArgumentException, GitException {
            GitRevisionInfo gitRevisionInfo = getClient().log(GitUtils.HEAD, getProgressMonitor());
            final String headCommitMessage = gitRevisionInfo.getFullMessage();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getParameters().getPanel().setHeadCommitMessage(headCommitMessage);
                }
            });
        }

        private boolean loadFiles() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getCommitTable().setNodes(new GitLocalFileNode[0]);
                }
            });
            // Ensure that cache is uptodate
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Arrays.asList(roots)), getProgressMonitor());
            // the realy time consuming part is over;
            // no need to show the progress component,
            // which only makes the dialog flicker
            refreshFinished[0] = true;
            File[][] split = Utils.splitFlatOthers(roots);
            List<File> fileList = new ArrayList<>();
            EnumSet<Status> acceptedStatus = getAcceptedStatus(filter);
            for (int c = 0; c < split.length; c++) {
                File[] splitRoots = split[c];
                boolean recursive = c == 1;
                if (recursive) {
                    File[] files = cache.listFiles(splitRoots, acceptedStatus);
                    for (int i = 0; i < files.length; i++) {
                        for (int r = 0; r < splitRoots.length; r++) {
                            if (Utils.isAncestorOrEqual(splitRoots[r], files[i])) {
                                if (!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }
                } else {
                    File[] files = GitUtils.flatten(splitRoots, acceptedStatus);
                    for (int i = 0; i < files.length; i++) {
                        if (!fileList.contains(files[i])) {
                            fileList.add(files[i]);
                        }
                    }
                }
            }
            if (fileList.isEmpty()) {
                return true;
            }
            List<GitLocalFileNode> nodesList = new ArrayList<>(fileList.size());
            for (File file : fileList) {
                GitLocalFileNode node = new GitLocalFileNode(repository, file, getAcceptedMode(filter));
                nodesList.add(node);
            }
            final GitLocalFileNode[] nodes = nodesList.toArray(new GitLocalFileNode[0]);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getCommitTable().setNodes(nodes);
                    if (diffProvider.isOpen()) {
                        diffProvider.refreshFiles(nodes, getAcceptedMode(filter));
                    }
                }
            });
            return false;
        }
    }
    
    private static EnumSet<Status> getAcceptedStatus (VCSCommitFilter f) {
        if(f == FILTER_HEAD_VS_INDEX) {
            return FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX;
        } else if(f == FILTER_HEAD_VS_WORKING) {
            return FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING;                
        }         
        throw new IllegalStateException("wrong filter " + (f != null ? f.getID() : "NULL"));    // NOI18N        
    }
    
    private static FileInformation.Mode getAcceptedMode (VCSCommitFilter f) {
        if (f == FILTER_HEAD_VS_INDEX) {
            return Mode.HEAD_VS_INDEX;
        } else if (f == FILTER_HEAD_VS_WORKING) {
            return Mode.HEAD_VS_WORKING_TREE;
        }
        throw new IllegalStateException("wrong filter " + (f != null ? f.getID() : "NULL")); //NOI18N
    }

    private static class DiffProvider extends VCSCommitDiffProvider<GitFileNode> {

        private MultiDiffPanelController controller;
        private GitFileNode[] files;
        private Mode mode;

        @Override
        public Set<File> getModifiedFiles () {
            return getSaveCookiesPerFile().keySet();
        }

        private Map<File, SaveCookie> getSaveCookiesPerFile () {
            Map<File, SaveCookie> modifiedFiles = new HashMap<>();
            if (controller != null) {
                Map<File, SaveCookie> cookies = controller.getSaveCookies(false);
                modifiedFiles.putAll(cookies);
            }
            return modifiedFiles;
        }

        @Override
        public JComponent createDiffComponent (File file) {
            componentClosed();
            controller = new MultiDiffPanelController(new GitFileNode[0], mode);
            return controller.getPanel();
        }

        @Override
        protected JComponent getDiffComponent (GitFileNode[] files) {
            return getDiffComponent(files, mode);
        }

        private JComponent getDiffComponent (GitFileNode[] files, Mode mode) {
            if (controller != null && (!Arrays.equals(this.files, files) || mode != this.mode)) {
                beforeFilesChanged();
                this.files = Arrays.copyOf(files, files.length);
                controller.changeFiles(files, mode);
            }
            setMode(mode);
            if (controller == null) {
                this.files = Arrays.copyOf(files, files.length);
                controller = new MultiDiffPanelController(files, mode);
            }
            return controller.getPanel();
        }

        @Override
        protected void selectFile (File file) {
            if (controller != null) {
                controller.selectFile(file);
            }
        }

        private void setMode (Mode mode) {
            this.mode = mode;
        }
        
        private void refreshFiles (GitFileNode[] files, Mode mode) {
            getDiffComponent(files, mode);
        }

        private void componentClosed () {
            if (controller != null) {
                controller.componentClosed();
                controller = null;
            }
        }

        private void beforeFilesChanged () {
            if (controller != null) {
                SaveCookie[] saveCookies = getSaveCookies();
                if (saveCookies.length > 0 && SaveBeforeClosingDiffConfirmation.allSaved(saveCookies)) {
                    EditorCookie[] editorCookies = getEditorCookies();
                    for (EditorCookie cookie : editorCookies) {
                        cookie.open();
                    }
                }
            }
        }

        private boolean isOpen () {
            return controller != null;
        }

        /**
         * Returns save cookies available for files in the commit table
         * @return
         */
        @Override
        protected SaveCookie[] getSaveCookies () {
            return getSaveCookiesPerFile().values().toArray(new SaveCookie[0]);
        }

        /**
         * Returns editor cookies available for modified and not open files in the commit table
         * @return
         */
        @Override
        protected EditorCookie[] getEditorCookies () {
            LinkedList<EditorCookie> allCookies = new LinkedList<EditorCookie>();
            if (controller != null) {
                EditorCookie[] cookies = controller.getEditorCookies(true);
                if (cookies.length > 0) {
                    allCookies.add(cookies[0]);
                }
            }
            return allCookies.toArray(new EditorCookie[0]);
        }        
    }    
    
    private static class GitCommitFilter extends VCSCommitFilter {
        private final Icon icon;
        private final String tooltip;
        private final String id;

        GitCommitFilter(String id, Icon icon, String tooltip, boolean selected) {
            super(selected);
            this.icon = icon;
            this.tooltip = tooltip;
            this.id = id;
        }
        
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public void setSelected (boolean selected) {
            super.setSelected(selected);
        }
        
    }
// <editor-fold defaultstate="collapsed" desc="dialog for merged repository">
    static class GitCommitPanelMerged extends GitCommitPanel {

        private final File repository;
        private final DiffProvider diffProvider;

        static GitCommitPanel create(File[] roots, File repository, GitUser user, String mergeCommitMessage) {
            Preferences preferences = GitModuleConfig.getDefault().getPreferences();
            String lastCanceledCommitMessage = GitModuleConfig.getDefault().getLastCanceledCommitMessage();

            DefaultCommitParameters parameters = new GitCommitParameters(preferences, 
                    mergeCommitMessage == null ? lastCanceledCommitMessage : mergeCommitMessage,
                    mergeCommitMessage != null, user);

            Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
            GitHookContext hooksCtx = new GitHookContext(roots, null, new GitHookContext.LogEntry[]{});

            DiffProvider diffProvider = new DiffProvider();

            return new GitCommitPanelMerged(new GitCommitTable(false, true), roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider);
        }

        private GitCommitPanelMerged(GitCommitTable gitCommitTable, File[] roots, File repository, DefaultCommitParameters parameters,
                Preferences preferences, Collection<GitHook> hooks, GitHookContext hooksCtx, DiffProvider diffProvider) {
            super(gitCommitTable, roots, repository, parameters, preferences, hooks, hooksCtx, diffProvider, true, createFilters());
            this.repository = repository;
            this.diffProvider = diffProvider;
        }

        private static List<VCSCommitFilter> createFilters() {
            assert EventQueue.isDispatchThread();
            disableFilters();
            GitCommitPanel.FILTER_HEAD_VS_WORKING.setSelected(true);
            return Arrays.<VCSCommitFilter>asList(FILTER_HEAD_VS_INDEX, FILTER_HEAD_VS_WORKING);
        }

        @Override
        protected GitProgressSupport getProgressSupport(boolean[] refreshFinnished) {
            return new MergedCommitDialogProgressSupport(refreshFinnished, getSelectedFilter());
        }

        private class MergedCommitDialogProgressSupport extends GitProgressSupport {

            private final boolean refreshFinished[];
            private final VCSCommitFilter filter;

            MergedCommitDialogProgressSupport(boolean[] refreshFinished, VCSCommitFilter filter) {
                this.refreshFinished = refreshFinished;
                this.filter = filter;
            }

            @Override
            public void perform() {
                try {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            getCommitTable().setNodes(new GitLocalFileNode[0]);
                        }
                    });
                    // get list of modifications
                    File[] files;
                    try {
                        files = getClient().listModifiedIndexEntries(new File[] { repository }, getProgressMonitor());
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                        return;
                    }
                    FileStatusCache cache = Git.getInstance().getFileStatusCache();
                    if (isCanceled()) {
                        return;
                    }
                    cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Arrays.asList(files)), getProgressMonitor());
                    if (isCanceled()) {
                        return;
                    }

                    // the realy time consuming part is over;
                    // no need to show the progress component,
                    // which only makes the dialog flicker
                    refreshFinished[0] = true;
                    EnumSet<Status> statuses;
                    final Mode acceptedMode = getAcceptedMode(filter);
                    if (acceptedMode == Mode.HEAD_VS_INDEX) {
                        statuses = FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX;
                    } else {
                        statuses = FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING;
                    }
                    files = cache.listFiles(new File[]{repository}, statuses);
                    if (files.length == 0) {
                        return;
                    }

                    ArrayList<GitLocalFileNode> nodesList = new ArrayList<GitLocalFileNode>(files.length);

                    for (File file : files) {
                        GitLocalFileNode node = new GitFileNode.GitMergeFileNode(repository, file, acceptedMode);
                        nodesList.add(node);
                    }
                    final GitLocalFileNode[] nodes = nodesList.toArray(new GitLocalFileNode[files.length]);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            getCommitTable().setNodes(nodes);
                            if (diffProvider.isOpen()) {
                                diffProvider.refreshFiles(nodes, acceptedMode);
                            }
                        }
                    });
                } finally {
                    refreshFinished[0] = true;
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            stopProgress();
                        }
                    });
                }
            }
        }

        @Override
        public void setErrorLabel (String htmlErrorLabel) {
            if (htmlErrorLabel == null || htmlErrorLabel.isEmpty()) {
                htmlErrorLabel = NbBundle.getMessage(GitCommitPanel.class, "MSG_CommitPanel.afterMerge"); //NOI18N
            }
            super.setErrorLabel(htmlErrorLabel);
        }

    }// </editor-fold>
}
