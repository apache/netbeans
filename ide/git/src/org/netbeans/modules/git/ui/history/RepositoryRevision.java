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
package org.netbeans.modules.git.ui.history;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.branch.CherryPickAction;
import org.netbeans.modules.git.ui.checkout.CheckoutRevisionAction;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.diff.ExportCommitAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.revert.RevertCommitAction;
import org.netbeans.modules.git.ui.tag.CreateTagAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

public class RepositoryRevision {

    private GitRevisionInfo message;


    /**
     * List of events associated with the revision.
     */ 
    private final List<Event> events = new ArrayList<Event>(5);
    private final List<Event> dummyEvents;
    private final Map<File, String> commonAncestors = new HashMap<File, String>();
    private final Set<GitTag> tags;
    private final Set<GitBranch> branches;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged"; //NOI18N
    private final File repositoryRoot;
    private final File[] selectionRoots;
    private String preferredRevision;
    private final SearchExecutor.Mode mode;

    RepositoryRevision (GitRevisionInfo message, File repositoryRoot, File[] selectionRoots,
            Set<GitTag> tags, Set<GitBranch> branches, File dummyFile, String dummyFileRelativePath,
            SearchExecutor.Mode mode) {
        this.message = message;
        this.repositoryRoot = repositoryRoot;
        this.selectionRoots = selectionRoots;
        this.tags = tags;
        this.branches = branches;
        support = new PropertyChangeSupport(this);
        dummyEvents = new ArrayList<Event>(1);
        if (dummyFile != null && dummyFileRelativePath != null) {
            dummyEvents.add(new Event(dummyFile, dummyFileRelativePath));
        }
        this.mode = mode;
    }

    public Event[] getEvents() {
        return events.toArray(new Event[0]);
    }

    Event[] getDummyEvents () {
        return dummyEvents.toArray(new Event[0]);
    }

