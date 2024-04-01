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
package org.netbeans.modules.mercurial.ui.log;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.io.File;
import java.text.DateFormat;
import java.util.List;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.options.AnnotationColorProvider;
import org.netbeans.modules.mercurial.ui.branch.HgBranch;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.diff.DiffSetupSource;
import org.netbeans.modules.mercurial.ui.diff.ExportDiffAction;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.history.AbstractSummaryView;
import org.netbeans.modules.versioning.history.AbstractSummaryView.SummaryViewMaster.SearchHighlight;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * @author Maros Sandor
 */
/**
 * Shows Search History results in a JList.
 * 
 * @author Maros Sandor
 */
final class SummaryView extends AbstractSummaryView implements DiffSetupSource {

    private final SearchHistoryPanel master;
    
    private static final DateFormat defaultFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private static final Color HIGHLIGHT_BRANCH_FG = Color.BLACK;
    private static final Color HIGHLIGHT_TAG_FG = Color.BLACK;
    private static final Color HIGHLIGHT_BRANCH_BG = Color.decode("0xd5dde6"); //NOI18N
    private static final Color HIGHLIGHT_BRANCH_HEAD_BG = Color.decode("0xaaffaa"); //NOI18N
    private static final Color HIGHLIGHT_TAG_BG = Color.decode("0xffffaa"); //NOI18N
    
    static final class HgLogEntry extends AbstractSummaryView.LogEntry implements PropertyChangeListener {

        private final RepositoryRevision revision;
        private List<Event> events = new ArrayList<Event>(10);
        private List<Event> dummyEvents;
        private final SearchHistoryPanel master;
        private String complexRevision;
        private final PropertyChangeListener list;
        private Collection<RevisionHighlight> complexRevisionHighlights;
    
        public HgLogEntry (RepositoryRevision revision, SearchHistoryPanel master) {
            this.revision = revision;
            this.master = master;
            this.dummyEvents = Collections.<Event>emptyList();
            if (revision.isEventsInitialized()) {
                refreshEvents();
                list = null;
            } else {
                prepareDummyEvents();
                revision.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list = WeakListeners.propertyChange(this, revision));
            }
        }

        @Override
        public Collection<Event> getEvents () {
            return events;
        }

        @Override
        public Collection<Event> getDummyEvents () {
            return dummyEvents;
        }

        @Override
        public String getAuthor () {
            return revision.getLog().getAuthor();
        }

        @Override
        public String getDate () {
            Date date = revision.getLog().getDate();
            return date != null ? defaultFormat.format(date) : null;
        }

        @Override
        public String getRevision () {
            if (complexRevision == null) {
                complexRevisionHighlights = new ArrayList<RevisionHighlight>(revision.getLog().getBranches().length + revision.getLog().getTags().length + 1);
                StringBuilder sb = new StringBuilder(revision.getLog().getRevisionNumber()).append(" ("); //NOI18N
                int pos = sb.length();
                StringBuilder labelBuilder = new StringBuilder();
                // add branch labels
                if (revision.getLog().getBranches().length == 0 && revision.isHeadOfBranch(HgBranch.DEFAULT_NAME)) {
                    complexRevisionHighlights.add(new RevisionHighlight(pos + labelBuilder.length(), HgBranch.DEFAULT_NAME.length(), HIGHLIGHT_BRANCH_FG, HIGHLIGHT_BRANCH_HEAD_BG));
                    labelBuilder.append(HgBranch.DEFAULT_NAME).append(' ');
                } else {
                    for (String s : revision.getLog().getBranches()) {
                        complexRevisionHighlights.add(new RevisionHighlight(pos + labelBuilder.length(), s.length(), HIGHLIGHT_BRANCH_FG, 
                                revision.isHeadOfBranch(s) ? HIGHLIGHT_BRANCH_HEAD_BG : HIGHLIGHT_BRANCH_BG));
                        labelBuilder.append(s).append(' ');
                    }
                }
                // add tag labels
                for (String s : revision.getLog().getTags()) {
                    complexRevisionHighlights.add(new RevisionHighlight(pos + labelBuilder.length(), s.length(), HIGHLIGHT_TAG_FG, HIGHLIGHT_TAG_BG));
                    labelBuilder.append(s).append(' ');
                }
                if (labelBuilder.length() == 0) {
                    labelBuilder.append(revision.getLog().getCSetShortID());
                } else {
                    labelBuilder.append(revision.getLog().getCSetShortID().substring(0, 7));
                }
                complexRevision = sb.append(labelBuilder).append(")").toString(); //NOI18N
            }
            return complexRevision;
        }

