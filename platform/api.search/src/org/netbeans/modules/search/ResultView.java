/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.search;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.FocusManager;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.provider.SearchComposition;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Panel which displays search results in explorer like manner.
 * This panel is a singleton.
 *
 * @see  <a href="doc-files/results-class-diagram.png">Class diagram</a>
 * @author Petr Kuzel, Jiri Mzourek, Peter Zavadsky
 * @author Marian Petras
 * @author kaktus
 */

@TopComponent.Description(preferredID=ResultView.ID, persistenceType=TopComponent.PERSISTENCE_ALWAYS, iconBase="org/netbeans/modules/search/res/find.gif")
@TopComponent.Registration(mode="output", position=1900, openAtStartup=false)
@ActionID(id = "org.netbeans.modules.search.ResultViewOpenAction", category = "Window")
@TopComponent.OpenActionRegistration(displayName="#TEXT_ACTION_SEARCH_RESULTS", preferredID=ResultView.ID)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-0")
})
public final class ResultView extends TopComponent {

    private static final boolean isMacLaf = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N
    private static final Color macBackground = UIManager.getColor("NbExplorerView.background"); //NOI18N
    private static final String CARD_NAME_EMPTY = "empty";              //NOI18N
    private static final String CARD_NAME_TABS = "tabs";                //NOI18N
    private static final String CARD_NAME_SINGLE = "single";            //NOI18N
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    static final String ID = "search-results";                  //NOI18N
    
    private JPopupMenu pop;
    private PopupListener popL;
    private CloseListener closeL;

    private JPanel emptyPanel;
    private JPanel singlePanel;
    private JTabbedPane tabs;
    private WeakReference<ResultViewPanel> tabToReuse;
    private CurrentLookupProvider lookupProvider = new CurrentLookupProvider();

    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this <code>TopComponent</code>
     */
    public static synchronized ResultView getInstance() {
        ResultView view;
        view = (ResultView) WindowManager.getDefault().findTopComponent(ID);
        if (view == null) {
            view = new ResultView(); // should not happen
        }
        return view;
    }

    private final CardLayout contentCards;
    
    public ResultView() {
        setLayout(contentCards = new CardLayout());

        setName("Search Results");                                      //NOI18N
        setDisplayName(NbBundle.getMessage(ResultView.class, "TITLE_SEARCH_RESULTS"));    //NOI18N
        
        initAccessibility();

        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        popL = new PopupListener();
        closeL = new CloseListener();
        
        emptyPanel = new JPanel();
        singlePanel = new JPanel();
        singlePanel.setLayout(new BoxLayout(singlePanel, BoxLayout.PAGE_AXIS));
        emptyPanel.setOpaque(true);
        tabs = TabbedPaneFactory.createCloseButtonTabbedPane();
        tabs.addChangeListener((ChangeEvent e) -> updateLookup());
        tabs.setMinimumSize(new Dimension(0, 0));
        tabs.addMouseListener(popL);
        tabs.addPropertyChangeListener(closeL);
        add(emptyPanel, CARD_NAME_EMPTY);
        add(tabs, CARD_NAME_TABS);
        add(singlePanel, CARD_NAME_SINGLE);
        if (isMacLaf) {
            emptyPanel.setBackground(macBackground);
            tabs.setBackground(macBackground);
            tabs.setOpaque(true);
            setBackground(macBackground);
            setOpaque(true);
        } else {
            emptyPanel.setBackground(
                    UIManager.getColor("Tree.background"));         //NOI18N
        }
        contentCards.show(this, CARD_NAME_EMPTY);
        associateLookup(Lookups.proxy(lookupProvider));
    }

