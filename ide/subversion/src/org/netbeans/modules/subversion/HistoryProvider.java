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
package org.netbeans.modules.subversion;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.history.HistoryAction;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.util.*;
import org.tigris.subversion.svnclientadapter.*;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryProvider implements VCSHistoryProvider {

    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<VCSHistoryProvider.HistoryChangeListener>();
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
    public HistoryEntry[] getHistory(File[] files, Date fromDate) {
        
        try {
            SvnClient client = Subversion.getInstance().getClient(files[0]);

            List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
            Map<String, Set<File>> rev2FileMap = new HashMap<String, Set<File>>();
            Map<String, ISVNLogMessage> rev2LMMap = new HashMap<String, ISVNLogMessage>();
            Map<File, SVNUrl> file2Copy = new HashMap<File, SVNUrl>();
            SVNUrl repoUrl = null;
            for (File file : files) {
                FileInformation fi = Subversion.getInstance().getStatusCache().getStatus(file);
                if ((fi.getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
                    continue;
                }
                
                ISVNLogMessage[] messages = null;
                if ((fi.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) &&
                     fi.getEntry(file).isCopied()) 
                {
                    ISVNInfo info = SvnUtils.getInfoFromWorkingCopy(client, file);
                    SVNUrl copyUrl = info.getCopyUrl();
                    repoUrl = info.getRepository();
                    
                    if (copyUrl != null) {
                        messages = 
                            client.getLogMessages(
                                    copyUrl, 
                                    fromDate == null ? 
                                        new SVNRevision.Number(1) : 
                                        new SVNRevision.DateSpec(fromDate),
                                        SVNRevision.HEAD
                                    );
                        file2Copy.put(file, copyUrl);
                    }
                }
                if (messages == null) {
                    messages = 
                        client.getLogMessages(
                                    file, 
                                    fromDate == null ? 
                                        new SVNRevision.Number(1) : 
                                        new SVNRevision.DateSpec(fromDate),
                                        SVNRevision.HEAD
                                    );
                }
                
                for (ISVNLogMessage m : messages) {
                    String r = m.getRevision().toString();
                    rev2LMMap.put(r, m);
                    Set<File> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<File>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            }

            for(ISVNLogMessage m : rev2LMMap.values()) {
                Set<File> s = rev2FileMap.get(m.getRevision().toString());
                File[] involvedFiles = s.toArray(new File[0]);
                HistoryEntry e = new HistoryEntry(
                    involvedFiles, 
                    m.getDate(), 
                    m.getMessage(), 
                    m.getAuthor(), 
                    m.getAuthor(), 
                    m.getRevision().toString(), 
                    m.getRevision().toString(), 
                    getActions(), 
                    new RevisionProviderImpl(m.getRevision(), repoUrl, file2Copy));
                ret.add(e);
                
            }

            return ret.toArray(new HistoryEntry[0]);
        } catch (SVNClientException e) {
            if (SvnClientExceptionHandler.isCancelledAction(e.getMessage())) {
                Subversion.LOG.log(Level.FINE, null, e);
            } else {
                SvnClientExceptionHandler.notifyException(e, true, true);
            }
        }
        return null;
    }

    @Override
    public Action createShowHistoryAction(File[] files) {
        return new OpenHistoryAction(files);
    }

    public void fireHistoryChange(final File[] files) {
        final HistoryChangeListener[] la;
        synchronized(listeners) {
            la = listeners.toArray(new HistoryChangeListener[0]);
        }
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                for (HistoryChangeListener l : la) {
                    l.fireHistoryChanged(new HistoryEvent(HistoryProvider.this, files));
                }
            }
        });
    }
    
    private static class RevisionProviderImpl implements RevisionProvider {
        private final SVNRevision revision;
        private final Map<File, SVNUrl> file2Copy;
        private final SVNUrl repoUrl;

        public RevisionProviderImpl(SVNRevision svnRevision, SVNUrl repoUrl, Map<File, SVNUrl> file2Copy) {
            this.revision = svnRevision;
            this.file2Copy = file2Copy;
            this.repoUrl = repoUrl;
        }
        
        @Override
        public void getRevisionFile(File originalFile, File revisionFile) {
            try {
                File file;
                SVNUrl copyUrl = repoUrl != null ? file2Copy.get(originalFile) : null;
                if(copyUrl != null) {
                    file = VersionsCache.getInstance().getFileRevision(repoUrl, copyUrl, revision.toString(), originalFile.getName());
                } else {
                    file = VersionsCache.getInstance().getFileRevision(originalFile, revision.toString());
                }
                FileUtils.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
            } catch (IOException e) {
                Exception ex = e;
                if (e.getCause() instanceof SVNClientException) {
                    ex = (SVNClientException) e.getCause();
                }
                if (SvnClientExceptionHandler.isCancelledAction(ex.getMessage())) {
                    Subversion.LOG.log(Level.FINE, null, e);
                } else {
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                }
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

        public void openHistory(final File[] files) {
            if(files == null || files.length == 0) {
                return;
            }
            if(!org.netbeans.modules.subversion.api.Subversion.isClientAvailable(true)) {
                org.netbeans.modules.subversion.Subversion.LOG.log(Level.WARNING, "Subversion client is unavailable");
                return;
            }

            /**
            * Open in AWT
            */
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    SearchHistoryAction.openHistory(files);
                }
            });
        }
    }
    
    private synchronized Action[] getActions() {
        if(actions == null) {
            actions = new Action[] {
                new RollbackAction(),
                new ViewAction(),
                new AnnotateAction()
            };
        }
        return actions;
    }

    private class ViewAction extends HistoryAction {
        public ViewAction() {
            super(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_View"));
        }
            @Override
        protected void perform(HistoryEntry entry, Set<File> files) {
            SVNRevision.Number svnRevision = new SVNRevision.Number(Long.parseLong(entry.getRevision()));
            view(svnRevision, false, files.toArray(new File[0]));
        }
    }

    private class AnnotateAction extends HistoryAction {
        public AnnotateAction() {
            super(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_ShowAnnotations"));
        }
        @Override
        protected void perform(HistoryEntry entry, Set<File> files) {
            SVNRevision.Number svnRevision = new SVNRevision.Number(Long.parseLong(entry.getRevision()));
            view(svnRevision, true, files.toArray(new File[0]));
        }
    }
    
    private class RollbackAction extends HistoryAction {
        @Override
        protected void perform(final HistoryEntry entry, final Set<File> files) {
            final File file = files.iterator().next();
            SVNUrl repository;
            try {
                repository = SvnUtils.getRepositoryRootUrl(file);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
                return;
            }
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
            SvnProgressSupport support = new SvnProgressSupport() {
                @Override
                public void perform() {
                    try {
                        SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(file);
                        for(File file : files) {
                            SvnClient client = Subversion.getInstance().getClient(false);
                            ISVNInfo info = client.getInfo(file);
                            SVNUrl fileUrl = info.getUrl();
                            SVNRevision.Number svnRevision = new SVNRevision.Number(Long.parseLong(entry.getRevision()));
                            SvnUtils.rollback(file, repoUrl, fileUrl, svnRevision, false, getLogger());
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            };
            support.start(rp, repository, NbBundle.getMessage(SearchHistoryAction.class, "MSG_Rollback_Progress")); // NOI18N
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
            return NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_RollbackTo", rev); // NOI18N
        }
        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }
    
    private void view(final SVNRevision revision, final boolean showAnnotations, final File... files) {
        if(files == null || files.length == 0) {
            return;
        }
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(files[0]);
                    for (File file : files) {
                        SvnClient client = Subversion.getInstance().getClient(false);
                        ISVNInfo info = client.getInfo(file);
                        SVNUrl fileUrl = info.getUrl();
                        SvnUtils.openInRevision(file, repoUrl, fileUrl, revision, revision, showAnnotations);
                    }
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
                }
            }
        });
    }
}