        @Override
        protected Collection<RevisionHighlight> getRevisionHighlights () {
            getRevision();
            return complexRevisionHighlights;
        }

        @Override
        public String getMessage () {
            return revision.getLog().getMessage();
        }

        @Override
        public Action[] getActions () {
            List<Action> actions = new ArrayList<Action>();
            if (!master.isIncomingSearch()) {
                actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffRevision")) { //NOI18N

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(master, revision);
                    }
                });
            }
            actions.addAll(Arrays.asList(revision.getActions()));
            return actions.toArray(new Action[0]);
        }

        @Override
        public String toString () {
            return revision.toString();
        }

        @Override
        protected void expand () {
            revision.expandEvents();
        }

        @Override
        protected void cancelExpand () {
            revision.cancelExpand();
        }

        @Override
        protected boolean isEventsInitialized () {
            return revision.isEventsInitialized();
        }

        @Override
        public boolean isVisible () {
            return master.applyFilter(revision);
        }

        @Override
        protected boolean isLessInteresting () {
            return getRepositoryRevision().getLog().isMerge();
        }

        RepositoryRevision getRepositoryRevision () {
            return revision;
        }

        void prepareDummyEvents () {
            ArrayList<Event> evts = new ArrayList<Event>(revision.getDummyEvents().length);
            for (RepositoryRevision.Event event : revision.getDummyEvents()) {
                evts.add(new HgLogEvent(master, event));
            }
            dummyEvents = evts;
        }

        void refreshEvents () {
            ArrayList<Event> evts = new ArrayList<Event>(revision.getEvents().length);
            for (RepositoryRevision.Event event : revision.getEvents()) {
                evts.add(new HgLogEvent(master, event));
            }
            List<Event> newEvents = new ArrayList<Event>(evts);
            dummyEvents.clear();
            events = evts;
            eventsChanged(null, newEvents);
        }

        @Override
        public void propertyChange (PropertyChangeEvent evt) {
            if (RepositoryRevision.PROP_EVENTS_CHANGED.equals(evt.getPropertyName()) && revision == evt.getSource()) {
                refreshEvents();
            }
        }
    }
    
    static class HgLogEvent extends AbstractSummaryView.LogEntry.Event {

        private final RepositoryRevision.Event event;
        private final SearchHistoryPanel master;

        HgLogEvent (SearchHistoryPanel master, RepositoryRevision.Event event) {
            this.master = master;
            this.event = event;
        }

        @Override
        public String getPath () {
            return event.getChangedPath().getPath();
        }

        @Override
        public String getOriginalPath () {
            return event.getChangedPath().getCopySrcPath();
        }

        @Override
        public String getAction () {
            return Character.toString(event.getChangedPath().getAction());
        }
        
        public RepositoryRevision.Event getEvent() {
            return event;
        }

        @Override
        public Action[] getUserActions () {
            List<Action> actions = new ArrayList<Action>();
            if (!master.isIncomingSearch()) {
                actions.add(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious")) { // NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(master, event);
                    }
                });
                actions.addAll(Arrays.asList(event.getActions()));
            }
            return actions.toArray(new Action[0]);
        }

        @Override
        public boolean isVisibleByDefault () {
            return master.isShowInfo() || event.isUnderRoots();
        }

        @Override
        public String toString () {
            return event.toString();
        }
    }
    
    public SummaryView (SearchHistoryPanel master, List<? extends LogEntry> results) {
        super(createViewSummaryMaster(master), results);
        this.master = master;
    }

    private static SummaryViewMaster createViewSummaryMaster (final SearchHistoryPanel master) {
        final Map<String, String> colors = new HashMap<String, String>();
        colors.put("A", HgUtils.getColorString(AnnotationColorProvider.getInstance().ADDED_LOCALLY_FILE.getActualColor()));
        colors.put("C", HgUtils.getColorString(AnnotationColorProvider.getInstance().COPIED_LOCALLY_FILE.getActualColor()));
        colors.put("R", HgUtils.getColorString(AnnotationColorProvider.getInstance().MOVED_LOCALY_FILE.getActualColor()));
        colors.put("M", HgUtils.getColorString(AnnotationColorProvider.getInstance().MODIFIED_LOCALLY_FILE.getActualColor()));
        colors.put("D", HgUtils.getColorString(AnnotationColorProvider.getInstance().REMOVED_LOCALLY_FILE.getActualColor()));

        return new SummaryViewMaster() {

            @Override
            public JComponent getComponent () {
                return master;
            }

            @Override
            public File[] getRoots () {
                return master.getRoots();
            }

            @Override
            public Collection<SearchHighlight> getSearchHighlights () {
                return master.getSearchHighlights();
            }

            @Override
            public Map<String, String> getActionColors () {
                return colors;
            }

            @Override
            public void getMoreResults (PropertyChangeListener callback, int count) {
                master.getMoreRevisions(callback, count);
            }

            @Override
            public boolean hasMoreResults () {
                return master.hasMoreResults();
            }
            
        };
    
    }

    @Override
    public Collection<Setup> getSetups() {
        Node [] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes.length == 0) {
            List<RepositoryRevision> results = master.getResults();
            return master.getSetups(results.toArray(new RepositoryRevision[0]), new RepositoryRevision.Event[0]);
        }
    
        Set<RepositoryRevision.Event> events = new HashSet<RepositoryRevision.Event>();
        Set<RepositoryRevision> revisions = new HashSet<RepositoryRevision>();

        Object [] sel = getSelection();
        for (Object revCon : sel) {
            if (revCon instanceof RepositoryRevision) {
                revisions.add((RepositoryRevision) revCon);
            } else {
                events.add((RepositoryRevision.Event) revCon);
            }
        }
        return master.getSetups(revisions.toArray(new RepositoryRevision[0]), events.toArray(new RepositoryRevision.Event[0]));
    }

    @Override
    public String getSetupDisplayName() {
        return null;
    }

    @Override
    @NbBundle.Messages({
        "LBL_SummaryView.action.diffRevisions=Diff Selected Revisions",
        "LBL_SummaryView.action.diffFiles=Open Selected Files in Diff Tab"
    })
    protected void onPopup (JComponent invoker, Point p, final Object[] selection) {
        JPopupMenu menu = new JPopupMenu();
        
        String previousRevision = null;
        final RepositoryRevision container;
        final RepositoryRevision.Event[] drev;

        boolean revisionsSelected = false;
        boolean missingFile = false;        
        final boolean singleSelection = selection.length == 1;
        boolean oneRevisionMultiselected = true;
        
        for (Object o : selection) {
            revisionsSelected = true;
            if (!(o instanceof HgLogEntry)) {
                revisionsSelected = false;
            }
        }
        if (revisionsSelected) {
            container = ((HgLogEntry) selection[0]).revision;
            oneRevisionMultiselected = false;
            drev = new RepositoryRevision.Event[0];
        } else {
            drev = new RepositoryRevision.Event[selection.length];

            for(int i = 0; i < selection.length; i++) {
                if (!(selection[i] instanceof HgLogEvent)) {
                    return;
                }
                drev[i] = ((HgLogEvent) selection[i]).getEvent();
                
                if(!missingFile && drev[i].getFile() == null) {
                    missingFile = true;
                }
                if(oneRevisionMultiselected && i > 0 && 
                   !drev[0].getLogInfoHeader().getLog().getRevisionNumber().equals(drev[i].getLogInfoHeader().getLog().getRevisionNumber())) 
                {
                    oneRevisionMultiselected = false;
                }                
            }                
            container = drev[0].getLogInfoHeader();
        }
        long revision = Long.parseLong(container.getLog().getRevisionNumber());

        final boolean revertToEnabled = !missingFile && !revisionsSelected && oneRevisionMultiselected;
        final boolean viewEnabled = selection.length == 1 && !revisionsSelected && drev[0].getFile() != null && drev[0].getChangedPath().getAction() != HgLogMessage.HgDelStatus;
        final boolean annotationsEnabled = viewEnabled;
        final boolean diffToPrevEnabled = selection.length == 1;
        
        if (master.isIncomingSearch()) {
            if (revisionsSelected && singleSelection) {
                for (Action a : container.getActions()) {
                    menu.add(new JMenuItem(a));
                }
            } else {
                return;
            }
        } else {
            if (singleSelection || !revisionsSelected) {
                if (revision > 0) {
                    menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", "" + previousRevision )) { // NOI18N
                        {
                            setEnabled(diffToPrevEnabled);
                        }
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            diffPrevious(master, selection[0]);
                        }
                    }));
                }
            }

            if (revisionsSelected) {
                if (singleSelection) {
                    for (Action a : container.getActions()) {
                        menu.add(new JMenuItem(a));
                    }
                } else if (selection.length == 2) {
                    menu.add(new JMenuItem(new AbstractAction(Bundle.LBL_SummaryView_action_diffRevisions()) {
                        @Override
                        public void actionPerformed (ActionEvent e) {
                            File[] roots = master.getRoots();
                            List<Node> nodes = new ArrayList<Node>(roots.length);
                            for (final File root : roots) {
                                nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(root)) {
                                    @Override
                                    public String getDisplayName () {
                                        return root.getName();
                                    }
                                });
                            }
                            HgLogMessage info1 = ((HgLogEntry) selection[0]).getRepositoryRevision().getLog();
                            HgLogMessage info2 = ((HgLogEntry) selection[1]).getRepositoryRevision().getLog();
                            SystemAction.get(DiffAction.class).diff(master.getRoots(),
                                    info2.getHgRevision(),
                                    info1.getHgRevision(),
                                    Utils.getContextDisplayName(VCSContext.forNodes(nodes.toArray(new Node[0]))),
                                    false, true);
                        }
                    }));
                }
            } else {
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackTo", "" + revision)) { // NOI18N
                    {                    
                        setEnabled(revertToEnabled);
                    }
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        revertModifications(selection);
                    }                
                }));

                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View")) { // NOI18N
                    {
                        setEnabled(viewEnabled);
                    }
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                drev[0].viewFile(false);
                            }
                        });
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ShowAnnotations")) { // NOI18N
                    {
                        setEnabled(annotationsEnabled);
                    }
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                drev[0].viewFile(true);
                            }
                        });
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(Bundle.CTL_Action_ViewCurrent_name()) {
                    {
                        setEnabled(viewEnabled);
                    }
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.openFile(FileUtil.normalizeFile(drev[0].getFile()));
                            }
                        });
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_ExportFileDiff")) { // NOI18N
                    {
                        setEnabled(viewEnabled);
                    }
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        exportFileDiff(drev[0]);
                    }
                }));
                if (drev.length == 2 && drev[0].getLogInfoHeader() != drev[1].getLogInfoHeader()) {
                    menu.add(new JMenuItem(new AbstractAction(Bundle.LBL_SummaryView_action_diffFiles()) {
                        @Override
                        public void actionPerformed (ActionEvent e) {
                            master.showDiff(drev);
                        }
                    }));
                }
            }
        }

        menu.show(invoker, p.x, p.y);
    }

    public void revertModifications(Object[] selection) {
        Set<RepositoryRevision.Event> events = new HashSet<RepositoryRevision.Event>();
        Set<RepositoryRevision> revisions = new HashSet<RepositoryRevision>();
        for (Object o : selection) {
            if (o instanceof RepositoryRevision) {
                revisions.add((RepositoryRevision) o);
            } else if (o instanceof HgLogEntry) {
                revisions.add(((HgLogEntry) o).getRepositoryRevision());
            } else if (o instanceof HgLogEntry.Event) {
                events.add(((HgLogEvent) o).getEvent());
            } else {
                events.add((RepositoryRevision.Event) o);
            }
        }
        revert(revisions.toArray(new RepositoryRevision[0]), (RepositoryRevision.Event[]) events.toArray(new RepositoryRevision.Event[0]));
    }

    static void revert(final RepositoryRevision [] revisions, final RepositoryRevision.Event [] events) {
        File root;
        if(revisions == null || revisions.length == 0){
            if(events == null || events.length == 0 || events[0].getLogInfoHeader() == null) return;
            root = events[0].getLogInfoHeader().getRepositoryRoot();
        }else{
            root = revisions[0].getRepositoryRoot();
        }
        
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(root);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                revertImpl(revisions, events, this);
            }
        };
        support.start(rp, root, NbBundle.getMessage(SummaryView.class, "MSG_Revert_Progress")); // NOI18N
    }

    private static void revertImpl(RepositoryRevision[] revisions, RepositoryRevision.Event[] events, HgProgressSupport progress) {
        List<File> revertFiles = new ArrayList<File>();
        boolean doBackup = HgModuleConfig.getDefault().getBackupOnRevertModifications();
        if (revisions != null) {
            for (RepositoryRevision revision : revisions) {
                File root = revision.getRepositoryRoot();
                for (RepositoryRevision.Event event : revision.getEvents()) {
                    if (event.getFile() == null) {
                        continue;
                    }
                    revertFiles.add(event.getFile());
                }
                RevertModificationsAction.performRevert(
                        root, revision.getLog().getRevisionNumber(), revertFiles, doBackup, false, progress.getLogger());
                revertFiles.clear();
            }
        }
        
        Map<File, List<RepositoryRevision.Event>> revertMap = new HashMap<File, List<RepositoryRevision.Event>>();
        for (RepositoryRevision.Event event : events) {
            if (event.getFile() == null) continue;
         
            File root = Mercurial.getInstance().getRepositoryRoot(event.getFile());
            List<RepositoryRevision.Event> revEvents = revertMap.get(root);
            if(revEvents == null){
                revEvents = new ArrayList<RepositoryRevision.Event>();
                revertMap.put(root, revEvents);
            }
            revEvents.add(event);            
        }
        if (events != null && events.length > 0 && !revertMap.isEmpty()) {
            Set<File> roots = revertMap.keySet();
            for(File root: roots){
                List<RepositoryRevision.Event> revEvents = revertMap.get(root);
                for(RepositoryRevision.Event event: revEvents){
                    if (event.getFile() == null) continue;
                    revertFiles.add(event.getFile());
                }
                if(revEvents != null && !revEvents.isEmpty()){
                    // Assuming all files in a given repository reverting to same revision
                    RevertModificationsAction.performRevert(
                        root, revEvents.get(0).getLogInfoHeader().getLog().getRevisionNumber(), revertFiles, doBackup, false, progress.getLogger());
                }
            }                       
        }
        
    }

    private static void diffPrevious (SearchHistoryPanel master, Object o) {
        if (o instanceof RepositoryRevision.Event) {
            RepositoryRevision.Event drev = (RepositoryRevision.Event) o;
            master.showDiff(drev);
        } else if (o instanceof HgLogEntry.Event) {
            RepositoryRevision.Event drev = ((HgLogEvent) o).getEvent();
            master.showDiff(drev);
        } else if (o instanceof HgLogEntry) {
            RepositoryRevision container = ((HgLogEntry) o).getRepositoryRevision();
            master.showDiff(container);
        } else {
            RepositoryRevision container = (RepositoryRevision) o;
            master.showDiff(container);
        }
    }
    
    private static void exportFileDiff(RepositoryRevision.Event drev) {
        ExportDiffAction.exportDiffFileRevision(drev);
    }
}
