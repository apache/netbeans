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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.subversion.ui.history;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import java.io.File;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.update.RevertModifications;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * Describes log information for a file. This is the result of doing a
 * cvs log command. The fields in instances of this object are populated
 * by response handlers.
 *
 * @author Maros Sandor
 */
final class RepositoryRevision {

    private ISVNLogMessage message;

    private SVNUrl repositoryRootUrl;

    /**
     * List of events associated with the revision.
     */
    private final List<Event> events = new ArrayList<Event>(5);
    private List<Event> fakeRootEvents;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged"; //NOI18N
    private final File[] selectionRoots;
    private final Map<String,File> pathToRoot;
    private final Map<String, SVNRevision> pegRevisions;

    public RepositoryRevision(ISVNLogMessage message, SVNUrl rootUrl, File[] selectionRoots,
            Map<String,File> pathToRoot, Map<String, SVNRevision> pegRevisions) {
        this.message = message;
        this.repositoryRootUrl = rootUrl;
        this.selectionRoots = selectionRoots;
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
        List<Action> actions = new ArrayList<Action>();
        actions.add(new AbstractAction(NbBundle.getMessage(RepositoryRevision.class, "CTL_SummaryView_RollbackChange")) { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                SvnProgressSupport support = new SvnProgressSupport() {
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
        private File    file;

        private ISVNLogMessageChangePath changedPath;

        private String name;
        private String path;
        private boolean underRoots;
        private File originalFile;
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
        public File getFile() {
            return file;
        }

        /** Setter for property file.
         * @param file New value of property file.
         */
        public void setFile(File file) {
            this.file = file;
        }

        public File getOriginalFile () {
            return originalFile;
        }

        public void setOriginalFile (File originalFile) {
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
    
        @NbBundle.Messages({
            "CTL_Action.ViewCurrent.name=View Current"
        })
        Action[] getActions () {
            if (actions == null) {
                actions = new ArrayList<Action>();
                boolean rollbackToEnabled = getFile() != null && getChangedPath().getAction() != 'D';
                boolean rollbackChangeEnabled = getFile() != null && (getChangedPath().getAction() != 'D' || !getFile().exists());
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
                                    Utils.openFile(FileUtil.normalizeFile(getFile()));
                                }
                            });
                        }
                    });
                }
            }
            return actions.toArray(new Action[actions.size()]);
        }

        void viewFile (boolean showAnnotations) {
            File originFile = getFile();
            SVNRevision rev = getLogInfoHeader().getLog().getRevision();
            SVNUrl repoUrl = getLogInfoHeader().getRepositoryRootUrl();
            SVNUrl fileUrl = repoUrl.appendPath(getChangedPath().getPath());
            SvnUtils.openInRevision(originFile, repoUrl, fileUrl, rev, rev, showAnnotations);
        }
        
        void rollback () {
            SvnProgressSupport support = new SvnProgressSupport() {
                @Override
                public void perform() {
                    File file = getFile();
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
            SvnProgressSupport support = new SvnProgressSupport() {
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
        fakeRootEvents = new LinkedList<Event>();
        for (final File selectionRoot : selectionRoots) {
            Event e = new Event(new ISVNLogMessageChangePath() {
                private String path;
                @Override
                public String getPath() {
                    if(path == null) {
                        try {
                            path = SvnUtils.getRelativePath(selectionRoot);
                            if (!path.startsWith("/")) { //NOI18B
                                path = "/" + path; //NOI18B
                            }
                        } catch (SVNClientException ex) {
                            Subversion.LOG.log(Level.INFO, selectionRoot.getAbsolutePath(), ex);
                            path = "/"; //NOI18B
                        }
                    }
                    return path;
                }
                @Override
                public Number getCopySrcRevision() {
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

        @Override
        protected void perform () {
            try {
                SvnClient client = Subversion.getInstance().getClient(repositoryRootUrl, this);
                ISVNLogMessage [] messages = new ISVNLogMessage[0];
                if (pegRevisions == null) {
                    // searching URL
                    messages = client.getLogMessages(repositoryRootUrl, message.getRevision(), message.getRevision());
                } else {
                    // do not call search history for with repo root url, some repositories
                    // may limit access to the root folder
                    for (File f : selectionRoots) {
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
                                    support.firePropertyChange(RepositoryRevision.PROP_EVENTS_CHANGED, null, new ArrayList<Event>(events));
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
                List<Event> events = new ArrayList<Event>(paths.length);
                Set<String> removedPaths = new HashSet<String>(paths.length);
                for (ISVNLogMessageChangePath path : paths) {
                    if (path.getAction() == 'D') {
                        removedPaths.add(path.getPath());
                    }
                }
                for (ISVNLogMessageChangePath path : paths) {
                    boolean underRoots = false;
                    File f = computeFile(path.getPath());
                    if (f != null) {
                        for (File selectionRoot : selectionRoots) {
                            if (VersioningSupport.isFlat(selectionRoot)) {
                                underRoots = selectionRoot.equals(f) || selectionRoot.equals(f.getParentFile());
                            } else {
                                underRoots = Utils.isAncestorOrEqual(selectionRoot, f);
                            }
                            if (underRoots) {
                                break;
                            }
                        }
                    }
                    String action = Character.toString(path.getAction());
                    if (path.getAction() == 'A' && path.getCopySrcPath() != null) {
                        if (removedPaths.contains(path.getCopySrcPath())) {
                            action = "R"; // rename
                        } else {
                            action = "C"; // copied
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

    private File computeFile(String path) {
        for (String s : pathToRoot.keySet()) {
            if (path.startsWith(s)) {
                return new File(pathToRoot.get(s), path.substring(s.length()));
            }
        }
        return null;
    }
}
