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
package org.netbeans.modules.mercurial.remote;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.ui.log.LogAction;
import org.netbeans.modules.mercurial.remote.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.history.HistoryActionVCSProxyBased;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
public class HgHistoryProvider implements VCSHistoryProvider {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //NOI18N
    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<>();
    private Action[] actions;

    private static final Logger LOG = Logger.getLogger(HgHistoryProvider.class.getName());
    
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
        
        logFiles("retrieving history for files: ", files); //NOI18N
        long t = System.currentTimeMillis();
        if(files == null || files.length == 0) {
            return null;
        }
        
        try {
            if(!isClientAvailable(files[0])) {
                LOG.log(Level.WARNING, "Remote ''{0}'' Mercurial client is unavailable", VCSFileProxySupport.getFileSystem(files[0]));
                return null;
            }
            Set<VCSFileProxy> repositories = getRepositoryRoots(files);
            if(repositories == null) {
                return null;
            }

            List<HistoryEntry> ret = new LinkedList<>();
            Map<String, Set<VCSFileProxy>> rev2FileMap = new HashMap<>();
            Map<String, HgLogMessage> rev2LMMap = new HashMap<>();

            String fromRevision;
            String toRevision;
            if(fromDate == null) {
                fromRevision = "0"; //NOI18N
                toRevision = "BASE"; //NOI18N
            } else {
                fromRevision = dateFormat.format(fromDate);
                toRevision = dateFormat.format(new Date(System.currentTimeMillis()));
            }

            VCSFileProxy repositoryRoot = repositories.iterator().next();
            for (VCSFileProxy file : files) {
                FileInformation info = Mercurial.getInstance().getFileStatusCache().refresh(file);
                int status = info.getStatus();
                if ((status & FileInformation.STATUS_VERSIONED) == 0) {
                    continue;
                }
                HgLogMessage[] history = HistoryRegistry.getInstance().getLogs(repositoryRoot, files, fromRevision, toRevision);
                for (HgLogMessage h : history) {
                    String r = h.getHgRevision().getRevisionNumber();
                    rev2LMMap.put(r, h);
                    Set<VCSFileProxy> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            }    

            for(HgLogMessage h : rev2LMMap.values()) {
                Set<VCSFileProxy> s = rev2FileMap.get(h.getHgRevision().getRevisionNumber());
                VCSFileProxy[] involvedFiles = s.toArray(new VCSFileProxy[s.size()]);
                ret.add(createHistoryEntry(h, repositoryRoot, involvedFiles));
            }
            return ret.toArray(new HistoryEntry[ret.size()]);
        } finally {
            LOG.log(Level.FINE, "retrieving history took {0}", (System.currentTimeMillis() - t));
        }
    }

    @Override
    public Action createShowHistoryAction(VCSFileProxy[] files) {
        return new OpenHistoryAction(files);
    }
    