    public GitRevisionInfo getLog() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevision());
        text.append("\t"); //NOI18N
        text.append(DateFormat.getDateTimeInstance().format(new Date(getLog().getCommitTime())));
        text.append("\t"); //NOI18N
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getFullMessage());
        return text.toString();
    }

    String getAncestorCommit (File file, GitClient client, ProgressMonitor pm) throws GitException {
        String ancestorCommit = commonAncestors.get(file);
        if (ancestorCommit == null && !commonAncestors.containsKey(file)) {
            GitRevisionInfo info = null;
            if (getLog().getParents().length == 1) {
                info = client.getPreviousRevision(file, getLog().getRevision(), pm);
            } else if (getLog().getParents().length > 1) {
                info = client.getCommonAncestor(getLog().getParents(), pm);
            }
            ancestorCommit = info == null ? null : info.getRevision();
            commonAncestors.put(file, ancestorCommit);
        }
        return ancestorCommit;
    }

    public GitBranch[] getBranches () {
        return branches == null ? new GitBranch[0] : branches.toArray(new GitBranch[0]);
    }

    public GitTag[] getTags () {
        return tags == null ? new GitTag[0] : tags.toArray(new GitTag[0]);
    }
    
    boolean expandEvents () {
        Search s = currentSearch;
        if (s == null && !eventsInitialized) {
            currentSearch = new Search();
            currentSearch.start(Git.getInstance().getRequestProcessor(repositoryRoot), repositoryRoot);
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

    File getRepositoryRoot () {
        return repositoryRoot;
    }
    
    String getShortRevision () {
        String revision = getLog().getRevision();
        if (revision.length() > 7) {
            revision = revision.substring(0, 7);
        }
        return revision;
    }
    
    @NbBundle.Messages({
        "# {0} - revision", "LBL_Action.CheckoutRevision=Checkout {0}",
        "# {0} - revision", "MSG_CheckoutRevision.progress=Checking out {0}"
    })
    Action[] getActions () {
        if (RepositoryInfo.getInstance(repositoryRoot) == null) {
            // repository deleted? Then return no actions, nothing makes sense any more.
            return new Action[0];
        }
        List<Action> actions = new ArrayList<Action>();
        final String revision = getPreferredRevision();
        actions.add(new AbstractAction(Bundle.LBL_Action_CheckoutRevision(revision)) {
            @Override
            public void actionPerformed (ActionEvent e) {
                CheckoutRevisionAction action = SystemAction.get(CheckoutRevisionAction.class);
                action.checkoutRevision(repositoryRoot, revision, null, Bundle.MSG_CheckoutRevision_progress(revision));
            }
        });
        actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_TagCommit")) { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                CreateTagAction action = SystemAction.get(CreateTagAction.class);
                action.createTag(repositoryRoot, getLog().getRevision());
            }
        });
        if (getLog().getParents().length < 2) {
            if (!isInCurrentBranch()) {
                actions.add(new AbstractAction(NbBundle.getMessage(CherryPickAction.class, "LBL_CherryPickAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        final String revision = getLog().getRevision();
                        Utils.post(new Runnable() {

                            @Override
                            public void run () {
                                SystemAction.get(CherryPickAction.class).cherryPick(repositoryRoot, revision);
                            }
                        });
                    }
                });
            }
            actions.add(new AbstractAction(NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_PopupName")) { //NOI18N
                @Override
                public void actionPerformed (ActionEvent e) {
                    ExportCommitAction action = SystemAction.get(ExportCommitAction.class);
                    action.exportCommit(repositoryRoot, getLog().getRevision());
                }
            });
            if (mode != SearchExecutor.Mode.REMOTE_IN) {
                actions.add(new AbstractAction(NbBundle.getMessage(RevertCommitAction.class, "LBL_RevertCommitAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        RevertCommitAction action = SystemAction.get(RevertCommitAction.class);
                        action.revert(repositoryRoot, selectionRoots, getLog().getRevision());
                    }
                });
            }
        }
        return actions.toArray(new Action[0]);
    }

    private String getPreferredRevision () {
        if (preferredRevision == null) {
            for (GitBranch branch : getBranches()) {
                if (branch.getName() != GitBranch.NO_BRANCH) {
                    if (!branch.isRemote()) {
                        preferredRevision = branch.getName();
                        break;
                    } else if (preferredRevision == null) {
                        preferredRevision = branch.getName();
                    }
                }
            }
        }
        if (preferredRevision == null) {
            for (GitTag tag : getTags()) {
                preferredRevision = tag.getTagName();
                break;
            }
        }
        if (preferredRevision == null) {
            preferredRevision = getLog().getRevision();
            preferredRevision = preferredRevision.length() > 7 ? preferredRevision.substring(0, 7) : preferredRevision;
        }
        return preferredRevision;
    }
    
    private boolean isInCurrentBranch () {
        GitBranch activeBranch = RepositoryInfo.getInstance(repositoryRoot).getActiveBranch();
        for (GitBranch b : getLog().getBranches().values()) {
            if (activeBranch.getName().equals(b.getName()) || activeBranch.getId().equals(b.getId())) {
                return true;
            }
        }
        return false;
    }
    
    public class Event implements Comparable<Event> {
        /**
         * The file or folder that this event is about. It may be null if the File cannot be computed.
         */ 
        private final File    file;
    
        private final String path;
        private final Status status;
        private boolean underRoots;
        private final File originalFile;
        private final String originalPath;

        public Event (GitFileInfo changedPath, boolean underRoots) {
            path = changedPath.getRelativePath();
            file = changedPath.getFile();
            originalPath = changedPath.getOriginalPath() == null ? path : changedPath.getOriginalPath();
            originalFile = changedPath.getOriginalFile() == null ? file : changedPath.getOriginalFile();
            status = changedPath.getStatus();
            this.underRoots = underRoots;
        }
        
        private Event (File dummyFile, String dummyPath) {
            this.path = dummyPath;
            this.file = dummyFile;
            this.originalPath = dummyPath;
            this.originalFile = dummyFile;
            this.status = Status.UNKNOWN;
            underRoots = true;
        }

        public RepositoryRevision getLogInfoHeader () {
            return RepositoryRevision.this;
        }

        public File getFile() {
            return file;
        }

        public File getOriginalFile () {
            return originalFile;
        }

        public String getName() {
            return getFile().getName();
        }

        public String getPath() {
            return path;
        }
        
        public char getAction () {
            switch (status) {
                case ADDED:
                    return 'A';
                case MODIFIED:
                    return 'M';
                case RENAMED:
                    return 'R';
                case COPIED:
                    return 'C';
                case REMOVED:
                    return 'D';
                default:
                    return '?';
            }
        }
        
        @Override
        public String toString() {
            return path;
        }

        @Override
        public int compareTo (Event other) {
            int retval = status.compareTo(other.status);
            if (retval == 0) {
                retval = path.compareTo(other.path);
            }
            return retval;
        }

        boolean isUnderRoots () {
            return underRoots;
        }

        String getOriginalPath () {
            return originalPath;
        }
        
        Action[] getActions (boolean forNodes) {
            List<Action> actions = new ArrayList<Action>();
            if (isViewEnabled()) {
                actions.add(getViewAction(forNodes ? null : this));
                actions.add(getAnnotateAction(forNodes ? null : this));
                actions.add(getRevertAction(forNodes ? null : this));
                actions.add(getViewCurrentAction(forNodes ? null : this));
            }
            return actions.toArray(new Action[0]);
        }

        void openFile (boolean showAnnotations, ProgressMonitor pm) {
            try {
                String revision = getLogInfoHeader().getLog().getRevision();
                GitUtils.openInRevision(getFile(), -1, revision, showAnnotations, pm);
            } catch (IOException ex) {
                Logger.getLogger(RepositoryRevision.class.getName()).log(Level.FINE, null, ex);
            }
        }

        private Action getViewAction (Event event) {
            if (event == null) {
                return viewAction;
            } else {
                return new ViewAction(repositoryRoot, event);
            }
        }

        private Action getViewCurrentAction (Event event) {
            if (event == null) {
                return viewCurrentAction;
            } else {
                return new ViewCurrentAction(event.getFile());
            }
        }

        private Action getAnnotateAction (Event event) {
            if (event == null) {
                return annotateAction;
            } else {
                return new AnnotateAction(repositoryRoot, event);
            }
        }

        RevertAction getRevertAction (Event event) {
            if (event == null) {
                return revertAction;
            } else {
                return new RevertAction(repositoryRoot, new File[] { event.getFile() }, event.getLogInfoHeader().getShortRevision() );
            }
        }

        private boolean isViewEnabled () {
            return getFile() != null && getAction() != 'D';
        }
    }

    private abstract static class HistoryEventAction extends AbstractAction implements ContextAwareAction {

        public HistoryEventAction (String name) {
            super(name);
        }

        @Override
        public Action createContextAwareInstance (Lookup actionContext) {
            return createAction(actionContext.lookupAll(RevisionNode.class));
        }

        private Action createAction (Collection<? extends RevisionNode> nodes) {
            List<Event> events = new ArrayList<Event>(nodes.size());
            File root = null;
            for (RevisionNode n : nodes) {
                root = n.getEvent().getLogInfoHeader().getRepositoryRoot();
                if (n.getEvent().isViewEnabled()) {
                    events.add(n.getEvent());
                }
            }
            return createAction(root, events.toArray(new Event[0]));
        }

        protected abstract Action createAction (File repositoryRoot, Event... events);
    }

    private static ViewAction viewAction = new ViewAction();
    private static ViewCurrentAction viewCurrentAction = new ViewCurrentAction();
    private static AnnotateAction annotateAction = new AnnotateAction();
    private static RevertAction revertAction = new RevertAction(0);
    
    private static class ViewAction extends HistoryEventAction {

        Event[] events;
        private File repositoryRoot;
        
        private ViewAction () {
            super(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")); //NOI18N
        }

        private ViewAction (File repositoryRoot, Event... events) {
            this();
            this.events = events;
            this.repositoryRoot = repositoryRoot;
        }

        @Override
        public boolean isEnabled () {
            return events.length > 0;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            new GitProgressSupport() {
                @Override
                protected void perform () {
                    for (Event ev : events) {
                        if (ev.isViewEnabled()) {
                            ev.openFile(false, getProgressMonitor());
                        }
                    }
                }
            }.start(Git.getInstance().getRequestProcessor(), repositoryRoot, NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
        }

        @Override
        protected Action createAction (File repositoryRoot, Event... events) {
            return new ViewAction(repositoryRoot, events);
        }
    }
    
    @NbBundle.Messages({
        "CTL_Action.ViewCurrent.name=View Current"
    })
    private static class ViewCurrentAction extends HistoryEventAction {

        File[] files;
        
        private ViewCurrentAction () {
            super(Bundle.CTL_Action_ViewCurrent_name());
        }

        private ViewCurrentAction (File... files) {
            this();
            this.files = files;
        }

        @Override
        public boolean isEnabled () {
            return files.length > 0;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            Utils.post(new Runnable() {
                @Override
                public void run () {
                    for (File f : files) {
                        Utils.openFile(FileUtil.normalizeFile(f));
                    }
                }
            });
        }

        @Override
        protected Action createAction (File repositoryRoot, Event... events) {
            Set<File> fileSet = new HashSet<File>(events.length);
            for (Event e : events) {
                if (e.isViewEnabled()) {
                    fileSet.add(e.getFile());
                }
            }
            return new ViewCurrentAction(fileSet.toArray(new File[0]));
        }
    }
    
    private static class AnnotateAction extends HistoryEventAction {

        Event[] events;
        private File repositoryRoot;
        
        private AnnotateAction () {
            super(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ShowAnnotations")); //NOI18N
        }

        private AnnotateAction (File repositoryRoot, Event... events) {
            this();
            this.events = events;
            this.repositoryRoot = repositoryRoot;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            new GitProgressSupport() {
                @Override
                protected void perform () {
                    for (Event ev : events) {
                        ev.openFile(true, getProgressMonitor());
                    }
                }
            }.start(Git.getInstance().getRequestProcessor(), repositoryRoot, NbBundle.getMessage(SummaryView.class, "MSG_SummaryView.openingFilesFromHistory")); //NOI18N
        }

        @Override
        protected Action createAction (File repositoryRoot, Event... events) {
            return new AnnotateAction(repositoryRoot, events);
        }
    }
    
    @NbBundle.Messages({
        "RepositoryRevision.action.RevertTo.single=Revert File",
        "RepositoryRevision.action.RevertTo=Revert Files",
        "RepositoryRevision.action.RevertTo.progress=Reverting Files"
    })
    static class RevertAction extends HistoryEventAction {

        private File[] files;
        private String revision;
        private File repositoryRoot;
        
        private RevertAction (int fileSize) {
            super(fileSize == 1 ? Bundle.RepositoryRevision_action_RevertTo_single() : Bundle.RepositoryRevision_action_RevertTo());
        }

        private RevertAction (File repositoryRoot, File[] files, String revision) {
            this(files.length);
            this.revision = revision;
            this.files = files;
            this.repositoryRoot = repositoryRoot;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            SystemAction.get(RevertChangesAction.class).revertFiles(repositoryRoot, files, revision, Bundle.RepositoryRevision_action_RevertTo_progress());
        }

        @Override
        protected Action createAction (File repositoryRoot, Event... events) {
            String rev = null;
            List<File> fileList = new ArrayList<File>(events.length);
            for (Event e : events) {
                String eventRevision = e.getLogInfoHeader().getShortRevision();
                if (rev == null) {
                    rev = eventRevision;
                } else if (!rev.equals(eventRevision)) {
                    // action disabled for multiple revision
                    rev = null;
                    break;
                }
                if (e.isViewEnabled()) {
                    fileList.add(e.getFile());
                }
            }
            final boolean enbl = rev != null;
            return new RevertAction(repositoryRoot, fileList.toArray(new File[0]), rev) {

                @Override
                public boolean isEnabled () {
                    return enbl;
                }
                
            };
        }
    }
    
    private class Search extends GitProgressSupport {

        @Override
        protected void perform () {
            Map<File, GitFileInfo> files;
            try {
                files = getLog().getModifiedFiles();
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
                files = Collections.<File, GitFileInfo>emptyMap();
            }
            final List<Event> logEvents = prepareEvents(files);
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
                            support.firePropertyChange(RepositoryRevision.PROP_EVENTS_CHANGED, null, new ArrayList<Event>(events));
                        }
                    }
                });
            }
        }

        @Override
        protected void finishProgress () {

        }

        @Override
        protected void startProgress () {

        }

        @Override
        protected ProgressHandle getProgressHandle () {
            return null;
        }

        private void start (RequestProcessor requestProcessor, File repositoryRoot) {
            start(requestProcessor, repositoryRoot, null);
        }

        private List<Event> prepareEvents (Map<File, GitFileInfo> files) {
            final List<Event> logEvents = new ArrayList<Event>(files.size());
            Set<File> renamedFilesOriginals = new HashSet<File>(files.size());
            for (Map.Entry<File, GitFileInfo> e : files.entrySet()) {
                if (e.getValue().getStatus() == Status.RENAMED) {
                    renamedFilesOriginals.add(e.getValue().getOriginalFile());
                }
            }
            
            for (Map.Entry<File, GitFileInfo> e : files.entrySet()) {
                File f = e.getKey();
                if (renamedFilesOriginals.contains(f)) {
                    // lets not track delete part of a rename and display only the rename itself
                    continue;
                }
                GitFileInfo info = e.getValue();
                boolean underRoots = false;
                for (File selectionRoot : selectionRoots) {
                    if (VersioningSupport.isFlat(selectionRoot)) {
                        underRoots = selectionRoot.equals(f.getParentFile());
                    } else {
                        underRoots = Utils.isAncestorOrEqual(selectionRoot, f);
                    }
                    if (underRoots) {
                        break;
                    }
                }
                logEvents.add(new Event(info, underRoots));
            }
            Collections.sort(logEvents);
            return logEvents;
        }
    }
}
