/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.log.LogAction;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.history.HistoryAction;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class HgHistoryProvider implements VCSHistoryProvider {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<VCSHistoryProvider.HistoryChangeListener>();
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
    public synchronized HistoryEntry[] getHistory(File[] files, Date fromDate) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!"; 
        
        logFiles("retrieving history for files: ", files); // NOi18N
        long t = System.currentTimeMillis();
        
        try {
            if(!isClientAvailable()) {
                LOG.log(Level.WARNING, "Mercurial client is unavailable");
                return null;
            }

            Set<File> repositories = getRepositoryRoots(files);
            if(repositories == null) {
                return null;
            }

            List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
            Map<String, Set<File>> rev2FileMap = new HashMap<String, Set<File>>();
            Map<String, HgLogMessage> rev2LMMap = new HashMap<String, HgLogMessage>();

            String fromRevision;
            String toRevision;
            if(fromDate == null) {
                fromRevision = "0";
                toRevision = "BASE";
            } else {
                fromRevision = dateFormat.format(fromDate);
                toRevision = dateFormat.format(new Date(System.currentTimeMillis()));
            }

            File repositoryRoot = repositories.iterator().next();
            for (File file : files) {
                FileInformation info = Mercurial.getInstance().getFileStatusCache().refresh(file);
                int status = info.getStatus();
                if ((status & FileInformation.STATUS_VERSIONED) == 0) {
                    continue;
                }
                HgLogMessage[] history = HistoryRegistry.getInstance().getLogs(repositoryRoot, files, fromRevision, toRevision);
                for (HgLogMessage h : history) {
                    String r = h.getHgRevision().getRevisionNumber();
                    rev2LMMap.put(r, h);
                    Set<File> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<File>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            }    

            for(HgLogMessage h : rev2LMMap.values()) {
                Set<File> s = rev2FileMap.get(h.getHgRevision().getRevisionNumber());
                File[] involvedFiles = s.toArray(new File[s.size()]);
                ret.add(createHistoryEntry(h, repositoryRoot, involvedFiles));
            }
            return ret.toArray(new HistoryEntry[ret.size()]);
        } finally {
            LOG.log(Level.FINE, "retrieving history took {0}", (System.currentTimeMillis() - t));
        }
    }

    @Override
    public Action createShowHistoryAction(File[] files) {
        return new OpenHistoryAction(files);
    }
    
    public void fireHistoryChange(final File[] files) {
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

    private void logFiles(String msg, File[] files) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if(f == null) {
                continue;
            }
            sb.append(f.getAbsolutePath());
            if(i < files.length - 1) {
                sb.append(","); // NOI18N
            }
        }
        LOG.fine(sb.toString());
    }

    private class RevisionProviderImpl implements RevisionProvider {
        private HgRevision hgRevision;

        public RevisionProviderImpl(HgRevision hgRevision) {
            this.hgRevision = hgRevision;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
            
            if(!isClientAvailable()) {
                LOG.log(Level.WARNING, "Mercurial client is unavailable");
                return;
            }

            try {
                FileInformation info = Mercurial.getInstance().getFileStatusCache().refresh(originalFile);
                if (info != null && (info.getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) != 0
                        && info.getStatus(null) != null && info.getStatus(null).getOriginalFile() != null) 
                {
                    originalFile = info.getStatus(null).getOriginalFile();
                }
                
                Set<File> repositories = getRepositoryRoots(originalFile);
                if(repositories == null || repositories.isEmpty()) {
                    LOG.log(Level.WARNING, "Repository root not found for file {0}", originalFile);
                    return;
                }
                File repository = repositories.iterator().next();
                File historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, hgRevision.getChangesetId(), true);
                if(historyFile != null) {
                    // ah! we already now the file was moved in the history,
                    // so lets look for contents by using its previous name
                    originalFile= historyFile;
                }
                File file = VersionsCache.getInstance().getFileRevision(originalFile, hgRevision, false);
                if(file != null) {
                    FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                } else if(historyFile == null) {
                    // well then, lets try to find out if the file was move at some point in the history
                    LOG.log(Level.WARNING, "File {0} not found in revision {1}. Will make a guess ...", new Object[]{originalFile, hgRevision});
                    historyFile = HistoryRegistry.getInstance().getHistoryFile(repository, originalFile, hgRevision.getChangesetId(), false);
                    if(historyFile != null) {
                        file = VersionsCache.getInstance().getFileRevision(historyFile, hgRevision, false);
                        if(file != null) {
                            FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
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
        private HgLogMessage logMessage;
        private File[] files;
        private File repository;

        public ParentProviderImpl(HgLogMessage logMessage, File[] files, File repository) {
            this.logMessage = logMessage;
            this.files = files;
            this.repository = repository;
        }

        @Override
        public HistoryEntry getParentEntry(File file) {
            HgRevision ancestor = logMessage.getAncestor(file);
            if (ancestor.equals(HgRevision.EMPTY)) {
                File originalFile = HistoryRegistry.getInstance().getHistoryFile(repository, file, logMessage.getCSetShortID(), false);
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
    
    private HistoryEntry createHistoryEntry(HgLogMessage h, File repository, File[] files) {
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
        private final File[] files;

        public OpenHistoryAction(File[] files) {
            this.files = files;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            openHistory(files);
        }
        private void openHistory(File[] files) {
            if(!isClientAvailable()) {
                LOG.log(Level.WARNING, "Mercurial client is unavailable"); // NOI18N
                return;
            }

            if(files == null || files.length == 0) {
                return;
            }
            Set<File> repositories = getRepositoryRoots(files);
            if(repositories == null) {
                return;
            }
            LogAction.openHistory(repositories.iterator().next(), files);
        }
        
    }

    private synchronized Action[] getActions() {
        if(actions == null) {
            actions = new Action[] {
                new HistoryAction() {
                    @Override
                    protected void perform(final HistoryEntry entry, final Set<File> files) {
                        final File root = Mercurial.getInstance().getRepositoryRoot(files.iterator().next());
                        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
                        HgProgressSupport support = new HgProgressSupport() {
                            @Override
                            public void perform() {
                                RevertModificationsAction.performRevert(
                                    root,   
                                    getHgRevision(entry).getRevisionNumber(),                           
                                    new LinkedList<File>(files), 
                                    HgModuleConfig.getDefault().getBackupOnRevertModifications(), 
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
                },
                new HistoryAction(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_View")) { // NOI18N
                    @Override
                    protected void perform(HistoryEntry entry, Set<File> files) {
                        view(entry, false, files);
                    }
                },
                new HistoryAction(NbBundle.getMessage(LogAction.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                    @Override
                    protected void perform(HistoryEntry entry, Set<File> files) {
                        view(entry, true, files);
                    }
                }
            };
        }
        return actions;
    }
    
    private void view(final HistoryEntry entry, final boolean showAnnotations, final Set<File> files) {
        final File root = Mercurial.getInstance().getRepositoryRoot(files.iterator().next());
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        rp.post(new Runnable() {
            @Override
            public void run() {
                for (File f : files) {
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
        String[] revs = entry.getRevision().split(":");
        final HgRevision revision = new HgRevision(revs[1], revs[0]);
        return revision;
    }
    
    /**
     * Returns true if mercurial client is installed and has a supported version.<br/>
     * Does not show any warning dialog.
     * @return true if mercurial client is available.
     */
    private static boolean isClientAvailable() {
        return isClientAvailable(false);
    }

    private static boolean isClientAvailable (boolean notifyUI) {
        return org.netbeans.modules.mercurial.Mercurial.getInstance().isAvailable(true, notifyUI);
    }

    private static Set<File> getRepositoryRoots(File... files) {
        Set<File> repositories = HgUtils.getRepositoryRoots(new HashSet<File>(Arrays.asList(files)));
        if (repositories.size() != 1) {
            LOG.log(Level.WARNING, "History requested for {0} repositories", repositories.size()); // NOI18N
            return null;
        }
        return repositories;
    }
}