    public void fireHistoryChange(final VCSFileProxy[] files) {
        final HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[listeners.size()]);
        }
        Mercurial.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (HistoryChangeListener l : la) {
                    l.fireHistoryChanged(new HistoryEvent(HgHistoryProvider.this, files));
                }
            }
        });
    }

    private void logFiles(String msg, VCSFileProxy[] files) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        for (int i = 0; i < files.length; i++) {
            VCSFileProxy f = files[i];
            if(f == null) {
                continue;
            }
            sb.append(f.getPath());
            if(i < files.length - 1) {
                sb.append(","); // NOI18N
            }
        }
        LOG.fine(sb.toString());
    }

    private static class RevisionProviderImpl implements RevisionProvider {
        private final HgRevision hgRevision;

        public RevisionProviderImpl(HgRevision hgRevision) {
            this.hgRevision = hgRevision;
        }
        
        @Override
        public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
            
            if(!isClientAvailable(originalFile)) {
                LOG.log(Level.WARNING, "Remote ''{0}''  Mercurial client is unavailable", VCSFileProxySupport.getFileSystem(originalFile));
                return;
            }

            try {
                FileInformation info = Mercurial.getInstance().getFileStatusCache().refresh(originalFile);
                if (info != null && (info.getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) != 0
                        && info.getStatus(null) != null && info.getStatus(null).getOriginalFile() != null) 
                {
                    originalFile = info.getStatus(null).getOriginalFile();
                }
                
                Set<VCSFileProxy> repositories = getRepositoryRoots(originalFile);
                if(repositories == null || repositories.isEmpty()) {
                    LOG.log(Level.WARNING, "Repository root not found for file {0}", originalFile);
                    return;
                }
                VCSFileProxy repository = repositories.iterator().next();
                VCSFileProxy historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, hgRevision.getChangesetId(), true);
                if(historyFile != null) {
                    // ah! we already now the file was moved in the history,
                    // so lets look for contents by using its previous name
                    originalFile= historyFile;
                }
                VCSFileProxy file = VersionsCache.getInstance().getFileRevision(originalFile, hgRevision, false);
                if(file != null) {
                    VCSFileProxySupport.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                } else if(historyFile == null) {
                    // well then, lets try to find out if the file was move at some point in the history
                    LOG.log(Level.WARNING, "File {0} not found in revision {1}. Will make a guess ...", new Object[]{originalFile, hgRevision});
                    historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, hgRevision.getChangesetId(), false);
                    if(historyFile != null) {
                        file = VersionsCache.getInstance().getFileRevision(historyFile, hgRevision, false);
                        if(file != null) {
                            VCSFileProxySupport.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                        }
                    }
                }
            } catch (IOException e) {
                if(e.getCause() instanceof HgException.HgCommandCanceledException)  {
                    LOG.log(Level.FINE, null, e);
                } else {
                    LOG.log(Level.WARNING, null, e);
                }
            }        
        }
    }

    private class ParentProviderImpl implements ParentProvider {
        private final HgLogMessage logMessage;
        private final VCSFileProxy[] files;
        private final VCSFileProxy repository;

        public ParentProviderImpl(HgLogMessage logMessage, VCSFileProxy[] files, VCSFileProxy repository) {
            this.logMessage = logMessage;
            this.files = files;
            this.repository = repository;
        }

        @Override
        public HistoryEntry getParentEntry(VCSFileProxy file) {
            HgRevision ancestor = logMessage.getAncestor(file);
            if (ancestor.equals(HgRevision.EMPTY)) {
                VCSFileProxy originalFile = HistoryRegistry.getInstance().getHistoryFile(repository, file, logMessage.getCSetShortID(), false);
                if (originalFile != null) {
                    ancestor = logMessage.getAncestor(originalFile);
                }
            }
            if (ancestor.equals(HgRevision.EMPTY)) {
                return null;
            }
            HgLogMessage history = HistoryRegistry.getInstance().getLog(repository, file, ancestor.getChangesetId());
            if(history == null) {
                return null;
            }
            
            return createHistoryEntry(history, repository, files);
        }
    }
    
    private HistoryEntry createHistoryEntry(HgLogMessage h, VCSFileProxy repository, VCSFileProxy[] files) {
        String username = h.getUsername();
        String author = h.getAuthor();
        if(username == null || "".equals(username.trim())) { // NOI18N
            username = author;
        }
        return new HistoryEntry(
                files, 
                h.getDate(), 
                h.getMessage(), 
                author, 
                username, 
                h.getHgRevision().getRevisionNumber() + ":" + h.getHgRevision().getChangesetId(), // NOI18N
                h.getHgRevision().getRevisionNumber(), 
                getActions(), 
                new RevisionProviderImpl(h.getHgRevision()),
                null,
                new ParentProviderImpl(h, files, repository));
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
            if(!isClientAvailable(files[0])) {
                LOG.log(Level.WARNING, "Remote ''{0}'' Mercurial client is unavailable", VCSFileProxySupport.getFileSystem(files[0])); // NOI18N
                return;
            }

            Set<VCSFileProxy> repositories = getRepositoryRoots(files);
            if(repositories == null) {
                return;
            }
            LogAction.openHistory(repositories.iterator().next(), files);
        }
        
    }

    private synchronized Action[] getActions() {
        if(actions == null) {
            actions = new Action[] {
                new HistoryActionVCSProxyBased() {
                    @Override
                    protected void perform(final HistoryEntry entry, final Set<VCSFileProxy> files) {
                        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(files.iterator().next());
                        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                        HgProgressSupport support = new HgProgressSupport() {
                            @Override
                            public void perform() {
                                RevertModificationsAction.performRevert(
                                    root,   
                                    getHgRevision(entry).getRevisionNumber(),                           
                                    new LinkedList<>(files), 
                                    HgModuleConfig.getDefault(root).getBackupOnRevertModifications(), 
                                    false, 
                                    this.getLogger());
                            }
                        };
                        support.start(rp, root, NbBundle.getMessage(LogAction.class, "MSG_Revert_Progress")); // NOI18N
                    }    
                    @Override
                    protected boolean isMultipleHistory() {
                        return false;
                    }
                    @Override
                    public String getName() {
                        String rev = getRevisionShort();
                        if(rev == null) {
                            rev = ""; // NOI18N
                        }
                        return NbBundle.getMessage(LogAction.class, "CTL_SummaryView_RollbackTo", rev);
                    }

                    @Override
                    public HelpCtx getHelpCtx() {
                        return null;
                    }
                },
                new HistoryActionVCSProxyBased(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_View")) { // NOI18N
                    @Override
                    protected void perform(HistoryEntry entry, Set<VCSFileProxy> files) {
                        view(entry, false, files);
                    }
                    @Override
                    public HelpCtx getHelpCtx() {
                        return null;
                    }
                },
                new HistoryActionVCSProxyBased(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                    @Override
                    protected void perform(HistoryEntry entry, Set<VCSFileProxy> files) {
                        view(entry, true, files);
                    }
                    @Override
                    public HelpCtx getHelpCtx() {
                        return null;
                    }
                }
            };
        }
        return actions;
    }
    
    private void view(final HistoryEntry entry, final boolean showAnnotations, final Set<VCSFileProxy> files) {
        final VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(files.iterator().next());
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        rp.post(new Runnable() {
            @Override
            public void run() {
                for (VCSFileProxy f : files) {
                    try {
                        HgUtils.openInRevision(f, -1, getHgRevision(entry), showAnnotations);
                    } catch (IOException ex) {
                        // Ignore if file not available in cache
                    }
                }
            }
        });
    }
    
    private HgRevision getHgRevision(HistoryEntry entry) {
        String[] revs = entry.getRevision().split(":"); //NOI18N
        final HgRevision revision = new HgRevision(revs[1], revs[0]);
        return revision;
    }
    
    /**
     * Returns true if mercurial client is installed and has a supported version.<br/>
     * Does not show any warning dialog.
     * @return true if mercurial client is available.
     */
    private static boolean isClientAvailable(VCSFileProxy root) {
        return Mercurial.getInstance().isAvailable(root, true, false);
    }

    private static Set<VCSFileProxy> getRepositoryRoots(VCSFileProxy... files) {
        Set<VCSFileProxy> repositories = HgUtils.getRepositoryRoots(new HashSet<>(Arrays.asList(files)));
        if (repositories.size() != 1) {
            LOG.log(Level.WARNING, "History requested for {0} repositories", repositories.size()); // NOI18N
            return null;
        }
        return repositories;
    }
}

