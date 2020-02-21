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
package org.netbeans.modules.mercurial.remote.ui.log;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.HistoryRegistry;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.ui.diff.ExportDiffAction;
import org.netbeans.modules.mercurial.remote.ui.pull.FetchAction;
import org.netbeans.modules.mercurial.remote.ui.pull.PullAction;
import org.netbeans.modules.mercurial.remote.ui.push.PushAction;
import org.netbeans.modules.mercurial.remote.ui.rollback.BackoutAction;
import org.netbeans.modules.mercurial.remote.ui.update.UpdateAction;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;

/**
 * Describes log information for a file. This is the result of doing a
 * cvs log command. The fields in instances of this object are populated
 * by response handlers.
 *
 * 
 */
public class RepositoryRevision {

    private final HgLogMessage message;

    private final VCSFileProxy repositoryRoot;
    private final VCSFileProxy[] selectionRoots;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged"; //NOI18N

    /**
     * List of events associated with the revision.
     */ 
    private final List<Event> events = new ArrayList<>(5);
    private final List<Event> dummyEvents;
    private final Set<String> headOfBranches;
    private final Kind kind;
    
    public static enum Kind {
        INCOMING,
        OUTGOING,
        LOCAL
    }

    public RepositoryRevision(HgLogMessage message, VCSFileProxy repositoryRoot, Kind kind, VCSFileProxy[] selectionRoots, Set<String> headOfBranches) {
        this.message = message;
        this.repositoryRoot = repositoryRoot;
        this.selectionRoots = selectionRoots;
        this.kind = kind;
        this.headOfBranches = headOfBranches;
        support = new PropertyChangeSupport(this);
        dummyEvents = prepareEvents(message.getDummyChangedPaths());
    }

    public VCSFileProxy getRepositoryRoot() {
        return repositoryRoot;
    }

    Event[] getEvents() {
        return events.toArray(new Event[events.size()]);
    }

    Event[] getDummyEvents () {
        return dummyEvents.toArray(new Event[dummyEvents.size()]);
    }

    public HgLogMessage getLog() {
        return message;
    }

