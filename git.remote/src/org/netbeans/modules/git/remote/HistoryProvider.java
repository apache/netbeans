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
package org.netbeans.modules.git.remote;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitUser;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.history.HistoryActionVCSProxyBased;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class HistoryProvider implements VCSHistoryProvider {
    
    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<>();
    private static final Logger LOG = Logger.getLogger(HistoryProvider.class.getName());
    private Action[] actions;

    @Override
    public void addHistoryChangeListener(VCSHistoryProvider.HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeHistoryChangeListener(VCSHistoryProvider.HistoryChangeListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    
    @Override
    public synchronized HistoryEntry[] getHistory(VCSFileProxy[] files, Date fromDate) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        
        Set<VCSFileProxy> repositories = getRepositoryRoots(files);
        if(repositories == null) {
            return null;
        }
        
        List<HistoryEntry> ret = new LinkedList<>();
        Map<String, Set<VCSFileProxy>> rev2FileMap = new HashMap<>();
        Map<String, GitRevisionInfo> rev2LMMap = new LinkedHashMap<>();
            
        VCSFileProxy repositoryRoot = repositories.iterator().next();
        for (VCSFileProxy file : files) {
            FileInformation info = Git.getInstance().getFileStatusCache().getStatus(file);
            if (!info.containsStatus(FileInformation.STATUS_MANAGED)) {
                continue;
            }
            GitRevisionInfo[] history;
            try {
                history = HistoryRegistry.getInstance().getLogs(repositoryRoot, files, fromDate, null, GitUtils.NULL_PROGRESS_MONITOR);
                for (GitRevisionInfo h : history) {
                    String r = h.getRevision();
                    rev2LMMap.put(r, h);
                    Set<VCSFileProxy> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            } catch (GitException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }    

        for (GitRevisionInfo h : rev2LMMap.values()) {
            Set<VCSFileProxy> s = rev2FileMap.get(h.getRevision());
            VCSFileProxy[] involvedFiles = s.toArray(new VCSFileProxy[s.size()]);
            
            HistoryEntry e = createHistoryEntry(h, involvedFiles, repositoryRoot);
            ret.add(e);
        }
        return ret.toArray(new HistoryEntry[ret.size()]);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("RCN") // assert in release mode does not guarantee that "message != null"
    private HistoryEntry createHistoryEntry (GitRevisionInfo h, VCSFileProxy[] involvedFiles, VCSFileProxy repositoryRoot) {
        GitUser user = h.getAuthor();
        if (user == null) {
            user = h.getCommitter();
        }
        String username = user.getName();
        String author = user.toString();
        String message = h.getFullMessage();
        assert message != null;
        HistoryEntry e = new HistoryEntry(
                involvedFiles, 
                new Date(h.getCommitTime()),
                message == null ? "" : message,  //NOI18N
                author, 
                username, 
                h.getRevision(), 
                h.getRevision().length() > 7 ? h.getRevision().substring(0, 7) : h.getRevision(), 
                getActions(), 
                new RevisionProviderImpl(h.getRevision()),
                null,
                new ParentProviderImpl(h, involvedFiles, repositoryRoot));
        return e;
    }

    @Override
    public Action createShowHistoryAction(VCSFileProxy[] files) {
        return new OpenHistoryAction(files);
    }
    
    public void fireHistoryChange (final VCSFileProxy[] files) {
        final HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[listeners.size()]);
        }
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (HistoryChangeListener l : la) {
                    l.fireHistoryChanged(new HistoryEvent(HistoryProvider.this, files));
                }
            }
        });
    }

    private static class RevisionProviderImpl implements RevisionProvider {
        private final String revision;

        public RevisionProviderImpl(String revision) {
            this.revision = revision;
        }
        
        @Override
        public void getRevisionFile (VCSFileProxy originalFile, VCSFileProxy revisionFile) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

            try {
                FileInformation info = Git.getInstance().getFileStatusCache().getStatus(originalFile);
                if (info.containsStatus(FileInformation.Status.NEW_HEAD_INDEX) && info.getOldFile() != null) {
                    originalFile = info.getOldFile();
                }
                
                Set<VCSFileProxy> repositories = getRepositoryRoots(originalFile);
                if(repositories == null || repositories.isEmpty()) {
                    LOG.log(Level.WARNING, "Repository root not found for file {0}", originalFile);
                    return;
                }
                VCSFileProxy repository = repositories.iterator().next();
                VCSFileProxy historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, revision, true);
                if(historyFile != null) {
                    // ah! we already know the file was moved in the history,
                    // so lets look for contents by using its previous name
                    originalFile = historyFile;
                }
                VCSFileProxy file = VersionsCache.getInstance().getFileRevision(originalFile, revision, GitUtils.NULL_PROGRESS_MONITOR);
                if(file != null) {
                    VCSFileProxySupport.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                } else if(historyFile == null) {
                    // well then, lets try to find out if the file was move at some point in the history
                    LOG.log(Level.WARNING, "File {0} not found in revision {1}. Will make a guess ...", new Object[]{originalFile, revision});
                    historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, revision, false);
                    if(historyFile != null) {
                        file = VersionsCache.getInstance().getFileRevision(historyFile, revision, GitUtils.NULL_PROGRESS_MONITOR);
                        if(file != null) {
                            VCSFileProxySupport.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                        }
                    }
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }        
        }
    }

    private static class OpenHistoryAction extends AbstractAction {
        private final VCSFileProxy[] files;

        public OpenHistoryAction(VCSFileProxy[] files) {
            this.files = files;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            openHistory(files);
        }
        private void openHistory(VCSFileProxy[] files) {
            if(files == null || files.length == 0) {
                return;
            }
            Set<VCSFileProxy> repositories = getRepositoryRoots(files);
            if(repositories == null || repositories.isEmpty()) {
                return;
            }
            List<Node> nodes = new ArrayList<>(files.length);
            for (VCSFileProxy f : files) {
                nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(f)) {
                    @Override
                    public String getDisplayName() {
                        return getLookup().lookup(VCSFileProxy.class).getName();
                    }
                });
            }
            SearchHistoryAction.openSearch(repositories.iterator().next(), files, VCSFileProxySupport.getContextDisplayName(VCSContext.forNodes(nodes.toArray(new Node[nodes.size()]))));
        }
        
    }

    @NbBundle.Messages({
        "# {0} - commit id", "HistoryProvider.action.RevertTo=Revert to {0}",
        "HistoryProvider.action.RevertTo.progress=Reverting Files"
    })
    private synchronized Action[] getActions() {
        if(actions == null) {
            actions = new Action[] {
                new HistoryActionVCSProxyBased(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_View")) {
                    @Override
                    protected void perform(HistoryEntry entry, Set<VCSFileProxy> files) {
                        view(entry.getRevision(), false, files);
                    }
                    @Override
                    public HelpCtx getHelpCtx() {
                        return null;
                    }
                },
                new HistoryActionVCSProxyBased(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_ShowAnnotations")) {
                    @Override
                    protected void perform(HistoryEntry entry, Set<VCSFileProxy> files) {
                        view(entry.getRevision(), true, files);
                    }
                    @Override
                    public HelpCtx getHelpCtx() {
                        return null;
                    }
                },
                new HistoryActionVCSProxyBased() {
                    @Override
                    protected void perform(final HistoryEntry entry, final Set<VCSFileProxy> files) {
                        VCSFileProxy root = Git.getInstance().getRepositoryRoot(files.iterator().next());
                        SystemAction.get(RevertChangesAction.class).revertFiles(root, files.toArray(new VCSFileProxy[files.size()]),
                                getRevisionShort(), Bundle.HistoryProvider_action_RevertTo_progress());
                    }    
                    @Override
                    protected boolean isMultipleHistory() {
                        return false;
                    }
                    @Override
                    public String getName() {
                        String rev = getRevisionShort();
                        if (rev == null) {
                            rev = ""; // NOI18N
                        }
                        return Bundle.HistoryProvider_action_RevertTo(rev);
                    }

                    @Override
                    public boolean isEnabled () {
                        return null != getRevisionShort();
                    }
                    @Override
                    public HelpCtx getHelpCtx() {
                        return null;
                    }
                },
            };
        }
        return actions;
    }
    
    private void view(final String revision, final boolean showAnnotations, final Set<VCSFileProxy> files) {
        final VCSFileProxy root = Git.getInstance().getRepositoryRoot(files.iterator().next());
        new GitProgressSupport() {
            @Override
            protected void perform () {
                for (VCSFileProxy f : files) {
                    VCSFileProxy original = HistoryRegistry.getInstance().getHistoryFile(root, f, revision, true);
                    if (original != null) {
                        f = original;
                    }
                    try {
                        GitUtils.openInRevision(f, -1, revision, showAnnotations, getProgressMonitor());
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        }.start(Git.getInstance().getRequestProcessor(), root, NbBundle.getMessage(SearchHistoryAction.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
    }
    
    private static Set<VCSFileProxy> getRepositoryRoots(VCSFileProxy... files) {
        Set<VCSFileProxy> repositories = GitUtils.getRepositoryRoots(new HashSet<>(Arrays.asList(files)));
        if (repositories.size() != 1) {
            LOG.log(Level.WARNING, "History requested for {0} repositories", repositories.size()); // NOI18N
            return null;
        }
        return repositories;
    }
    
    private class ParentProviderImpl implements ParentProvider {
        private final GitRevisionInfo info;
        private final VCSFileProxy[] files;
        private final VCSFileProxy repository;
        private final Map<VCSFileProxy, HistoryEntry> commonAncestors;

        public ParentProviderImpl (GitRevisionInfo info, VCSFileProxy[] files, VCSFileProxy repository) {
            this.info = info;
            this.files = files;
            this.repository = repository;
            this.commonAncestors = new HashMap<>(files.length);
        }

        @Override
        public HistoryEntry getParentEntry (VCSFileProxy file) {
            HistoryEntry ancestorEntry = commonAncestors.get(file);
            if (ancestorEntry == null && !commonAncestors.containsKey(file)) {
                GitRevisionInfo parent = null;
                GitClient client = null;
                try {
                    client = Git.getInstance().getClient(repository);
                    if (info.getParents().length == 1) {
                        VCSFileProxy historyFile = info.getModifiedFiles().containsKey(file)
                                ? file
                                : HistoryRegistry.getInstance().getHistoryFile(repository, file, info.getRevision(), false);
                        if (historyFile != null) {
                            parent = client.getPreviousRevision(historyFile, info.getRevision(), GitUtils.NULL_PROGRESS_MONITOR);
                        }
                    } else if (info.getParents().length > 1) {
                        parent = client.getCommonAncestor(info.getParents(), GitUtils.NULL_PROGRESS_MONITOR);
                    }
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                } finally {
                    if (client != null) {
                        client.release();
                    }
                }
                ancestorEntry = parent == null ? null : createHistoryEntry(parent, files, repository);
                commonAncestors.put(file, ancestorEntry);
            }
            return ancestorEntry;
        }
    }
}