    @Deprecated
    public static final class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7398708142639457544L;
        public Object readResolve() {
            return null;
        }
    }

    private void initAccessibility() {
        ResourceBundle bundle = NbBundle.getBundle(ResultView.class);
        getAccessibleContext().setAccessibleName (bundle.getString ("ACSN_ResultViewTopComponent"));                   //NOI18N
        getAccessibleContext().setAccessibleDescription (bundle.getString ("ACSD_ResultViewTopComponent"));            //NOI18N
    }

    /**
     * This method exists just to make the <code>close()</code> method
     * accessible via <code>Class.getDeclaredMethod(String, Class[])</code>.
     * It is used in <code>Manager</code>.
     */
    void closeResults() {
        close();
    }

    @Override
    protected void componentOpened() {
        assert EventQueue.isDispatchThread();
        Manager.getInstance().searchWindowOpened();

        ResultViewPanel panel = getCurrentResultViewPanel();
        if (panel != null) {
            panel.componentOpened();
        }
        setToolTipText(NbBundle.getMessage(ResultView.class,
                "TOOLTIP_SEARCH_RESULTS"));                             //NOI18N
    }

    @Override
    public void requestFocus() {
        ResultViewPanel panel = getCurrentResultViewPanel();
        if (panel != null) {
            panel.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        ResultViewPanel panel = getCurrentResultViewPanel();
        if (panel != null) {
            return panel.requestFocusInWindow();
        } else {
            return false;
        }
    }

    private ResultViewPanel getCurrentResultViewPanel(){
        if (singlePanel.getComponents().length == 1) {
            Component comp = singlePanel.getComponents()[0];
            if (comp instanceof ResultViewPanel) {
                return (ResultViewPanel) comp;
            } else {
                return null;
            }
        } else if (tabs.getTabCount() > 0) {
            Component comp = tabs.getSelectedComponent();
            if (comp instanceof ResultViewPanel) {
                return (ResultViewPanel) comp;
            } else {
                return null;
            }
        }
        return null;
    }

    private String getTabTitle(Component panel){
        return NbBundle.getMessage(ResultView.class,
                                   "TEXT_MSG_RESULTS_FOR_X",   //NOI18N
                                   String.valueOf(panel.getName()));
    }

    private void updateTabTitle(JPanel panel) {
        if (getComponentCount() != 0) {
            if (tabs.getTabCount() > 0) {
                int index = tabs.indexOfComponent(panel);
                tabs.setTitleAt(index, getTabTitle(panel));
                tabs.setToolTipTextAt(index, panel.getToolTipText());
            }
        }
    }
    private void removePanel(ResultViewPanel panel) {
        if (tabs.getTabCount() > 0) {
            if (panel == null) {
                panel = (ResultViewPanel) tabs.getSelectedComponent();
            }
            if (panel.isSearchInProgress()){
                panel.getSearchComposition().terminate();
            }
            tabs.remove(panel);
            panel.getSearchComposition().getSearchResultsDisplayer().closed();
            if (tabs.getTabCount() == 0) {
                contentCards.show(this, CARD_NAME_EMPTY);
                updateLookup();
            } else if (tabs.getTabCount() == 1) {
                Component c = tabs.getComponentAt(0);
                singlePanel.add(c);
                contentCards.show(this, CARD_NAME_SINGLE);
                updateLookup();
            }
            this.repaint();
        } else if (singlePanel.getComponents().length == 1)  {
            Component comp = singlePanel.getComponents()[0];
            ResultViewPanel rvp = (ResultViewPanel) comp;
            if (rvp.isSearchInProgress()) {
                Manager.getInstance().stopSearching(viewToSearchMap.get(rvp));
            }
            singlePanel.remove(comp);
            contentCards.show(this, CARD_NAME_EMPTY);
            rvp.getSearchComposition().getSearchResultsDisplayer().closed();
            this.repaint();
        } else {
            close();
        }
        // Manager.getInstance().scheduleCleanTask(new CleanTask(viewToSearchMap.get(panel).getResultModel())); TODO
        
        SearchTask sTask = viewToSearchMap.remove(panel);
        searchToViewMap.remove(sTask);
        ReplaceTask rTask = searchToReplaceMap.remove(sTask);
        replaceToSearchMap.remove(rTask);

        validate();
        updateTooltip();
    }

    @Override
    protected void componentClosed() {
        assert EventQueue.isDispatchThread();

        Manager.getInstance().searchWindowClosed();
        closeAll(false); // #170545
    }
    
    /**
     * Displays a message informing about the task which blocks the search
     * from being started. The search may also be blocked by a not yet finished
     * previous search task.
     *
     * @param  blockingTask  constant identifying the blocking task
     * @see  Manager#SEARCHING
     * @see  Manager#CLEANING_RESULT
     * @see  Manager#PRINTING_DETAILS
     */
    void notifySearchPending(final SearchTask task,final int blockingTask) {
        assert EventQueue.isDispatchThread();

        ResultViewPanel panel = searchToViewMap.get(task);
        if (panel != null) {
            String msgKey = null;
            switch (blockingTask) {
                case Manager.REPLACING:
                    msgKey = "TEXT_FINISHING_REPLACE";                  //NOI18N
                    break;
                case Manager.SEARCHING:
                    msgKey = "TEXT_FINISHING_PREV_SEARCH";                  //NOI18N
                    break;
                /*
                 * case Manager.CLEANING_RESULT: msgKey =
                 * "TEXT_CLEANING_RESULT"; //NOI18N break; case
                 * Manager.PRINTING_DETAILS: msgKey = "TEXT_PRINTING_DETAILS";
                 * //NOI18N break;
                 */
                default:
                    assert false;
            }
            panel.showInfo(NbBundle.getMessage(ResultView.class, msgKey));
            panel.setBtnStopEnabled(true);
        }
    }
    
    /**
     */
    void searchTaskStateChanged(final SearchTask task, final int changeType) {
        assert EventQueue.isDispatchThread();

        ResultViewPanel panel = searchToViewMap.get(task);
        if (panel == null) {
            return;
        }
        switch (changeType) {
            case Manager.EVENT_SEARCH_STARTED:
                updateTabTitle(panel);
                panel.searchStarted();
                break;
            case Manager.EVENT_SEARCH_FINISHED:
                panel.searchFinished();
                break;
            case Manager.EVENT_SEARCH_INTERRUPTED:
                panel.searchInterrupted();
                break;
            case Manager.EVENT_SEARCH_CANCELLED:
                panel.searchCancelled();
                break;
            default:
                assert false;
        }
    }
    
    /**
     */
    void showAllDetailsFinished() {
        assert EventQueue.isDispatchThread();
        
//        mainPanel.updateShowAllDetailsBtn();
    }

    private Map<SearchTask, ResultViewPanel> searchToViewMap =
            new HashMap<>();
    private Map<ResultViewPanel, SearchTask> viewToSearchMap =
            new HashMap<>();

    void addSearchPair(ResultViewPanel panel, SearchTask task){
        if ((task != null) && (panel != null)){
            SearchTask oldTask = viewToSearchMap.get(panel);
            if (oldTask != null){
                searchToViewMap.remove(oldTask);
            }
            searchToViewMap.put(task, panel);
            viewToSearchMap.put(panel, task);
        }
    }

    private Map<ReplaceTask, SearchTask> replaceToSearchMap =
            new HashMap<>();
    private Map<SearchTask, ReplaceTask> searchToReplaceMap =
            new HashMap<>();

    private void closeAll(boolean butCurrent) {
        if (tabs.getTabCount() > 0) {
            Component current = tabs.getSelectedComponent();
            Component[] c =  tabs.getComponents();
            for (Component c1 : c) {
                if (butCurrent && c1 == current) {
                    continue;
                }
                if (c1 instanceof ResultViewPanel) {
                    // #172546
                    removePanel((ResultViewPanel) c1);
                }
            }
        } else if (singlePanel.getComponents().length > 0) {
            Component comp = singlePanel.getComponents()[0];
            if (comp instanceof ResultViewPanel) { // #172546
                removePanel((ResultViewPanel) comp);
            }
        }
    }

    private class CloseListener implements PropertyChangeListener {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((ResultViewPanel) evt.getNewValue());
            }
        }
    }

    private class PopupListener extends MouseUtils.PopupMouseAdapter {
        @Override
        protected void showPopup (MouseEvent e) {
            pop.show(ResultView.this, e.getX(), e.getY());
        }
    }

    private class Close extends AbstractAction {
        public Close() {
            super(NbBundle.getMessage(ResultView.class, "LBL_CloseWindow"));  //NOI18N
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }

    private final class CloseAll extends AbstractAction {
        public CloseAll() {
            super(NbBundle.getMessage(ResultView.class, "LBL_CloseAll"));  //NOI18N
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAll(false);
        }
    }

    private class CloseAllButCurrent extends AbstractAction {
        public CloseAllButCurrent() {
            super(NbBundle.getMessage(ResultView.class, "LBL_CloseAllButCurrent"));  //NOI18N
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAll(true);
        }
    }

    /**
     * Add a tab for a new displayer.
     */
    ResultViewPanel addTab(SearchTask searchTask) {

        int tabIndex = tryReuse();
        ResultViewPanel panel = new ResultViewPanel(searchTask);
        SearchComposition<?> composition = searchTask.getComposition();
        String title = composition.getSearchResultsDisplayer().getTitle();

        if (singlePanel.getComponents().length == 0
                && tabs.getTabCount() == 0) {
            singlePanel.add(panel);
            contentCards.show(this, CARD_NAME_SINGLE);
            updateLookup();
        } else if (singlePanel.getComponents().length == 1) {
            ResultViewPanel comp =
                    (ResultViewPanel) singlePanel.getComponents()[0];
            tabs.insertTab(comp.getName(), null, comp,
                    comp.getToolTipText(), 0);
            tabs.setToolTipTextAt(0, comp.getToolTipText());
            int tabToInsert = tabIndex > -1 ? tabIndex : 1;
            tabs.insertTab(title, null, panel, panel.getToolTipText(),
                    tabToInsert);
            tabs.setToolTipTextAt(tabToInsert, panel.getToolTipText());
            tabs.setSelectedIndex(tabIndex > -1 ? tabIndex : 1);
            contentCards.show(this, CARD_NAME_TABS);
        } else {
            tabs.insertTab(title, null, panel,
                    panel.getToolTipText(),
                    tabIndex > -1 ? tabIndex : tabs.getTabCount());
            tabs.setToolTipTextAt(
                    tabIndex > -1 ? tabIndex : tabs.getTabCount() - 1,
                    panel.getToolTipText());
            tabs.setSelectedComponent(panel);
            tabs.validate();
        }
        validate();
        requestActive();
        updateTooltip();
        return panel;
    }

    /**
     * Return tab index to reuse, or -1 to disable reusing.
     */
    private int tryReuse() {
        ResultViewPanel toReuse = getTabToReuse();
        if (toReuse == null) {
            return -1;
        } else if (singlePanel.getComponents().length == 1
                && singlePanel.getComponent(0) == toReuse) {
            removePanel(toReuse);
            clearReusableTab();
            return 0;
        } else if (tabs.getTabCount() > 0) {
            int index = tabs.indexOfComponent(toReuse);
            if (index >= 0) {
                removePanel(toReuse);
                clearReusableTab();
                return index;
            }
        }
        return tabs.getTabCount();
    }

    public boolean isFocused() {
        ResultViewPanel rvp = getCurrentResultViewPanel();
        if (rvp != null) {
            Component owner = FocusManager.getCurrentManager().getFocusOwner();
            return owner != null && SwingUtilities.isDescendingFrom(owner, rvp);
        } else {
            return false;
        }
    }

    private synchronized void setTabToReuse(
            ResultViewPanel resultViewPanel) {
        tabToReuse = resultViewPanel == null
                ? null
                : new WeakReference<>(resultViewPanel);
    }

    private synchronized ResultViewPanel getTabToReuse() {
        return tabToReuse == null || tabToReuse.get() == null
                ? null
                : tabToReuse.get();
    }

    /**
     * Mark the currenly selected tab as reusable.
     */
    public synchronized void markCurrentTabAsReusable() {
        setTabToReuse(getCurrentResultViewPanel());
    }

    /**
     * Set that no tab should be reused. Clears effect of the last invocation of
     * method {@link #markCurrentTabAsReusable() }
     */
    public synchronized void clearReusableTab() {
        setTabToReuse(null);
    }

    private void updateLookup() {
        ResultViewPanel rvp = getCurrentResultViewPanel();
        lookupProvider.setLookup(rvp == null ? Lookup.EMPTY : rvp.getLookup());
        getLookup().lookup(Object.class); //refresh lookup
    }

    private void updateTooltip() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>");                                         //NOI18N
        sb.append(NbBundle.getMessage(ResultView.class,
                "TOOLTIP_SEARCH_RESULTS"));                             //NOI18N
        sb.append("</b>");                                              //NOI18N
        if (singlePanel.getComponents().length == 1) {
            appendTabToToolTip(singlePanel.getComponent(0), sb);
        } else if (tabs.getComponents().length > 0) {
            Component[] comps = tabs.getComponents();
            for (Component comp : comps) {
                appendTabToToolTip(comp, sb);
            }
        }
        sb.append("</html>");                                           //NOI18N
        setToolTipText(sb.toString());
    }

    private void appendTabToToolTip(Component c, StringBuilder sb) {
        if (c instanceof ResultViewPanel) {
            ResultViewPanel rvp = (ResultViewPanel) c;
            if (rvp.getToolTipText() != null) {
                sb.append("<br>&nbsp;&nbsp;");                          //NOI18N
                sb.append(UiUtils.escapeHtml(rvp.getToolTipText()));
                sb.append("&nbsp;");                                    //NOI18N
            }
        }
    }

    private static class CurrentLookupProvider implements Lookup.Provider {
        private Lookup currentLookup = Lookup.EMPTY;

        public void setLookup(Lookup lookup) {
            this.currentLookup = lookup;
        }

        @Override
        public Lookup getLookup() {
            return currentLookup;
        }
    }
}
