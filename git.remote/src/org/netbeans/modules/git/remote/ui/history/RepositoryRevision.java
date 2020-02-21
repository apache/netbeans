/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.git.remote.ui.history;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.branch.CherryPickAction;
import org.netbeans.modules.git.remote.ui.checkout.CheckoutRevisionAction;
import org.netbeans.modules.git.remote.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.remote.ui.diff.ExportCommitAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.revert.RevertCommitAction;
import org.netbeans.modules.git.remote.ui.tag.CreateTagAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
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
    private final List<Event> events = new ArrayList<>(5);
    private final List<Event> dummyEvents;
    private final Map<VCSFileProxy, String> commonAncestors = new HashMap<>();
    private final Set<GitTag> tags;
    private final Set<GitBranch> branches;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged"; //NOI18N
    private final VCSFileProxy repositoryRoot;
    private final VCSFileProxy[] selectionRoots;
    private String preferredRevision;
    private final SearchExecutor.Mode mode;

    RepositoryRevision (GitRevisionInfo message, VCSFileProxy repositoryRoot, VCSFileProxy[] selectionRoots,
            Set<GitTag> tags, Set<GitBranch> branches, VCSFileProxy dummyFile, String dummyFileRelativePath,
            SearchExecutor.Mode mode) {
        this.message = message;
        this.repositoryRoot = repositoryRoot;
        this.selectionRoots = selectionRoots;
        this.tags = tags;
        this.branches = branches;
        support = new PropertyChangeSupport(this);
        dummyEvents = new ArrayList<>(1);
        if (dummyFile != null && dummyFileRelativePath != null) {
            dummyEvents.add(new Event(dummyFile, dummyFileRelativePath));
        }
        this.mode = mode;
    }

    public Event[] getEvents() {
        return events.toArray(new Event[events.size()]);
    }

    Event[] getDummyEvents () {
        return dummyEvents.toArray(new Event[dummyEvents.size()]);
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

    String getAncestorCommit (VCSFileProxy file, GitClient client, ProgressMonitor pm) throws GitException {
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
        return branches == null ? new GitBranch[0] : branches.toArray(new GitBranch[branches.size()]);
    }

    public GitTag[] getTags () {
        return tags == null ? new GitTag[0] : tags.toArray(new GitTag[tags.size()]);
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

    VCSFileProxy getRepositoryRoot () {
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
        List<Action> actions = new ArrayList<>();
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
        return actions.toArray(new Action[actions.size()]);
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
        private final VCSFileProxy    file;
    
        private final String path;
        private final Status status;
        private boolean underRoots;
        private final VCSFileProxy originalFile;
        private final String originalPath;

        public Event (GitFileInfo changedPath, boolean underRoots) {
            path = changedPath.getRelativePath();
            file = changedPath.getFile();
            originalPath = changedPath.getOriginalPath() == null ? path : changedPath.getOriginalPath();
            originalFile = changedPath.getOriginalFile() == null ? file : changedPath.getOriginalFile();
            status = changedPath.getStatus();
            this.underRoots = underRoots;
        }
        
        private Event (VCSFileProxy dummyFile, String dummyPath) {
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

        public VCSFileProxy getFile() {
            return file;
        }

        public VCSFileProxy getOriginalFile () {
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
            List<Action> actions = new ArrayList<>();
            if (isViewEnabled()) {
                actions.add(getViewAction(forNodes ? null : this));
                actions.add(getAnnotateAction(forNodes ? null : this));
                actions.add(getRevertAction(forNodes ? null : this));
                actions.add(getViewCurrentAction(forNodes ? null : this));
            }
            return actions.toArray(new Action[actions.size()]);
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
                return new RevertAction(repositoryRoot, new VCSFileProxy[] { event.getFile() }, event.getLogInfoHeader().getShortRevision() );
            }
        }

        private boolean isViewEnabled () {
            return getFile() != null && getAction() != 'D';
        }
    }

    private static abstract class HistoryEventAction extends AbstractAction implements ContextAwareAction {

        public HistoryEventAction (String name) {
            super(name);
        }

        @Override
        public Action createContextAwareInstance (Lookup actionContext) {
            return createAction(actionContext.lookupAll(RevisionNode.class));
        }

        private Action createAction (Collection<? extends RevisionNode> nodes) {
            List<Event> events = new ArrayList<>(nodes.size());
            VCSFileProxy root = null;
            for (RevisionNode n : nodes) {
                root = n.getEvent().getLogInfoHeader().getRepositoryRoot();
                if (n.getEvent().isViewEnabled()) {
                    events.add(n.getEvent());
                }
            }
            return createAction(root, events.toArray(new Event[events.size()]));
        }

        protected abstract Action createAction (VCSFileProxy repositoryRoot, Event... events);
    }

    private static ViewAction viewAction = new ViewAction();
    private static ViewCurrentAction viewCurrentAction = new ViewCurrentAction();
    private static AnnotateAction annotateAction = new AnnotateAction();
    private static RevertAction revertAction = new RevertAction(0);
    
    private static class ViewAction extends HistoryEventAction {

        Event[] events;
        private VCSFileProxy repositoryRoot;
        
        private ViewAction () {
            super(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")); //NOI18N
        }

        private ViewAction (VCSFileProxy repositoryRoot, Event... events) {
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
        protected Action createAction (VCSFileProxy repositoryRoot, Event... events) {
            return new ViewAction(repositoryRoot, events);
        }
    }
    
    @NbBundle.Messages({
        "CTL_Action.ViewCurrent.name=View Current"
    })
    private static class ViewCurrentAction extends HistoryEventAction {

        VCSFileProxy[] files;
        
        private ViewCurrentAction () {
            super(Bundle.CTL_Action_ViewCurrent_name());
        }

        private ViewCurrentAction (VCSFileProxy... files) {
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
                    for (VCSFileProxy f : files) {
                        VCSFileProxySupport.openFile(f.normalizeFile());
                    }
                }
            });
        }

        @Override
        protected Action createAction (VCSFileProxy repositoryRoot, Event... events) {
            Set<VCSFileProxy> fileSet = new HashSet<>(events.length);
            for (Event e : events) {
                if (e.isViewEnabled()) {
                    fileSet.add(e.getFile());
                }
            }
            return new ViewCurrentAction(fileSet.toArray(new VCSFileProxy[fileSet.size()]));
        }
    }
    
    private static class AnnotateAction extends HistoryEventAction {

        Event[] events;
        private VCSFileProxy repositoryRoot;
        
        private AnnotateAction () {
            super(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ShowAnnotations")); //NOI18N
        }

        private AnnotateAction (VCSFileProxy repositoryRoot, Event... events) {
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
        protected Action createAction (VCSFileProxy repositoryRoot, Event... events) {
            return new AnnotateAction(repositoryRoot, events);
        }
    }
    
    @NbBundle.Messages({
        "RepositoryRevision.action.RevertTo.single=Revert File",
        "RepositoryRevision.action.RevertTo=Revert Files",
        "RepositoryRevision.action.RevertTo.progress=Reverting Files"
    })
    static class RevertAction extends HistoryEventAction {

        private VCSFileProxy[] files;
        private String revision;
        private VCSFileProxy repositoryRoot;
        
        private RevertAction (int fileSize) {
            super(fileSize == 1 ? Bundle.RepositoryRevision_action_RevertTo_single() : Bundle.RepositoryRevision_action_RevertTo());
        }

        private RevertAction (VCSFileProxy repositoryRoot, VCSFileProxy[] files, String revision) {
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
        protected Action createAction (VCSFileProxy repositoryRoot, Event... events) {
            String rev = null;
            List<VCSFileProxy> fileList = new ArrayList<>(events.length);
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
            return new RevertAction(repositoryRoot, fileList.toArray(new VCSFileProxy[fileList.size()]), rev) {

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
            Map<VCSFileProxy, GitFileInfo> files;
            try {
                files = getLog().getModifiedFiles();
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
                files = Collections.<VCSFileProxy, GitFileInfo>emptyMap();
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
                            support.firePropertyChange(RepositoryRevision.PROP_EVENTS_CHANGED, null, new ArrayList<>(events));
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

        private void start (RequestProcessor requestProcessor, VCSFileProxy repositoryRoot) {
            start(requestProcessor, repositoryRoot, null);
        }

        private List<Event> prepareEvents (Map<VCSFileProxy, GitFileInfo> files) {
            final List<Event> logEvents = new ArrayList<>(files.size());
            Set<VCSFileProxy> renamedFilesOriginals = new HashSet<>(files.size());
            for (Map.Entry<VCSFileProxy, GitFileInfo> e : files.entrySet()) {
                if (e.getValue().getStatus() == Status.RENAMED) {
                    renamedFilesOriginals.add(e.getValue().getOriginalFile());
                }
            }
            
            for (Map.Entry<VCSFileProxy, GitFileInfo> e : files.entrySet()) {
                VCSFileProxy f = e.getKey();
                if (renamedFilesOriginals.contains(f)) {
                    // lets not track delete part of a rename and display only the rename itself
                    continue;
                }
                GitFileInfo info = e.getValue();
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
                logEvents.add(new Event(info, underRoots));
            }
            Collections.sort(logEvents);
            return logEvents;
        }
    }
}
