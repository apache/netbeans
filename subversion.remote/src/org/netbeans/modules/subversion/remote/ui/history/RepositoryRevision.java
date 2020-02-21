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
package org.netbeans.modules.subversion.remote.ui.history;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessageChangePath;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.update.RevertModifications;
import org.netbeans.modules.subversion.remote.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Describes log information for a file. This is the result of doing a
 * cvs log command. The fields in instances of this object are populated
 * by response handlers.
 *
 * 
 */
final class RepositoryRevision {

    private ISVNLogMessage message;

    private final SVNUrl repositoryRootUrl;

    /**
     * List of events associated with the revision.
     */
    private final List<Event> events = new ArrayList<>(5);
    private List<Event> fakeRootEvents;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged"; //NOI18N
    private final VCSFileProxy[] selectionRoots;
    private final Map<String,VCSFileProxy> pathToRoot;
    private final Map<String, SVNRevision> pegRevisions;
    private final FileSystem fileSystem;

    public RepositoryRevision(ISVNLogMessage message, SVNUrl rootUrl, VCSFileProxy[] selectionRoots,
            Map<String,VCSFileProxy> pathToRoot, Map<String, SVNRevision> pegRevisions) {
        this.message = message;
        this.repositoryRootUrl = rootUrl;
        this.selectionRoots = selectionRoots;
        fileSystem = VCSFileProxySupport.getFileSystem(selectionRoots[0]);
        support = new PropertyChangeSupport(this);
        this.pathToRoot = pathToRoot;
        this.pegRevisions = pegRevisions;
        initFakeRootEvent();
    }

    public SVNUrl getRepositoryRootUrl() {
        return repositoryRootUrl;
    }

    List<Event> getDummyEvents () {
        return fakeRootEvents;
    }

    List<Event> getEvents() {
        return events;
    }

