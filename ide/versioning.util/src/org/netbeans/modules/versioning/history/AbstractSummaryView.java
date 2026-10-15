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
package org.netbeans.modules.versioning.history;

import org.openide.util.NbBundle;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.util.VCSHyperlinkSupport;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;

/**
 * @author Maros Sandor
 */
/**
 * Shows Search History results in a JList.
 *
 * @author Maros Sandor
 */
public abstract class AbstractSummaryView implements MouseListener, MouseMotionListener, PropertyChangeListener {

    static final Logger LOG = Logger.getLogger("org.netbeans.modules.versioning.util.AbstractSummaryView"); //NOI18N
    public static final String PROP_REVISIONS_ADDED = "propRevisionsAdded"; //NOI18N
    private final PropertyChangeListener list;
    private final ExpandCollapseGeneralAction expandCollapseAction;
    private JList<Item> resultsList;
    private final JScrollPane scrollPane;

    private final VCSHyperlinkSupport linkerSupport = new VCSHyperlinkSupport();
    private static final int NEXT_FILES_INITIAL_PAGING = 50;

    public AbstractSummaryView(SummaryViewMaster master, final List<? extends LogEntry> results) {
        this.master = master;
        list = WeakListeners.propertyChange(this, null);

        resultsList = new JList<>(new DefaultListModel<>());
        resultsList.setModel(new SummaryListModel(results, master.hasMoreResults()));
        resultsList.setCellRenderer(new SummaryCellRenderer(this, linkerSupport));
        resultsList.setFixedCellHeight(-1);

        resultsList.addMouseListener(this);
        resultsList.addMouseMotionListener(this);
        resultsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AbstractSummaryView.class, "ACSN_SummaryView_List")); //NOI18N
        resultsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AbstractSummaryView.class, "ACSD_SummaryView_List")); //NOI18N
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsList.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshView(false);
            }
        });

        resultsList.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); //NOI18N
        resultsList.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                Point p = org.netbeans.modules.versioning.util.Utils.getPositionForPopup(resultsList);
                Object[] selection = getSelection();
                if (selection.length > 0) {
                    onPopup(resultsList, p, selection);
                }
            }
        });
        ExpandAction expand = new ExpandAction();
        CollapseAction collapse = new CollapseAction();
        expandCollapseAction = new ExpandCollapseGeneralAction(expand, collapse);
        resultsList.getActionMap().put("selectNextColumn", expand);
        resultsList.getActionMap().put("selectPreviousColumn", collapse);
        resultsList.getActionMap().put("addToSelection", new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                List<?> selection = resultsList.getSelectedValuesList();
                if (selection.size() == 1) {
                    if (selection.get(0) instanceof ShowAllEventsItem) {
                        showRemainingFiles(((ShowAllEventsItem) selection.get(0)).getParent(), true);
                    } else if (selection.get(0) instanceof ShowLessEventsItem) {
                        showRemainingFiles(((ShowLessEventsItem) selection.get(0)).getParent(), false);
                    } else if (selection.get(0) instanceof MoreRevisionsItem) {
                        moreRevisions(10);
                    }
                }
            }
        });

        scrollPane.validate();
    }

    private List<Item> initializeResults (final List<? extends LogEntry> results, boolean hasMoreResults) {
        List<Item> toDisplay = expandResults(results);
        if (hasMoreResults) {
            toDisplay.add(new MoreRevisionsItem());
        }
        return toDisplay;
    }

    private List<Item> expandResults (List<? extends LogEntry> results) {
        List<Item> newResults = new ArrayList<>(results.size() * 6);
        for (LogEntry le : results) {
            le.removePropertyChangeListener(LogEntry.PROP_EVENTS_CHANGED, list);
            le.addPropertyChangeListener(LogEntry.PROP_EVENTS_CHANGED, list);
            RevisionItem item = new RevisionItem(le);
            newResults.add(item);
            newResults.add(new ActionsItem(item));
            if (!le.isEventsInitialized()) {
                newResults.add(new LoadingEventsItem(item));
            }
        }
        return newResults;
    }
    
    public final void requestFocusInWindow () {
        resultsList.requestFocusInWindow();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            expandCollapseAction.actionPerformed(new ActionEvent(resultsList, ActionEvent.ACTION_PERFORMED, null));
        } else {
            int idx = resultsList.locationToIndex(e.getPoint());
            if (idx == -1) return;
            Rectangle rect = resultsList.getCellBounds(idx, idx);
            Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
            linkerSupport.mouseClicked(p, getLinkerIdentFor(idx));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resultsList.setToolTipText(null);

        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        linkerSupport.mouseMoved(p, resultsList, getLinkerIdentFor(idx));
    }

    String getLinkerIdentFor (int index) {
        Item item = ((SummaryListModel) resultsList.getModel()).getElementAt(index);
        return item.getItemId();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // not interested
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    public final void refreshView () {
        refreshView(true);
    }

    private void refreshView (boolean refreshModel) {
        ListCellRenderer r = resultsList.getCellRenderer();
        resultsList.setCellRenderer(null);
        resultsList.setCellRenderer(r);
        if (refreshModel) {
            ((SummaryListModel) resultsList.getModel()).refreshModel();
        }
        scrollPane.revalidate();
    }
    
    private void onPopup(MouseEvent e) {
        Object[] selection = getSelection(e.getPoint());
        if (selection.length > 0) {
            onPopup(resultsList, e.getPoint(), selection);
        }
    }

    protected abstract void onPopup(JComponent invoker, Point p, Object[] selection);

    protected final Object[] getSelection () {
        List<Item> sel = resultsList.getSelectedValuesList();
        Object[] selection = new Object[sel.size()];
        for (int i = 0; i < sel.size(); ++i) {
            Item item = sel.get(i);
            Object o = item.getUserData();
            if (o == null) {
                // unallowed selection
                return new Object[0];
            }
            selection[i] = o;
        }
        return selection;
    }

    private Object[] getSelection (Point p) {
        int[] selected = resultsList.getSelectedIndices();
        int idx = resultsList.locationToIndex(p);
        if (idx == -1) {
            return new Object[0];
        }
        boolean contains = false;
        for (int i : selected) {
            if (i == idx) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            resultsList.setSelectedIndex(idx);
        }
        return getSelection();
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    File getRoot() {
        if(master.getRoots() == null || master.getRoots().length == 0) {
            return null;
        }
        return master.getRoots()[0];
    }

    Map<String, String> getActionColors() {
        return master.getActionColors();
    }

    SummaryViewMaster getMaster () {
        return master;
    }

    void itemChanged (Point p) {
        int index = resultsList.locationToIndex(p);
        if (index != -1) {
            ((SummaryListModel) resultsList.getModel()).fireChange(index);
            ((SummaryListModel) resultsList.getModel()).refreshModel();
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (LogEntry.PROP_EVENTS_CHANGED.equals(evt.getPropertyName())) {
            LogEntry src = (LogEntry) evt.getSource();
            refreshEvents(src);
        }
    }

    private void refreshEvents (final LogEntry src) {
        Mutex.EVENT.readAccess(() -> {
            ((SummaryListModel) resultsList.getModel()).addEvents(src);
        });
    }

    void showRemainingFiles (RevisionItem item, boolean showMore) {
        assert EventQueue.isDispatchThread();
        JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
        Cursor cursor = mainWindow.getCursor();
        try {
            if (showMore) {
                item.showMoreFiles();
            } else {
                item.showLessFiles();
            }
            mainWindow.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            mainWindow.getGlassPane().setVisible(true);
            ((SummaryListModel) resultsList.getModel()).refreshModel();
            if (resultsList.getSelectedIndices().length == 1) {
                resultsList.ensureIndexIsVisible(resultsList.getSelectedIndices()[0]);
            }
        } finally {
            mainWindow.getGlassPane().setCursor(cursor);
            mainWindow.getGlassPane().setVisible(false);
        }
    }

    void moreRevisions (Integer count) {
        master.getMoreResults(this, count);
    }
    
    public void entriesChanged (final List<? extends LogEntry> entries) {
        Mutex.EVENT.readAccess(() -> {
            List<Item> selection = resultsList.getSelectedValuesList();
            if (selection.size() > 0 && selection.get(selection.size() - 1) instanceof MoreRevisionsItem) {
                int lastIndex = ((SummaryListModel) resultsList.getModel()).getSize() - 1;
                resultsList.getSelectionModel().removeIndexInterval(lastIndex, lastIndex);
            }
            ((SummaryListModel) resultsList.getModel()).addEntries(entries, !master.hasMoreResults());
        });
    }

    public interface SummaryViewMaster {
        public JComponent getComponent();
        /**
         * Returns the roots on which the view was invoked. 
         * Note that the value is used only to retrieve an assotiated bugtracking 
         * system for the need of issue hyperlinks in commit messages. 
         * Thus in case the roots can't be provided it is ok to return null. 
         * 
         * @return 
         */
        public File[] getRoots();
        public Collection<SearchHighlight> getSearchHighlights ();
        public Map<String, String> getActionColors();
        public void getMoreResults(PropertyChangeListener callback, int count);
        public boolean hasMoreResults ();
        
        public static final class SearchHighlight {
            public static enum Kind {
                MESSAGE,
                AUTHOR,
                REVISION,
                FILE
            }
            
            private final Kind kind;
            private final String searchText;

            public SearchHighlight (Kind kind, String searchText) {
                this.kind = kind;
                this.searchText = searchText.toLowerCase();
            }

            public Kind getKind () {
                return kind;
            }

            public String getSearchText () {
                return searchText;
            }

            @Override
            public boolean equals (Object obj) {
                boolean eq = false;
                if (obj instanceof SearchHighlight) {
                    eq = kind == ((SearchHighlight) obj).kind;
                    if (eq) {
                        eq = searchText.equals(((SearchHighlight) obj).searchText);
                    }
                }
                return eq;
            }

            @Override
            public int hashCode () {
                int hash = 5;
                hash = 17 * hash + (this.kind != null ? this.kind.hashCode() : 0);
                hash = 17 * hash + (this.searchText != null ? this.searchText.hashCode() : 0);
                return hash;
            }
            
        }
    }

    private final SummaryViewMaster master;

    public abstract static class LogEntry {
        public static final String PROP_EVENTS_CHANGED = "propEventsChanged"; //NOI18N
        private final PropertyChangeSupport support;

        public LogEntry () {
            support = new PropertyChangeSupport(this);
        }
        
        public abstract Collection<Event> getEvents();
        public Collection<Event> getDummyEvents () {
            return Collections.<Event>emptyList();
        }
        public abstract String getAuthor();
        public abstract String getDate();
        public abstract String getRevision();
        public abstract String getMessage();
        public abstract Action[] getActions();
        public abstract boolean isVisible ();

        protected final void eventsChanged (List<? extends Event> oldEvents, List<? extends Event> newEvents) {
            support.firePropertyChange(PROP_EVENTS_CHANGED, oldEvents, newEvents);
        }
        
        public final void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
            support.addPropertyChangeListener(propertyName, listener);
        }
        public final void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
            support.removePropertyChangeListener(propertyName, listener);
        }

        protected abstract void cancelExpand ();
        protected abstract void expand ();
        protected abstract boolean isEventsInitialized ();

        /**
         * Returns true if the entry is of minor interest. Revisions like merges etc. might be of less importance than others.
         */
        protected abstract boolean isLessInteresting ();
        
        /**
         * Returns a collection of highlights applicable to this entry's revision string
         * Special parts of the revision string (e.g. branches, tags) can be displayed with different colors
         * @return collection of revision string highlights
         */
        protected abstract Collection<RevisionHighlight> getRevisionHighlights ();

        public abstract static class Event {
            public abstract String getPath();
            public abstract String getOriginalPath ();
            public abstract String getAction();
            public abstract Action[] getUserActions();
            public abstract boolean isVisibleByDefault ();
        }
        
        public static final class RevisionHighlight {
            private final int start;
            private final int length;
            private final Color foreground;
            private final Color background;

            public RevisionHighlight (int start, int length, Color foreground, Color background) {
                this.start = start;
                this.length = length;
                this.foreground = foreground;
                this.background = background;
            }

            public Color getBackground () {
                return background;
            }

            public int getLength () {
                return length;
            }

            public Color getForeground () {
                return foreground;
            }

            public int getStart () {
                return start;
            }

        }
    }

    abstract class Item<T> {
        private final T userData;
        private boolean visible = true;
        
        Item (T userData) {
            this.userData = userData;
        }

        final T getUserData () {
            return userData;
        }
        
        boolean isVisible () {
            return visible;
        }
        
        void setVisible (boolean visible) {
            this.visible = visible;
        }

        abstract String getItemId ();
    };
    
    class MoreRevisionsItem extends Item {
        public MoreRevisionsItem () {
            super(null);
        }

        @Override
        String getItemId () {
            return "#MoreRevisions"; //NOI18N
        }
    }

    //TODO record
    static final class MaxPathWidth {
        public final int visiblePaths;
        public final int maxPathWidth;
        public MaxPathWidth(int visiblePaths, int maxPathWidth) {
            this.visiblePaths = visiblePaths;
            this.maxPathWidth = maxPathWidth;
        }
    }

    class RevisionItem extends Item<LogEntry> {
        private final LogEntry entry;
        MaxPathWidth maxPathWidth = null;
        boolean messageExpanded;
        boolean revisionExpanded;
        private boolean viewEventsInitialized;
        private boolean initializingStarted;
        int showingFiles;
        private int nextFilesPaging = NEXT_FILES_INITIAL_PAGING;

        public RevisionItem (LogEntry entry) {
            super(entry);
            this.entry = entry;
        }

        @Override
        String getItemId () {
            return entry.getRevision();
        }
        
        @Override
        boolean isVisible () {
            return entry.isVisible();
        }

        private boolean isAllEventsVisible () {
            return revisionExpanded && showingFiles == -1;
        }

        @Override
        public String toString () {
            return entry.toString();
        }

        void setExpanded (boolean expanded) {
            revisionExpanded = expanded;
            if (revisionExpanded) {
                if (entry.isEventsInitialized()) {
                    if (!viewEventsInitialized) {
                        showLessFiles();
                        ((SummaryListModel) resultsList.getModel()).addEvents(getUserData());
                    }
                } else {
                    entry.expand();
                    if (!initializingStarted) {
                        initializingStarted = true;
                        ((SummaryListModel) resultsList.getModel()).addDummyEvents(getUserData());
                    }
                }
            }
        }

        private boolean canShowLessEvents() {
            return revisionExpanded && showingFiles > NEXT_FILES_INITIAL_PAGING || isAllEventsVisible()
                    && Math.min(NEXT_FILES_INITIAL_PAGING, getDefaultVisibleEventCount()) < entry.getEvents().size();
        }

        int getNextFilesToShowCount () {
            int cnt = entry.getEvents().size() - showingFiles;
            if (cnt <= nextFilesPaging) {
                cnt = -1;
            } else {
                cnt = nextFilesPaging;
            }
            return cnt;
        }

        private void showMoreFiles () {
            int nextFiles = getNextFilesToShowCount();
            if (nextFiles > 0) {
                showingFiles += nextFiles;
                nextFilesPaging = nextFilesPaging * 2;
            } else {
                showingFiles = -1;
            }
        }

        private void showLessFiles () {
            showingFiles = Math.min(NEXT_FILES_INITIAL_PAGING, getDefaultVisibleEventCount());
            nextFilesPaging = NEXT_FILES_INITIAL_PAGING;
        }

        boolean isEventVisible(EventItem event) {
            return isEventVisible(event.getUserData());
        }

        boolean isEventVisible(LogEntry.Event event) {
            if (!viewEventsInitialized || isAllEventsVisible()) {
                return true;
            } else if (showingFiles == 0) {
                // not yet 
                showLessFiles();
                if (entry.getEvents().size() == getDefaultVisibleEventCount()
                        && entry.getEvents().size() <= nextFilesPaging) {
                    showingFiles = -1;
                    return true;
                }
            }
            int visibleCount = 0;
            // at first display visible by default
            // only then the rest
            for (boolean defaultVisible : new boolean[] { true, false }) {
                for (LogEntry.Event e : entry.getEvents()) {
                    if (visibleCount >= showingFiles) {
                        // over the paging limit or ound as visible
                        break;
                    }
                    if (e.isVisibleByDefault() == defaultVisible) {
                        ++visibleCount;
                        if (e == event) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        private int getDefaultVisibleEventCount () {
            int i = 0;
            for (LogEntry.Event e : entry.getEvents()) {
                if (e.isVisibleByDefault()) {
                    ++i;
                }
            }
            return i;
        }
    }

    private abstract class ExpandCollapseAction extends AbstractAction {
        
        @Override
        public void actionPerformed (ActionEvent e) {
            List<Item> selection = resultsList.getSelectedValuesList();
            if (selection.size() == 1 && selection.get(0) instanceof RevisionItem) {
                perform((RevisionItem) selection.get(0));
            }
        }

        protected abstract void perform (RevisionItem revisionItem);
    }

    private class ExpandAction extends ExpandCollapseAction {

        @Override
        protected void perform (RevisionItem revisionItem) {
            if (!revisionItem.revisionExpanded) {
                revisionItem.setExpanded(true);
                ((SummaryListModel) resultsList.getModel()).refreshModel();
            }
        }
        
    }

    private class CollapseAction extends ExpandCollapseAction {

        @Override
        protected void perform (RevisionItem revisionItem) {
            if (revisionItem.revisionExpanded) {
                revisionItem.setExpanded(false);
                ((SummaryListModel) resultsList.getModel()).refreshModel();
            }
        }
    }

    private class ExpandCollapseGeneralAction extends ExpandCollapseAction {
        private final ExpandCollapseAction collapseAction;
        private final ExpandCollapseAction expandAction;

        public ExpandCollapseGeneralAction (ExpandCollapseAction expandAction, ExpandCollapseAction collapseAction) {
            this.expandAction = expandAction;
            this.collapseAction = collapseAction;
        }
        
        @Override
        protected void perform (RevisionItem revisionItem) {
            if (revisionItem.revisionExpanded) {
                collapseAction.perform(revisionItem);
            } else {
                expandAction.perform(revisionItem);
            }
        }
    }

    class LoadingEventsItem extends Item {
        private final RevisionItem parent;
        public LoadingEventsItem (RevisionItem parent) {
            super(null);
            this.parent = parent;
        }

        @Override
        String getItemId () {
            return parent.getItemId() + "#LOADING_EVENTS"; //NOI18N
        }

        @Override
        boolean isVisible () {
            return super.isVisible() && parent.isVisible() && parent.revisionExpanded;
        }
    }

    class EventItem extends Item<LogEntry.Event> {
        private final RevisionItem parent;
        private final LogEntry.Event event;
        public EventItem (LogEntry.Event event, RevisionItem parent) {
            super(event);
            this.event = event;
            this.parent = parent;
        }

        @Override
        String getItemId () {
            return parent.getItemId() + "#" + event.getPath() + "#" + event.getAction(); //NOI18N
        }

        @Override
        boolean isVisible () {
            boolean visible = false;
            if (super.isVisible() && parent.isVisible() && parent.revisionExpanded) {
                visible = parent.isEventVisible(this);
            }
            return visible;
        }

        void actionsToPopup (Point p) {
            Action[] actions = event.getUserActions();
            if (actions.length > 0) {
                JPopupMenu menu = new JPopupMenu();
                for (Action a : actions) {
                    menu.add(a);
                }
                int idx;
                for (idx = 0; idx < resultsList.getModel().getSize(); ++idx) {
                    if (resultsList.getModel().getElementAt(idx) == this) {
                        break;
                    }
                }
                if (idx == resultsList.getModel().getSize()) return;
                Rectangle rect = resultsList.getCellBounds(idx, idx);
                menu.show(resultsList, p.x + rect.x, p.y + rect.y);
            }
        }
        
        RevisionItem getParent () {
            return parent;
        }

        @Override
        public String toString () {
            return event.toString();
        }
    }

    class ShowAllEventsItem extends Item {
        private final RevisionItem parent;
        public ShowAllEventsItem (RevisionItem parent) {
            super(null);
            this.parent = parent;
        }

        @Override
        String getItemId () {
            return parent.getItemId() + "#SHOW_ALL_FILES"; //NOI18N
        }

        @Override
        boolean isVisible () {
            return parent.isVisible() && parent.revisionExpanded && !parent.isAllEventsVisible();
        }

        RevisionItem getParent () {
            return parent;
        }
    }

    class ShowLessEventsItem extends Item {
        private final RevisionItem parent;
        public ShowLessEventsItem (RevisionItem parent) {
            super(null);
            this.parent = parent;
        }

        @Override
        String getItemId () {
            return parent.getItemId() + "#SHOW_LESS_FILES"; //NOI18N
        }

        @Override
        boolean isVisible () {
            return parent.isVisible() && parent.revisionExpanded && parent.canShowLessEvents();
        }

        RevisionItem getParent () {
            return parent;
        }
    }

    class ActionsItem extends Item {
        private final RevisionItem parent;
        public ActionsItem (RevisionItem parent) {
            super(parent.getUserData());
            this.parent = parent;
        }

        @Override
        String getItemId () {
            return parent.getItemId() + "#ACTIONS"; //NOI18N
        }

        @Override
        boolean isVisible () {
            return parent.isVisible() && parent.revisionExpanded && parent.getUserData().getActions().length > 0;
        }

        RevisionItem getParent () {
            return parent;
        }
    }

    private class SummaryListModel extends AbstractListModel {

        private final Set<String> revisions;
        private final List<Item> allResults;
        private final List<Item> dispResults;

        public SummaryListModel (List<? extends LogEntry> entries, boolean hasMoreResults) {
            allResults = new ArrayList<>(initializeResults(entries, master.hasMoreResults()));
            dispResults = new ArrayList<>(allResults.size());
            revisions = new HashSet<>();
            for (LogEntry entry : entries) {
                revisions.add(entry.getRevision());
            }
            refreshModel();
        }
        
        @Override
        public int getSize() {
            return dispResults.size();
        }

        @Override
        public Item getElementAt(int index) {
            return dispResults.get(index);
        }
        
        void addEvents (LogEntry src) {
            assert EventQueue.isDispatchThread();
            RevisionItem rev = null;
            ListIterator<Item> it = allResults.listIterator();
            while (it.hasNext()) {
                Item revCandidate = it.next();
                if (revCandidate instanceof RevisionItem && revCandidate.getUserData() == src) {
                    rev = (RevisionItem) revCandidate;
                    if (it.hasNext()) {
                        if (it.next() instanceof ActionsItem) {
                            while (it.hasNext()) {
                                Item item = it.next();
                                if (item instanceof EventItem || item instanceof LoadingEventsItem) {
                                    item.setVisible(false);
                                    it.remove();
                                } else {
                                    it.previous();
                                    break;
                                }
                            }
                        } else {
                            it.previous();
                        }
                    }
                    break;
                }
            }
            if (rev != null) {
                rev.viewEventsInitialized = true;
                Collection<LogEntry.Event> events = src.getEvents();
                for (LogEntry.Event ev : events) {
                    it.add(new EventItem(ev, rev));
                }
                it.add(new ShowAllEventsItem(rev));
                it.add(new ShowLessEventsItem(rev));
            }
            refreshModel();
        }

        void addDummyEvents (LogEntry src) {
            assert EventQueue.isDispatchThread();
            RevisionItem rev = null;
            ListIterator<Item> it = allResults.listIterator();
            while (it.hasNext()) {
                Item revCandidate = it.next();
                if (revCandidate instanceof RevisionItem && revCandidate.getUserData() == src) {
                    rev = (RevisionItem) revCandidate;
                    if (it.hasNext()) {
                        if (!(it.next() instanceof ActionsItem)) {
                            it.previous();
                        }
                    }
                    break;
                }
            }
            if (rev != null) {
                Collection<LogEntry.Event> events = src.getDummyEvents();
                for (LogEntry.Event ev : events) {
                    it.add(new EventItem(ev, rev));
                }
            }
            refreshModel();
        }

        private void addEntries (List<? extends LogEntry> entries, boolean noMoreResults) {
            List<LogEntry> newEntries = new LinkedList<>(entries);
            newEntries.removeIf(entry -> !revisions.add(entry.getRevision()));
            List<Item> itemsToAdd = expandResults(newEntries);
            if (!itemsToAdd.isEmpty()) {
                int addedAtIndex;
                if (allResults.get(allResults.size() - 1) instanceof MoreRevisionsItem) {
                    addedAtIndex = allResults.size() - 1;
                } else {
                    addedAtIndex = allResults.size();
                }
                allResults.addAll(addedAtIndex, itemsToAdd);
            }
            if (noMoreResults && !allResults.isEmpty()) {
                if (allResults.get(allResults.size() - 1) instanceof MoreRevisionsItem) {
                    ((MoreRevisionsItem) allResults.remove(allResults.size() - 1)).setVisible(false);
                }
            }
            refreshModel();
        }

        private void refreshModel () {
            int index = 0;
            for (ListIterator<Item> it = dispResults.listIterator(); it.hasNext(); ) {
                if (it.next().isVisible()) {
                    ++index;
                } else {
                    it.remove();
                    fireIntervalRemoved(it, index, index);
                }
            }
            if (dispResults.isEmpty()) {
                for (Item item : allResults) {
                    if (item.isVisible()) {
                        dispResults.add(item);
                    }
                }
                fireIntervalAdded(this, 0, dispResults.size());
            } else {
                dispResults.add(new MoreRevisionsItem());
                ListIterator<Item> dispIterator = dispResults.listIterator();
                Item displayed = dispIterator.next();
                index = 0;
                int addedStart = -1, addedLast = 0;
                int removedStart = -1, removedLast = 0;
                for (Item item : allResults) {
                    if (item == displayed) {
                        fireAdds(addedStart, addedLast);
                        addedStart = -1; addedLast = 0;
                        if (!item.isVisible()) {
                            dispIterator.remove();
                            if (removedStart == -1) {
                                removedStart = removedLast = index;
                            } else {
                                if (removedLast < index -1) {
                                    fireRemovals(removedStart, removedLast);
                                    removedStart = index;
                                } else {
                                    removedLast = index;
                                }
                            }
                        }
                        if (dispIterator.hasNext()) {
                            displayed = dispIterator.next();
                            ++index;
                        }
                    } else {
                        fireRemovals(removedStart, removedLast);
                        removedStart = -1; removedLast = 0;
                        if (item.isVisible()) {
                            dispIterator.previous();
                            dispIterator.add(item);
                            if (addedStart == -1) {
                                addedStart = addedLast = index;
                            } else {
                                if (addedLast < index -1) {
                                    fireAdds(addedStart, addedLast);
                                    addedStart = index;
                                } else {
                                    addedLast = index;
                                }
                            }
                            dispIterator.next();
                            ++index;
                        }
                    }
                }
                fireAdds(addedStart, addedLast);
                dispResults.remove(dispResults.size() - 1);
            }
        }

        private void fireChange (int index) {
            fireContentsChanged(this, index, index);
        }

        private void fireAdds (int start, int end) {
            if (start != -1 && end >= start) {
                fireIntervalAdded(this, start, end);
                for (int i = start; i <= end; i++) {
                    if (resultsList.getSelectionModel().isSelectedIndex(i)) {
                        resultsList.getSelectionModel().removeSelectionInterval(i, i);
                    }
                }
            }
        }

        private void fireRemovals (int start, int end) {
            if (start != -1 && end >= start) {
                fireIntervalRemoved(this, start, end);
            }
        }
    }
}
