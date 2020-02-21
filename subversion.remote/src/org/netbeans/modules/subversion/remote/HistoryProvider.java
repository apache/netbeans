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
package org.netbeans.modules.subversion.remote;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.history.HistoryActionVCSProxyBased;
import org.openide.util.*;

/**
 *
 * 
 */
public class HistoryProvider implements VCSHistoryProvider {

    private final List<VCSHistoryProvider.HistoryChangeListener> listeners = new LinkedList<>();
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
    public HistoryEntry[] getHistory(VCSFileProxy[] files, Date fromDate) {
        
        try {
            SvnClient client = Subversion.getInstance().getClient(files[0]);

            List<HistoryEntry> ret = new LinkedList<>();
            Map<String, Set<VCSFileProxy>> rev2FileMap = new HashMap<>();
            Map<String, ISVNLogMessage> rev2LMMap = new HashMap<>();
            Map<VCSFileProxy, SVNUrl> file2Copy = new HashMap<>();
            SVNUrl repoUrl = null;
            for (VCSFileProxy file : files) {
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
                    Set<VCSFileProxy> s = rev2FileMap.get(r);
                    if(s == null) {
                        s = new HashSet<>();
                        rev2FileMap.put(r, s);
                    }
                    s.add(file);
                }
            }

            for(ISVNLogMessage m : rev2LMMap.values()) {
                Set<VCSFileProxy> s = rev2FileMap.get(m.getRevision().toString());
                VCSFileProxy[] involvedFiles = s.toArray(new VCSFileProxy[s.size()]);
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

            return ret.toArray(new HistoryEntry[ret.size()]);
        } catch (SVNClientException e) {
            if (SvnClientExceptionHandler.isCancelledAction(e.getMessage())) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, null, e);
                }
            } else {
                SvnClientExceptionHandler.notifyException(new Context(files), e, true, true);
            }
        }
        return null;
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
        private final Map<VCSFileProxy, SVNUrl> file2Copy;
        private final SVNUrl repoUrl;

        public RevisionProviderImpl(SVNRevision svnRevision, SVNUrl repoUrl, Map<VCSFileProxy, SVNUrl> file2Copy) {
            this.revision = svnRevision;
            this.file2Copy = file2Copy;
            this.repoUrl = repoUrl;
        }
        
        @Override
        public void getRevisionFile(VCSFileProxy originalFile, VCSFileProxy revisionFile) {
            try {
                SVNUrl copyUrl = repoUrl != null ? file2Copy.get(originalFile) : null;
                VCSFileProxy file;
                if(copyUrl != null) {
                    file = VCSFileProxy.createFileProxy(VersionsCache.getInstance(VCSFileProxySupport.getFileSystem(originalFile)).getFileRevision(repoUrl, copyUrl, revision.toString(), originalFile.getName()));
                } else {
                    file = VersionsCache.getInstance(VCSFileProxySupport.getFileSystem(originalFile)).getFileRevision(originalFile, revision.toString());
                    VCSFileProxySupport.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
                }
                VCSFileProxySupport.copyFile(file, revisionFile); // XXX lets be faster - LH should cache that somehow ...
            } catch (IOException e) {
                Exception ex = e;
                if (e.getCause() instanceof SVNClientException) {
                    ex = (SVNClientException) e.getCause();
                }
                if (SvnClientExceptionHandler.isCancelledAction(ex.getMessage())) {
                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                        Subversion.LOG.log(Level.FINE, null, e);
                    }
                } else {
                    SvnClientExceptionHandler.notifyException(new Context(revisionFile), ex, true, true);
                }
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

        public void openHistory(final VCSFileProxy[] files) {
            if(files == null || files.length == 0) {
                return;
            }
            
            if(!SvnClientFactory.isClientAvailable(new Context(files))) {
                Subversion.LOG.log(Level.WARNING, "Subversion client is unavailable");
                return;
            }

            /**
            * Open in AWT
            */
            EventQueue.invokeLater(new Runnable() {
                @Override
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

    private class ViewAction extends HistoryActionVCSProxyBased {
        public ViewAction() {
            super(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_View"));
        }
        @Override
        protected void perform(HistoryEntry entry, Set<VCSFileProxy> files) {
            SVNRevision.Number svnRevision = new SVNRevision.Number(Long.parseLong(entry.getRevision()));
            view(svnRevision, false, files.toArray(new VCSFileProxy[files.size()]));
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }

    private class AnnotateAction extends HistoryActionVCSProxyBased {
        public AnnotateAction() {
            super(NbBundle.getMessage(SearchHistoryAction.class, "CTL_SummaryView_ShowAnnotations"));
        }
        @Override
        protected void perform(HistoryEntry entry, Set<VCSFileProxy> files) {
            SVNRevision.Number svnRevision = new SVNRevision.Number(Long.parseLong(entry.getRevision()));
            view(svnRevision, true, files.toArray(new VCSFileProxy[files.size()]));
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
    }
    
    private static class RollbackAction extends HistoryActionVCSProxyBased {
        @Override
        protected void perform(final HistoryEntry entry, final Set<VCSFileProxy> files) {
            final VCSFileProxy file = files.iterator().next();
            final Context context = new Context(file);
            SVNUrl repository;
            try {
                repository = ContextAction.getSvnUrl(context);
                // repository can be null here, but the callees below seem to be able to process this
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(context, ex, false, false);
                return;
            }
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
            SvnProgressSupport support = new SvnProgressSupport(context.getFileSystem()) {
                @Override
                public void perform() {
                    try {
                        SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(file);
                        for(VCSFileProxy file : files) {
                            SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                            ISVNInfo info = client.getInfo(file);
                            SVNUrl fileUrl = info.getUrl();
                            SVNRevision.Number svnRevision = new SVNRevision.Number(Long.parseLong(entry.getRevision()));
                            SvnUtils.rollback(file, repoUrl, fileUrl, svnRevision, false, getLogger());
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
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
    
    private void view(final SVNRevision revision, final boolean showAnnotations, final VCSFileProxy... files) {
        if(files == null || files.length == 0) {
            return;
        }
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SVNUrl repoUrl = SvnUtils.getRepositoryRootUrl(files[0]);
                    for (VCSFileProxy file : files) {
                        SvnClient client = Subversion.getInstance().getClient(false, new Context(files));
                        ISVNInfo info = client.getInfo(file);
                        SVNUrl fileUrl = info.getUrl();
                        SvnUtils.openInRevision(file, repoUrl, fileUrl, revision, revision, showAnnotations);
                    }
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(new Context(files), ex, false, false);
                }
            }
        });
    }
}
