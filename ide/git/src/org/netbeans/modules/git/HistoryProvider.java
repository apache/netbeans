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
package org.netbeans.modules.git;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.history.HistoryAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka, Ondra Vrabec
 */
public class HistoryProvider implements VCSHistoryProvider {
    
    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<VCSHistoryProvider.HistoryChangeListener>();
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
    public synchronized HistoryEntry[] getHistory(File[] files, Date fromDate) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        
        Set<File> repositories = getRepositoryRoots(files);
        if(repositories == null) {
            return null;
        }
        
        List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
        Map<String, Set<File>> rev2FileMap = new HashMap<String, Set<File>>();
        Map<String, GitRevisionInfo> rev2LMMap = new LinkedHashMap<String, GitRevisionInfo>();
            
        File repositoryRoot = repositories.iterator().next();
        for (File file : files) {
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
                    Set<File> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<File>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            } catch (GitException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }    

        for (GitRevisionInfo h : rev2LMMap.values()) {
            Set<File> s = rev2FileMap.get(h.getRevision());
            File[] involvedFiles = s.toArray(new File[0]);
            
            HistoryEntry e = createHistoryEntry(h, involvedFiles, repositoryRoot);
            ret.add(e);
        }
        return ret.toArray(new HistoryEntry[0]);
    }

    private HistoryEntry createHistoryEntry (GitRevisionInfo h, File[] involvedFiles, File repositoryRoot) {
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
    public Action createShowHistoryAction(File[] files) {
        return new OpenHistoryAction(files);
    }
    
    public void fireHistoryChange (final File[] files) {
        final HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[0]);
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

    private class RevisionProviderImpl implements RevisionProvider {
        private String revision;

        public RevisionProviderImpl(String revision) {
            this.revision = revision;
        }
        
        @Override
        public void getRevisionFile (File originalFile, File revisionFile) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

            try {
                FileInformation info = Git.getInstance().getFileStatusCache().getStatus(originalFile);
                if (info.containsStatus(FileInformation.Status.NEW_HEAD_INDEX) && info.getOldFile() != null) {
                    originalFile = info.getOldFile();
                }
                
                Set<File> repositories = getRepositoryRoots(originalFile);
                if(repositories == null || repositories.isEmpty()) {
                    LOG.log(Level.WARNING, "Repository root not found for file {0}", originalFile);
                    return;
                }
                File repository = repositories.iterator().next();
                File historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, revision, true);
                if(historyFile != null) {
                    // ah! we already know the file was moved in the history,
                    // so lets look for contents by using its previous name
                    originalFile = historyFile;
                }
                File file = VersionsCache.getInstance().getFileRevision(originalFile, revision, GitUtils.NULL_PROGRESS_MONITOR);
                if(file != null) {
                    FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                } else if(historyFile == null) {
                    // well then, lets try to find out if the file was move at some point in the history
                    LOG.log(Level.WARNING, "File {0} not found in revision {1}. Will make a guess ...", new Object[]{originalFile, revision});
                    historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, revision, false);
                    if(historyFile != null) {
                        file = VersionsCache.getInstance().getFileRevision(historyFile, revision, GitUtils.NULL_PROGRESS_MONITOR);
                        if(file != null) {
                            FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                        }
                    }
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }        
        }
    }

    private static class OpenHistoryAction extends AbstractAction {
        private final File[] files;

        public OpenHistoryAction(File[] files) {
            this.files = files;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            openHistory(files);
        }
        private void openHistory(File[] files) {
            if(files == null || files.length == 0) {
                return;
            }
            Set<File> repositories = getRepositoryRoots(files);
            if(repositories == null || repositories.isEmpty()) {
                return;
            }
            List<Node> nodes = new ArrayList<Node>(files.length);
            for (File f : files) {
                nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(f)) {
                    @Override
                    public String getDisplayName() {
                        return getLookup().lookup(File.class).getName();
                    }
                });
            }
            SearchHistoryAction.openSearch(repositories.iterator().next(), files, Utils.getContextDisplayName(VCSContext.forNodes(nodes.toArray(new Node[0]))));
        }
        
    }

    @NbBundle.Messages({
        "# {0} - commit id", "HistoryProvider.action.RevertTo=Revert to {0}",
        "HistoryProvider.action.RevertTo.progress=Reverting Files"
    })
    private synchronized Action[] getActions() {
        if(actions == null) {
            actions = new Action[] {
                new HistoryAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_View")) {
                    @Override
                    protected void perform(HistoryEntry entry, Set<File> files) {
                        view(entry.getRevision(), false, files);
                    }
                },
                new HistoryAction(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_ShowAnnotations")) {
                    @Override
                    protected void perform(HistoryEntry entry, Set<File> files) {
                        view(entry.getRevision(), true, files);
                    }
                },
                new HistoryAction() {
                    @Override
                    protected void perform(final HistoryEntry entry, final Set<File> files) {
                        File root = Git.getInstance().getRepositoryRoot(files.iterator().next());
                        SystemAction.get(RevertChangesAction.class).revertFiles(root, files.toArray(new File[0]),
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

                },
            };
        }
        return actions;
    }
    
    private void view(final String revision, final boolean showAnnotations, final Set<File> files) {
        final File root = Git.getInstance().getRepositoryRoot(files.iterator().next());
        new GitProgressSupport() {
            @Override
            protected void perform () {
                for (File f : files) {
                    File original = HistoryRegistry.getInstance().getHistoryFile(root, f, revision, true);
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
    
    private static Set<File> getRepositoryRoots(File... files) {
        Set<File> repositories = GitUtils.getRepositoryRoots(new HashSet<File>(Arrays.asList(files)));
        if (repositories.size() != 1) {
            LOG.log(Level.WARNING, "History requested for {0} repositories", repositories.size()); // NOI18N
            return null;
        }
        return repositories;
    }
    
    private class ParentProviderImpl implements ParentProvider {
        private GitRevisionInfo info;
        private File[] files;
        private File repository;
        private Map<File, HistoryEntry> commonAncestors;

        public ParentProviderImpl (GitRevisionInfo info, File[] files, File repository) {
            this.info = info;
            this.files = files;
            this.repository = repository;
            this.commonAncestors = new HashMap<File, HistoryEntry>(files.length);
        }

        @Override
        public HistoryEntry getParentEntry (File file) {
            HistoryEntry ancestorEntry = commonAncestors.get(file);
            if (ancestorEntry == null && !commonAncestors.containsKey(file)) {
                GitRevisionInfo parent = null;
                GitClient client = null;
                try {
                    client = Git.getInstance().getClient(repository);
                    if (info.getParents().length == 1) {
                        File historyFile = info.getModifiedFiles().containsKey(file)
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