    public ISVNLogMessage getLog() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevision().getNumber());
        text.append("\t"); //NOI18N
        text.append(getLog().getDate());
        text.append("\t"); //NOI18N
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getMessage());
        return text.toString();
    }

    public void sort (Comparator<RepositoryRevision.Event> comparator) {
        if (events == null) {
            return;
        }
        Collections.sort(events, comparator);
    }

    boolean expandEvents () {
        Search s = currentSearch;
        if (s == null && !eventsInitialized) {
            currentSearch = new Search();
            currentSearch.start(Subversion.getInstance().getRequestProcessor(repositoryRootUrl), repositoryRootUrl, null);
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
    
    Action[] getActions () {
        List<Action> actions = new ArrayList<>();
        actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_RollbackChange")) { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                SvnProgressSupport support = new SvnProgressSupport(fileSystem) {
                    @Override
                    public void perform() {
                        RevertModifications.RevisionInterval revisionInterval = new RevertModifications.RevisionInterval(getLog().getRevision());
                        final Context ctx = new Context(selectionRoots);
                        RevertModificationsAction.performRevert(revisionInterval, false, false, ctx, this);
                    }
                };
                support.start(Subversion.getInstance().getRequestProcessor(repositoryRootUrl),
                        repositoryRootUrl, NbBundle.getMessage(SummaryView.class, "MSG_Revert_Progress")); //NOI18N
            }
        });
        return actions.toArray(new Action[actions.size()]);
    }

    public class Event {

        /**
         * The file or folder that this event is about. It may be null if the File cannot be computed.
         */
        private VCSFileProxy    file;

        private final ISVNLogMessageChangePath changedPath;

        private final String name;
        private final String path;
        private final boolean underRoots;
        private VCSFileProxy originalFile;
        private final String originalPath;
        private final String action;
        private final String originalName;
        private ArrayList<Action> actions;

        public Event (ISVNLogMessageChangePath changedPath, boolean underRoots, String displayAction) {
            this.changedPath = changedPath;
            name = changedPath.getPath().substring(changedPath.getPath().lastIndexOf('/') + 1);
            path = changedPath.getPath().substring(0, changedPath.getPath().lastIndexOf('/'));
            originalPath = changedPath.getCopySrcPath();
            originalName = originalPath == null ? null : originalPath.substring(originalPath.lastIndexOf('/') + 1);
            this.underRoots = underRoots;
            this.action = displayAction == null ? Character.toString(changedPath.getAction()) : displayAction;
        }

        public RepositoryRevision getLogInfoHeader() {
            return RepositoryRevision.this;
        }

        public ISVNLogMessageChangePath getChangedPath() {
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
        public void setFile(VCSFileProxy file) {
            this.file = file;
        }

        public VCSFileProxy getOriginalFile () {
            return originalFile;
        }

        public void setOriginalFile (VCSFileProxy originalFile) {
            this.originalFile = originalFile;
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

        String getOriginalPath () {
            return originalPath;
        }

        String getOriginalName () {
            return originalName;
        }

        String getAction() {
            return action;
        }
    
        @Messages({
            "CTL_Action.ViewCurrent.name=View Current"
        })
        Action[] getActions () {
            if (actions == null) {
                actions = new ArrayList<>();
                boolean rollbackToEnabled = getFile() != null && getChangedPath().getAction() != 'D'; //NOI18N
                boolean rollbackChangeEnabled = getFile() != null && (getChangedPath().getAction() != 'D' || !getFile().exists()); //NOI18N
                boolean viewEnabled = rollbackToEnabled && !getFile().isDirectory();
                if (rollbackChangeEnabled) {
                    actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_RollbackChange")) { //NOI18N
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            revert();
                        }
                    });
                }
                if (rollbackToEnabled) {
                    actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_RollbackToShort")) { //NOI18N
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
                                @Override
                                public void run() {
                                    rollback();
                                }
                            });
                        }
                    });
                }
                if (viewEnabled) {
                    actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_View")) { //NOI18N
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
                                @Override
                                public void run() {
                                    viewFile(false);
                                }
                            });
                        }
                    });
                    actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_ShowAnnotations")) { //NOI18N
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
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
                            Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
                                @Override
                                public void run() {
                                    VCSFileProxySupport.openFile(getFile().normalizeFile());
                                }
                            });
                        }
                    });
                }
            }
            return actions.toArray(new Action[actions.size()]);
        }

        void viewFile (boolean showAnnotations) {
            VCSFileProxy originFile = getFile();
            SVNRevision rev = getLogInfoHeader().getLog().getRevision();
            SVNUrl repoUrl = getLogInfoHeader().getRepositoryRootUrl();
            SVNUrl fileUrl = repoUrl.appendPath(getChangedPath().getPath());
            SvnUtils.openInRevision(originFile, repoUrl, fileUrl, rev, rev, showAnnotations);
        }
        
        void rollback () {
            SvnProgressSupport support = new SvnProgressSupport(fileSystem) {
                @Override
                public void perform() {
                    VCSFileProxy file = getFile();
                    boolean wasDeleted = getChangedPath().getAction() == 'D';
                    SVNUrl repoUrl = getLogInfoHeader().getRepositoryRootUrl();
                    SVNUrl fileUrl = repoUrl.appendPath(getChangedPath().getPath());                    
                    SVNRevision.Number revision = getLogInfoHeader().getLog().getRevision();
                    SvnUtils.rollback(file, repoUrl, fileUrl, revision, wasDeleted, getLogger());
                }
            };
            support.start(Subversion.getInstance().getRequestProcessor(repositoryRootUrl),
                    repositoryRootUrl, NbBundle.getMessage(RepositoryRevision.class, "MSG_Rollback_Progress")); //NOI18N
        }

        void revert () {
            SvnProgressSupport support = new SvnProgressSupport(fileSystem) {
                @Override
                public void perform() {
                    RevertModifications.RevisionInterval revisionInterval = new RevertModifications.RevisionInterval(getLogInfoHeader().getLog().getRevision());
                    final Context ctx = new Context(getFile());
                    RevertModificationsAction.performRevert(revisionInterval, false, false, ctx, this);
                }
            };
            support.start(Subversion.getInstance().getRequestProcessor(repositoryRootUrl),
                    repositoryRootUrl, NbBundle.getMessage(SummaryView.class, "MSG_Revert_Progress")); //NOI18N
        }
    }

    public static class EventFullNameComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1 == null || e2 == null || e1.getChangedPath() == null || e2.getChangedPath() == null) {
                return 0;
            }
            return e1.getChangedPath().getPath().compareTo(e2.getChangedPath().getPath());
        }
    }

    public static class EventBaseNameComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1 == null || e2 == null || e1.getName() == null || e2.getName() == null) {
                return 0;
            }
            return e1.getName().compareTo(e2.getName());
        }
    }

    public void initFakeRootEvent() {
        fakeRootEvents = new LinkedList<>();
        for (final VCSFileProxy selectionRoot : selectionRoots) {
            Event e = new Event(new ISVNLogMessageChangePath() {
                private String path;
                @Override
                public String getPath() {
                    if(path == null) {
                        try {
                            path = SvnUtils.getRelativePath(selectionRoot);
                            if (!path.startsWith("/")) {  //NOI18N
                                path = "/" + path;  //NOI18N
                            }
                        } catch (SVNClientException ex) {
                            Subversion.LOG.log(Level.INFO, selectionRoot.getPath(), ex);
                            path = "/";  //NOI18N
                        }
                    }
                    return path;
                }
                @Override
                public SVNRevision.Number getCopySrcRevision() {
                    return null;
                }
                @Override
                public String getCopySrcPath() {
                    return null;
                }
                @Override
                public char getAction() {
                    return '?';
                }
            }, true, null);
            e.setFile(selectionRoot);
            fakeRootEvents.add(e);
        }
    }
    
    private class Search extends SvnProgressSupport {

        private Search() {
            super(fileSystem);
        }

        @Override
        protected void perform () {
            try {
                SvnClient client = Subversion.getInstance().getClient(new Context(selectionRoots), repositoryRootUrl, this);
                ISVNLogMessage [] messages = new ISVNLogMessage[0];
                if (pegRevisions == null) {
                    // searching URL
                    messages = client.getLogMessages(repositoryRootUrl, message.getRevision(), message.getRevision());
                } else {
                    // do not call search history for with repo root url, some repositories
                    // may limit access to the root folder
                    for (VCSFileProxy f : selectionRoots) {
                        String p = SvnUtils.getRelativePath(f);
                        if (p != null && p.startsWith("/")) { //NOI18N
                            p = p.substring(1, p.length());
                        }
                        messages = client.getLogMessages(repositoryRootUrl.appendPath(p), pegRevisions.get(p),
                                message.getRevision(), message.getRevision(), false, true, 0);
                        if (messages.length > 0) {
                            break;
                        }
                    }
                }
                if (messages.length > 0) {
                    final ISVNLogMessage msg = messages[0];
                    final List<Event> logEvents = prepareEvents(msg);
                    if (!isCanceled()) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                if (!isCanceled()) {
                                    message = msg;
                                    events.clear();
                                    fakeRootEvents.clear();
                                    events.addAll(logEvents);
                                    eventsInitialized = true;
                                    currentSearch = null;
                                    support.firePropertyChange(RepositoryRevision.PROP_EVENTS_CHANGED, null, new ArrayList<>(events));
                                }
                            }
                        });
                    }
                }
            } catch (SVNClientException e) {
                if (!SvnClientExceptionHandler.handleLogException(repositoryRootUrl, message.getRevision(), e)) {
                    annotate(e);
                }
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

        private List<Event> prepareEvents (ISVNLogMessage message) {
            ISVNLogMessageChangePath [] paths = message.getChangedPaths();
            if (paths == null) {
                return Collections.<Event>emptyList();
            } else {
                List<Event> events = new ArrayList<>(paths.length);
                Set<String> removedPaths = new HashSet<>(paths.length);
                for (ISVNLogMessageChangePath path : paths) {
                    if (path.getAction() == 'D') {
                        removedPaths.add(path.getPath());
                    }
                }
                for (ISVNLogMessageChangePath path : paths) {
                    boolean underRoots = false;
                    VCSFileProxy f = computeFile(path.getPath());
                    if (f != null) {
                        for (VCSFileProxy selectionRoot : selectionRoots) {
                            // TODO: CND does not support flat folders, Probably should?
                            if (VCSFileProxySupport.isAncestorOrEqual(selectionRoot, f)) {
                                break;
                            }
                        }
                    }
                    String action = Character.toString(path.getAction());
                    if (path.getAction() == 'A' && path.getCopySrcPath() != null) {
                        if (removedPaths.contains(path.getCopySrcPath())) {
                            action = "R";  //NOI18N // rename
                        } else {
                            action = "C";  //NOI18N // copied
                        }
                    }
                    Event event = new Event(path, underRoots, action);
                    event.setFile(f);
                    if (path.getCopySrcPath() != null) {
                        event.setOriginalFile(computeFile(path.getCopySrcPath()));
                    }
                    events.add(event);
                }
                Collections.sort(events, new EventFullNameComparator());
                return events;
            }
        }
    }

    private VCSFileProxy computeFile(String path) {
        for (String s : pathToRoot.keySet()) {
            if (path.startsWith(s)) {
                final String rest = path.substring(s.length());
                if (rest.isEmpty()) {
                    return pathToRoot.get(s);
                } else {
                    return VCSFileProxy.createFileProxy(pathToRoot.get(s), rest);
                }
            }
        }
        return null;
    }
}