    @Override
    public String toString() {        
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevisionNumber());
        text.append("\t"); //NOI18N
        text.append(getLog().getCSetShortID());
        text.append("\t"); //NOI18N
        text.append(getLog().getDate());
        text.append("\t"); //NOI18N
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getMessage());
        return text.toString();
    }

    boolean expandEvents () {
        Search s = currentSearch;
        if (s == null && !eventsInitialized) {
            currentSearch = new Search();
            currentSearch.start(Mercurial.getInstance().getRequestProcessor(repositoryRoot));
            return true;
        }
        return !eventsInitialized;
    }

    void cancelExpand () {
        Search s = currentSearch;
        if (s != null) {
            s.cancel();
            currentSearch = null;
        }
    }

    boolean isEventsInitialized () {
        return eventsInitialized;
    }
    
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    boolean isHeadOfBranch (String branchName) {
        return headOfBranches.contains(branchName);
    }

    @Messages({
        "CTL_SearchHistory.action.push=Push to default",
        "# {0} - revision id", "MSG_SearchHistory.pushing=Pushing {0}",
        "# {0} - revision id", "CTL_SearchHistory.action.update=Update to {0}",
        "CTL_SearchHistory.action.pull=Pull from default",
        "# {0} - revision id", "MSG_SearchHistory.pulling=Pulling {0}",
        "CTL_SearchHistory.action.fetch=Fetch from default",
        "# {0} - revision id", "MSG_SearchHistory.fetching=Fetching {0}"
    })
    Action[] getActions () {
        List<Action> actions = new ArrayList<>();
        if (kind != Kind.INCOMING) {
            actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_BackoutRevision")) { //NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    backout();
                }
            });
            actions.add(new AbstractAction(Bundle.CTL_SearchHistory_action_update(getLog().getCSetShortID())) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SystemAction.get(UpdateAction.class).update(repositoryRoot, getLog());
                }
            });
        }
        if (kind == Kind.OUTGOING) {
            actions.add(new AbstractAction(Bundle.CTL_SearchHistory_action_push()) {
                @Override
                public void actionPerformed (ActionEvent e) {
                    push();
                }
            });
        }
        if (kind == Kind.INCOMING) {
            actions.add(new AbstractAction(Bundle.CTL_SearchHistory_action_pull()) {
                @Override
                public void actionPerformed (ActionEvent e) {
                    pull();
                }
            });
            actions.add(new AbstractAction(Bundle.CTL_SearchHistory_action_fetch()) {
                @Override
                public void actionPerformed (ActionEvent e) {
                    fetch();
                }
            });
        }
        return actions.toArray(new Action[actions.size()]);
    }
    
    void backout () {
        BackoutAction.backout(this);
    }

    void push () {
        final String revision = getLog().getCSetShortID();
        HgProgressSupport supp = new HgProgressSupport() {
            @Override
            protected void perform () {
                PushAction.getDefaultAndPerformPush(repositoryRoot, revision, null, getLogger());
            }
        };
        supp.start(Mercurial.getInstance().getRequestProcessor(repositoryRoot), repositoryRoot, Bundle.MSG_SearchHistory_pushing(revision));
    }

    void pull () {
        final String revision = getLog().getCSetShortID();
        HgProgressSupport supp = new HgProgressSupport() {
            @Override
            protected void perform () {
                PullAction.getDefaultAndPerformPull(repositoryRoot, revision, null, this);
            }
        };
        supp.start(Mercurial.getInstance().getRequestProcessor(repositoryRoot), repositoryRoot, Bundle.MSG_SearchHistory_pulling(revision));
    }
    
    void fetch () {
        final String revision = getLog().getCSetShortID();
        HgProgressSupport supp = new HgProgressSupport() {
            @Override
            protected void perform () {
                FetchAction.performFetch(repositoryRoot, revision, this);
            }
        };
        supp.start(Mercurial.getInstance().getRequestProcessor(repositoryRoot), repositoryRoot, Bundle.MSG_SearchHistory_fetching(revision));
    }
    
    public class Event {
    
        /**
         * The file that this event is about. It may be null if the File cannot be computed.
         */ 
        private VCSFileProxy    file;
        private VCSFileProxy originalFile;
    
        private final HgLogMessageChangedPath changedPath;

        private final String name;
        private final String path;
        private boolean underRoots;

        Event (HgLogMessageChangedPath changedPath) {
            this.changedPath = changedPath;
            name = changedPath.getPath().substring(changedPath.getPath().lastIndexOf('/') + 1);
            
            int indexPath = changedPath.getPath().lastIndexOf('/');
            if(indexPath > -1) {
                path = changedPath.getPath().substring(0, indexPath);
            } else {
                path = "";
            }
        }

        public RepositoryRevision getLogInfoHeader() {
            return RepositoryRevision.this;
        }

        HgLogMessageChangedPath getChangedPath() {
            return changedPath;
        }

        /** Getter for property file.
         * @return Value of property file.
         */
        public VCSFileProxy getFile() {
            return file;
        }

        /** Setter for property file.
         * @param file New value of property file.
         */
        public void setFile(VCSFileProxy file, boolean isUnderRoots) {
            this.file = file;
            this.underRoots = isUnderRoots;
        }

        public VCSFileProxy getOriginalFile() {
            return originalFile;
        }

        void setOriginalFile (VCSFileProxy file) {
            this.originalFile = file;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
        
        @Override
        public String toString() {
            return changedPath.getPath();
        }

        boolean isUnderRoots () {
            return underRoots;
        }

        @Messages({
            "CTL_Action.ViewCurrent.name=View Current"
        })
        Action[] getActions () {
            List<Action> actions = new ArrayList<>();
            boolean viewEnabled = getFile() != null && getChangedPath().getAction() != HgLogMessage.HgDelStatus;
            if (getFile() != null) {
                actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_RollbackTo", getLogInfoHeader().getLog().getRevisionNumber())) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        revertModifications();
                    }                
                });
            }
            if (viewEnabled) {
                actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_View")) { //NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                viewFile(false);
                            }
                        });
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                viewFile(true);
                            }
                        });
                    }
                });
                actions.add(new AbstractAction(Bundle.CTL_Action_ViewCurrent_name()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                VCSFileProxySupport.openFile(getFile().normalizeFile());
                            }
                        });
                    }
                });
                actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_ExportFileDiff")) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ExportDiffAction.exportDiffFileRevision(Event.this);
                    }
                });
            }
            return actions.toArray(new Action[actions.size()]);
        }
        
        void viewFile (boolean showAnnotations) {
            try {
                HgUtils.openInRevision(getFile(), -1, getLogInfoHeader().getLog().getHgRevision(), showAnnotations);
            } catch (IOException ex) {
                // Ignore if file not available in cache
            }
        }

        void revertModifications () {
            SummaryView.revert(null, new Event[] { this });
        }
    }

    private List<Event> prepareEvents (HgLogMessageChangedPath[] paths) {
        final List<Event> logEvents = new ArrayList<>(paths.length);
        for (HgLogMessageChangedPath path : paths) {
            logEvents.add(new Event(path));
        }
        for (RepositoryRevision.Event event : logEvents) {
            String filePath = event.getChangedPath().getPath();
            VCSFileProxy f = VCSFileProxy.createFileProxy(repositoryRoot, filePath);
            VCSFileProxy cachedRename = HistoryRegistry.getInstance().getHistoryFile(repositoryRoot, f, message.getCSetShortID(), true);
            boolean underRoots = false;
            for (VCSFileProxy selectionRoot : selectionRoots) {
                if (VersioningSupport.isFlat(selectionRoot)) {
                    underRoots = selectionRoot.equals(f.getParentFile());
                } else {
                    underRoots = VCSFileProxySupport.isAncestorOrEqual(selectionRoot, f);
                }
                if (underRoots) {
                    break;
                }
            }
            if (cachedRename != null) {
                f = cachedRename;
            }
            event.setFile(f, underRoots);
            event.setOriginalFile(f);
        }
        for (RepositoryRevision.Event event : logEvents) {
            if ((event.getChangedPath().getAction() == HgLogMessage.HgCopyStatus || event.getChangedPath().getAction() == HgLogMessage.HgRenameStatus)
                    && event.getChangedPath().getCopySrcPath() != null) {
                VCSFileProxy originalFile = VCSFileProxy.createFileProxy(repositoryRoot, event.getChangedPath().getCopySrcPath());
                event.setOriginalFile(originalFile);
            }
        }
        return logEvents;
    }

    private class Search extends HgProgressSupport {

        @Override
        protected void perform () {
            HgLogMessageChangedPath[] paths;
            if (getLog().getChangedPaths().length == 0) {
                HistoryRegistry.ChangePathCollector coll = kind == Kind.INCOMING
                        ? new HistoryRegistry.ChangePathCollector() {
                            @Override
                            public HgLogMessageChangedPath[] getChangePaths () {
                                HgLogMessage[] messages = null;
                                try {
                                    messages = HgCommand.getIncomingMessages(repositoryRoot, getLog().getCSetShortID(), null, true, true, false, 1, getLogger());
                                } catch (HgException.HgCommandCanceledException ex) {
                                    // do not take any action
                                } catch (HgException ex) {
                                    HgUtils.notifyException(ex);
                                }
                                return messages == null || messages.length == 0 ? new HgLogMessageChangedPath[0] : messages[0].getChangedPaths();
                            }
                        }
                        : new HistoryRegistry.DefaultChangePathCollector(repositoryRoot, getLogger(), getLog().getCSetShortID());
                List<HgLogMessageChangedPath> pathList = HistoryRegistry.getInstance().initializeChangePaths(
                        repositoryRoot, coll, getLog(), false);
                paths = pathList.toArray(new HgLogMessageChangedPath[pathList.size()]);
            } else {
                paths = getLog().getChangedPaths();
            }
            final List<Event> logEvents = prepareEvents(paths);
            if (!isCanceled()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        if (!isCanceled()) {
                            events.clear();
                            dummyEvents.clear();
                            events.addAll(logEvents);
                            eventsInitialized = true;
                            currentSearch = null;
                            support.firePropertyChange(RepositoryRevision.PROP_EVENTS_CHANGED, null, new ArrayList<>(events));
                        }
                    }
                });
            }
        }

        @Override
        protected void finnishProgress () {

        }

        @Override
        protected void startProgress () {

        }

        @Override
        protected ProgressHandle getProgressHandle () {
            return null;
        }
    }
}
