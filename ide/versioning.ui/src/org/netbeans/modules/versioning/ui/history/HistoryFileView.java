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
package org.netbeans.modules.versioning.ui.history;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEvent;
import org.netbeans.modules.versioning.ui.history.RevisionNode.MessageProperty;
import org.netbeans.modules.versioning.ui.history.HistoryComponent.Filter;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.TreePathSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.NodePopupFactory;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Stupka
 */
class HistoryFileView implements PreferenceChangeListener, VCSHistoryProvider.HistoryChangeListener {
           
    private FileTablePanel tablePanel;             

    private RequestProcessor rp = new RequestProcessor("HistoryView", 1, true); // NOI18N
    private final HistoryComponent tc; 
    private Filter filter;
    private Task refreshTask;
    private Task vcsTask;
    private final VersioningSystem versioningSystem;
    private final VersioningSystem lh;
    
    private Date currentDateFrom; 
    private LoadNextAction loadNextAction;
    private boolean visible;
    private VCSHistoryProvider pendingProviderToRefresh;
    
    public HistoryFileView(VersioningSystem versioningSystem, VersioningSystem lh, HistoryComponent tc) {                       
        this.tc = tc;
        this.versioningSystem = versioningSystem;
        this.lh = lh;
        this.visible = tc.isShowing();
        
        tablePanel = new FileTablePanel();
        loadNextAction = new LoadNextAction();
        
        registerHistoryListener(versioningSystem, this);
        HistorySettings.getInstance().addPreferenceListener(this);
        registerHistoryListener(lh, this);
    }
    
    public void refresh() {
        refreshTablePanel(null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if(HistorySettings.PROP_INCREMENTS.equals(evt.getKey()) ||
           HistorySettings.PROP_LOAD_ALL.equals(evt.getKey())) 
        {
            HistoryRootNode rootNode = getRootNode();
            if(rootNode != null) {
                loadNextAction.refreshName();
            }    
            // XXX invoke either for all or increment value
        }
    }
    
    private HistoryRootNode getRootNode() {
        Node rootContext = tablePanel.getExplorerManager().getRootContext();
        if(!(rootContext instanceof HistoryRootNode)) {
            return null;
        }  
        return (HistoryRootNode) rootContext;
    }
    
    public ExplorerManager getExplorerManager() {
        return tablePanel.getExplorerManager();
    }
    
    public JPanel getPanel() {
        return tablePanel;
    }
    
    public void close() {
        unregisterHistoryListener(versioningSystem, this);
        unregisterHistoryListener(lh, this);
    }    
    
    private synchronized void refreshTablePanel(final VCSHistoryProvider providerToRefresh) {    
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                Task t = refreshTask;
                if(t != null) {
                    t.cancel();
                    if(vcsTask != null) {
                        vcsTask.cancel();
                        vcsTask = null;
                    }
                }
                refreshTask = t = rp.create(new RefreshTable(mergeProvidersToRefresh(providerToRefresh, t)));
                if (visible) {
                    t.schedule(100);
                }
            }

            private VCSHistoryProvider mergeProvidersToRefresh (VCSHistoryProvider providerToRefresh, Task t) {
                if (t != null) {
                    // there was an unfinished refresh task
                    // we should definitely refresh also its entries
                    if (providerToRefresh != pendingProviderToRefresh) {
                        // need to refresh all
                        providerToRefresh = null;
                    }
                }
                return providerToRefresh;
            }
        });
    }

    void requestActive() {
        visible = true;
        if(getRootNode() == null) {
            // invoked for the first time -> refresh
            History.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    refresh();
                    tablePanel.requestActivate();
                }
            });
        } else {
            tablePanel.requestActivate();
            Task t = refreshTask;
            if (t != null) {
                t.schedule(100);
            }
        }
    }
    
    void hidden () {
        visible = false;
    }

    FileObject[] getFiles() {
        return tc.getFiles();
    }
                     
    void setFilter(Filter filter) {
        this.filter = filter;
        tablePanel.treeView.getOutline().setQuickFilter(0, filter);
    }
    
    void fireFilterChanged() {
        tablePanel.treeView.getOutline().setQuickFilter(0, filter);
    }
    
    // XXX serialize vcs calls
    // XXX on deserialization do not invoke for every opened TC, but only the activated one
    private void loadVCSEntries(final VCSFileProxy[] files, final boolean forceLoadAll) {
        if(versioningSystem == null) {
            return;
        }
        vcsTask = rp.post(new Runnable() {
            @Override
            public void run() {                        
                VCSHistoryProvider hp = versioningSystem.getVCSHistoryProvider();
                if(hp == null) {
                    return;
                }
                HistoryRootNode rootNode = getRootNode();
                if(rootNode == null) {
                    return;
                }    
                try {
                    rootNode.loadingVCSStarted();

                    VCSHistoryProvider.HistoryEntry[] vcsHistory;
                    if(forceLoadAll || HistorySettings.getInstance().getLoadAll()) {
                        vcsHistory = hp.getHistory(files, (Date) null); // get all
                        // XXX need different text for "Showing Subversion revisions..."
                    } else {
                        int increment = HistorySettings.getInstance().getIncrements();
                        if(currentDateFrom == null) {
                            currentDateFrom = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * (long) increment); // last X days
                        } else {
                            currentDateFrom = new Date(currentDateFrom.getTime() - 1000 * 60 * 60 * 24 * (long) increment); // last X days
                        }                
                        vcsHistory = hp.getHistory(files, currentDateFrom); // get all
                    }

                    if(vcsHistory == null || vcsHistory.length == 0) {
                        return;
                    }                
                    List<HistoryEntry> entries = new ArrayList<HistoryEntry>(vcsHistory.length);
                    for (VCSHistoryProvider.HistoryEntry he : vcsHistory) {
                        entries.add(new HistoryEntry(he, false));
                    }
                    rootNode.addVCSEntries(entries.toArray(new HistoryEntry[0]), 0);
                } finally {
                    rootNode.loadingVCSFinished(currentDateFrom);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run () {
                            tablePanel.repaint();
                        }
                    });
                    // XXX yet select the first node on
                }
            }
        });
    }

    private void restoreSelection(final HistoryRootNode root, Node[] oldSelection) {
        tablePanel.getExplorerManager().setRootContext(root);
        if(root.getChildren().getNodesCount() > 0) {                
                if (oldSelection != null && oldSelection.length > 0) {                        
                    Node[] newSelection = getEqualNodes(root, oldSelection);                        
                    if(newSelection.length > 0) {
                    setSelection(newSelection);
                    } else {
                        Node[] newExploredContext = getEqualNodes(root, new Node[] { oldSelection[0].getParentNode() });
                        if(newExploredContext.length > 0) {
                            selectFirstNeighborNode(newExploredContext[0], oldSelection[0]);
                        }
                    }
                } else {
                    selectFirstNode(root);
                }
            } else {
            setSelection(new Node[]{});
            }   
        }
        
        private Node[] getEqualNodes(Node root, Node[] oldNodes) {    
            List<Node> ret = new ArrayList<Node>();
            for(Node on : oldNodes) {
                Node node = findEqualInChildren(root, on);
                if(node != null) {
                    ret.add(node);                                
                }
                if(root.getName().equals(on.getName())) {
                    ret.add(root);
                }
            }            
            return ret.toArray(new Node[0]);                            
        }                   
        
        private Node findEqualInChildren(Node node, Node toFind) {
            Node[] children = node.getChildren().getNodes();
            for(Node child : children) {
                if(toFind.getName().equals(child.getName())) {
                    return child;                
                }
                Node n = findEqualInChildren(child, toFind);
                if(n != null) {
                    return n;
                }                 
            }
            return null;
        } 

        private void selectFirstNode(final Node root) {        
            Node[] dateFolders = root.getChildren().getNodes();
            if (dateFolders != null && dateFolders.length > 0) {
                final Node[] nodes = dateFolders[0].getChildren().getNodes();
                if (nodes != null && nodes.length > 0) {                
                setSelection(new Node[]{ nodes[0] });
                }
            }        
        }

    private void selectFirstNeighborNode(Node context, Node oldSelection) {            
            Node[] children = context.getChildren().getNodes();
            if(children.length > 0 && children[0] instanceof Comparable) {
                Node[] newSelection = new Node[] { children[0] } ;
                for(int i = 1; i < children.length; i++) {
                    Comparable c = (Comparable) children[i];
                    if( c.compareTo(oldSelection) < 0 ) {
                       newSelection[0] = children[i]; 
                    }                                            
                }
            setSelection(newSelection);
                tablePanel.getExplorerManager().setExploredContext(context);
            }        
        }   
        
    private void setSelection(final Node[] nodes) {
        SwingUtilities.invokeLater(new Runnable() {
        @Override
            public void run() {
                try {
                    tablePanel.getExplorerManager().setSelectedNodes(nodes);
                } catch (PropertyVetoException ex) {
                    // ignore
                }
            }
        });                                             
        }

    @Override
    public void fireHistoryChanged(HistoryEvent evt) {
        FileObject[] files = tc.getFiles();
        if(files == null) {
            return;
        }
        Set<FileObject> fileSet = new HashSet<FileObject>();
        for (VCSFileProxy f : evt.getFiles()) {
            FileObject fo = f != null ? f.toFileObject() : null;
            if(fo != null) {
                fileSet.add(fo);
            }
        }
        if(fileSet.isEmpty()) {
            return;
        }
        for (FileObject file : files) {
            if(fileSet.contains(file)) {
                refreshTablePanel(evt.getSource());
            }
        }
    }

    private static void registerHistoryListener(VersioningSystem versioningSystem, VCSHistoryProvider.HistoryChangeListener l) {
        VCSHistoryProvider hp = History.getHistoryProvider(versioningSystem);
        if(hp != null) {
            hp.addHistoryChangeListener(l);
        }
    }

    private static void unregisterHistoryListener(VersioningSystem versioningSystem, VCSHistoryProvider.HistoryChangeListener l) {
        VCSHistoryProvider hp = History.getHistoryProvider(versioningSystem);
        if(hp != null) {
            hp.removeHistoryChangeListener(l);
        }
    }
    
    HistoryEntry getParentEntry(HistoryEntry entry) {
        return getRootNode().getPreviousEntry(entry);
    }

    void selectPrevEntry() {
        Outline outline = tablePanel.treeView.getOutline();
        if(outline.getSelectedRowCount() != 1) {
            return;
        }
        int row = outline.getSelectedRow();
        if(row - 1 < 0) {
            return;
        }
        row = getPrevRow(row);
        if(row > -1) {
            outline.getSelectionModel().setSelectionInterval(row, row);
            scrollToVisible(row, -1);
        } 
    }
    
    private int getPrevRow(int row) {
        row = row - 1;
        Outline outline = tablePanel.treeView.getOutline();
        if(row < 0 || row >= outline.getRowCount()) {
            return -1;
        }
        TreePath path = outline.getOutlineModel().getLayout().getPathForRow(row);
        Node node = Visualizer.findNode(path.getLastPathComponent());
        if(node.isLeaf()) {
            if(node instanceof RevisionNode || node instanceof RevisionNode.FileNode) {
                return row;
            } else {
                return -1;
            }
        } else {
            TreePathSupport support = outline.getOutlineModel().getTreePathSupport();
            if(support.isExpanded(path)) {
                return getPrevRow(row);
            } else {
                support.expandPath(path);
                return row + node.getChildren().getNodesCount();
            }
        }
    }
    
    void selectNextEntry() {
        Outline outline = tablePanel.treeView.getOutline();
        if(outline.getSelectedRowCount() != 1) {
            return;
        }
        int row = outline.getSelectedRow();
        if(row == outline.getRowCount() - 1) {
            return;
        }
        row = getNextRow(row);
        if(row > -1) {
            outline.getSelectionModel().setSelectionInterval(row, row);
            scrollToVisible(row, 1);
        }
    }

    private int getNextRow(int row) {
        row = row + 1;
        Outline outline = tablePanel.treeView.getOutline();
        if(row < 0 || row >= outline.getRowCount()) {
            return -1;
        }
        TreePath path = outline.getOutlineModel().getLayout().getPathForRow(row);
        Node node = Visualizer.findNode(path.getLastPathComponent());
        if(node.isLeaf()) {
            if(node instanceof RevisionNode || node instanceof RevisionNode.FileNode) {
                return row;
            } else {
                return -1;
            }
        } else {
            TreePathSupport support = outline.getOutlineModel().getTreePathSupport();
            if(support.isExpanded(path)) {
                return getPrevRow(row);
            } else {
                support.expandPath(path);
                return row + 1;
            }
        }
    }
    
    boolean isFirstRow() {
        return tablePanel.treeView.getOutline().getSelectedRow() == 0;
    }
    
    boolean isLastRow() {
        Outline outline = tablePanel.treeView.getOutline();
        return outline.getSelectedRow() == outline.getRowCount() - 1;
    }

    boolean isSingleSelection() {
        int[] rows = tablePanel.treeView.getOutline().getSelectedRows();
        return rows != null && rows.length == 1;
    }

    private void scrollToVisible(int row, int direction) {
        Outline outline = tablePanel.treeView.getOutline();
        Rectangle rect = outline.getCellRect(row, 0, true);
        outline.scrollRectToVisible(new Rectangle(new Point(0, rect.y + direction * rect.height)));
    }

    private void logFiles(String prefix, FileObject[] fos) {
        if(fos != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            for (int i = 0; i < fos.length; i++) {
                FileObject fo = fos[i];
                sb.append(fo.getPath());
                if(i < fos.length - 1) {
                    sb.append(","); // NOI18N
                }
            }
            History.LOG.fine(sb.toString());
        }
    }

    /**
     * Selects a node with the timestamp = toSelect, otherwise the selection stays.
     * If there wasn't a selection set yet then the first node will be selected.
     */ 
    private class RefreshTable implements Runnable {
        private final VCSHistoryProvider providerToRefresh;

        RefreshTable(VCSHistoryProvider providerToRefresh) {
            this.providerToRefresh = providerToRefresh;
            // mark unfinished providers
            pendingProviderToRefresh = providerToRefresh;
        }
        
        @Override
        public void run() {  
            HistoryRootNode root = getRootNode();
            if(root == null) {
                final String vcsName = (String) (History.getHistoryProvider(versioningSystem) != null ? 
                                                    versioningSystem.getDisplayName() :
                                                    null);
                root = new HistoryRootNode(vcsName, loadNextAction, createActions()); 
                tablePanel.getExplorerManager().setRootContext(root);
            }
            
            FileObject[] fos = tc.getFiles();
            VCSFileProxy[] proxies = History.toProxies(fos);
                    
            // refresh local history
            try {
                root.addWaitNode();
                VCSHistoryProvider lhProvider = History.getHistoryProvider(lh);
                if(lhProvider != null && (providerToRefresh == null || lhProvider == providerToRefresh)) {
                    logFiles("Refreshing LH entries for: ", fos); // NOI18N
                    root.addLHEntries(loadLHEntries(proxies), 0);
                }
            } finally {
                root.removeWaitNode();
            }
            // refresh vcs
            VCSHistoryProvider vcsProvider = History.getHistoryProvider(versioningSystem);
            if(tc != null && vcsProvider != null && (providerToRefresh == null || providerToRefresh == vcsProvider)) {
                logFiles("Refreshing VCS entries for: ", fos); // NOI18N
                loadVCSEntries(proxies, false);
            }
            refreshTask = null;
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run () {
                    tablePanel.revalidate();
                    tablePanel.repaint();

                    int row = tablePanel.treeView.getOutline().getSelectedRow();
                    if(row > -1) {
                        scrollToVisible(row, 2);
                    }
                }
            });
        }

    } 
    
    private HistoryEntry[] loadLHEntries(VCSFileProxy[] files) {
        if(lh == null) {
            return new HistoryEntry[0];
        }
        VCSHistoryProvider hp = lh.getVCSHistoryProvider();
        if(hp == null) {
            return new HistoryEntry[0];
        }
        VCSHistoryProvider.HistoryEntry[] vcsHistory = hp.getHistory(files, null);
        HistoryEntry[] history = new HistoryEntry[vcsHistory.length];
        for (int i = 0; i < vcsHistory.length; i++) {
            history[i] = new HistoryEntry(vcsHistory[i], true);
        }
        return history;
    }

    private Action[] createActions() {
        List<Action> actions = new LinkedList<Action>();
        actions.add(loadNextAction); 
        actions.add(new AbstractAction(NbBundle.getMessage(HistoryFileView.class, "LBL_LoadAll")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                loadVCSEntries(History.toProxies(tc.getFiles()), true);
            }
        });
        actions.add(null); 
        actions.add(new AbstractAction(NbBundle.getMessage(HistoryFileView.class, "LBL_AlwaysLoadAll")) { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                HistorySettings.getInstance().setLoadAll(true);
                loadVCSEntries(History.toProxies(tc.getFiles()), true);
            }
        });
        return actions.toArray(new Action[0]);
    }
    private class FileTablePanel extends JPanel implements ExplorerManager.Provider, TreeExpansionListener {
        private final BrowserTreeTableView treeView;    
        private final ExplorerManager manager;

        public FileTablePanel() { 
            manager = new ExplorerManager();

            setLayout(new GridBagLayout());

            treeView = new BrowserTreeTableView();             
            treeView.addTreeExpansionListener(this);
            setLayout(new BorderLayout());
            add(treeView, BorderLayout.CENTER);
        }   

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        void requestActivate() {
            treeView.requestFocusInWindow();
        }

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            Object obj = event.getPath().getLastPathComponent();
            if(obj == null) return;
            Node n = Visualizer.findNode(obj);
            if(HistoryRootNode.isLoadNext(n)) { // XXX move to lhrootnode
                loadNextAction.actionPerformed(null);
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            // do nothing
        }
        
        private class BrowserTreeTableView extends OutlineView {    
            BrowserTreeTableView() {
                super( NbBundle.getMessage(HistoryFileView.class, "LBL_LocalHistory_Column_Date")); //NOI18N
                setupColumns();

                getOutline().setShowHorizontalLines(true);
                getOutline().setShowVerticalLines(false);
                getOutline().setRootVisible(false);                    
//                getOutline().setGridColor(Color.white);
                
                setBorder(BorderFactory.createEtchedBorder());
                
        //        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        //        treeView.getAccessibleContext().setAccessibleName(browserAcsn);           
                getOutline().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);            
                setPopupAllowed(true);    
                setDragSource(false);
                setDropTarget(false);
                getOutline().setColumnHidingAllowed(false);
                getOutline().setRenderDataProvider( new NoLeafIconRenderDataProvider( getOutline().getRenderDataProvider() ) );
                getOutline().setDefaultRenderer(Node.Property.class, new PropertyRenderer(getOutline()));
                
                TableMouseListener l = new TableMouseListener(getOutline());
                getOutline().addMouseMotionListener(l);
                getOutline().addMouseListener(l);
                
                setNodePopupFactory(new NodePopupFactory() {
                    @Override
                    public JPopupMenu createPopupMenu(int row, int column, Node[] selectedNodes, Component component) {

                        Action[] actions = NodeOp.findActions (selectedNodes);
                        JPopupMenu res = Utilities.actionsToPopup(actions, component);
                        
                        if (selectedNodes.length == 1 && (HistoryRootNode.isLoadNext(selectedNodes[0]) || HistoryRootNode.isWait(selectedNodes[0]))) {
                            return res;
                        }
                        
                        if ((component instanceof ETable) && (column >= 0)) {
                            ETable et = (ETable)component;
                            if (row >= 0) {
                                Object val = et.getValueAt(row, column);
                                val = et.transformValue(val);
                                String s = NbBundle.getMessage(HistoryFileView.class, "LBL_QuickFilter");
                                res.add(getQuickFilterPopup(et, column, val, s));
                            } else if (et.getQuickFilterColumn() == column) {
                                if (et.getQuickFilterColumn() != -1) {
                                    String s = NbBundle.getMessage(HistoryFileView.class, "LBL_QuickFilter");
                                    JMenu menu = new JMenu(s);
                                    JMenuItem noFilterItem = et.getQuickFilterNoFilterItem(et.getQuickFilterFormatStrings()[6]);
                                    menu.add(noFilterItem);
                                    res.add(menu);
                                }
                            }
                        }
                        return res;
                    }
                    
                    private final String[] quickFilterFormatStrings = new String [] {
                        "{0} == {1}", "{0} <> {1}", "{0} > {1}", 
                        "{0} < {1}", "{0} >= {1}", "{0} <= {1}",
                        NbBundle.getMessage(HistoryFileView.class, "LBL_NoFilter")
                    };
                    
                    public JMenuItem getQuickFilterPopup(ETable et, int column, Object value, String label) {
                        JMenu menu = new JMenu(label);
                        String columnDisplayName = et.getColumnDisplayName(et.getColumnName(column));
                        
                        boolean isDate = value instanceof RevisionNode;
                        
                        JMenuItem equalsItem = et.getQuickFilterEqualsItem(column, value, 
                                columnDisplayName, quickFilterFormatStrings[0], true);
                        menu.add(equalsItem);
                        
                        JMenuItem notequalsItem = et.getQuickFilterEqualsItem(column, value, 
                                columnDisplayName, quickFilterFormatStrings[1], false);
                        menu.add(notequalsItem);
                        
                        if(isDate) {
                            JMenuItem greaterItem = et.getQuickFilterCompareItem(column, value, 
                                    columnDisplayName, quickFilterFormatStrings[2], true, false);
                            menu.add(greaterItem);

                            JMenuItem lessItem = et.getQuickFilterCompareItem(column, value, 
                                    columnDisplayName, quickFilterFormatStrings[3], false, false);
                            menu.add(lessItem);

                            JMenuItem greaterEqualsItem = et.getQuickFilterCompareItem(column, value,
                                    columnDisplayName, quickFilterFormatStrings[4], true, true);
                            menu.add(greaterEqualsItem);

                            JMenuItem lessEqualsItem = et.getQuickFilterCompareItem(column, value,
                                    columnDisplayName, quickFilterFormatStrings[5], false, true);
                            menu.add(lessEqualsItem);
                            JMenuItem noFilterItem = et.getQuickFilterNoFilterItem(quickFilterFormatStrings[6]);
                            menu.add(noFilterItem);
                        }
                        return menu;
                    }                    
                });
            }

            @Override
            public void addNotify() {
                super.addNotify();
                setDefaultColumnSizes();
            }
            
            private void setupColumns() {
                // create colomns
                ResourceBundle loc = NbBundle.getBundle(FileTablePanel.class);            
                if(versioningSystem != null) {
                    addPropertyColumn(RevisionNode.PROPERTY_NAME_VERSION, loc.getString("LBL_LocalHistory_Column_Version"));                    // NOI18N            
                    setPropertyColumnDescription(RevisionNode.PROPERTY_NAME_VERSION, loc.getString("LBL_LocalHistory_Column_Version_Desc"));    // NOI18N            
                    addPropertyColumn(RevisionNode.PROPERTY_NAME_USER, loc.getString("LBL_LocalHistory_Column_User"));                          // NOI18N            
                    setPropertyColumnDescription(RevisionNode.PROPERTY_NAME_USER, loc.getString("LBL_LocalHistory_Column_User_Desc"));          // NOI18N            
                } 
                addPropertyColumn(RevisionNode.PROPERTY_NAME_LABEL, loc.getString("LBL_LocalHistory_Column_Label"));                            // NOI18N            
                setPropertyColumnDescription(RevisionNode.PROPERTY_NAME_LABEL, loc.getString("LBL_LocalHistory_Column_Label_Desc"));            // NOI18N            

                // comparators
                ETableColumnModel m = (ETableColumnModel) getOutline().getColumnModel();
                ETableColumn etc = (ETableColumn) m.getColumn(0);
                etc.setNestedComparator(new NodeComparator(etc));
                m.setColumnSorted(etc, false, 1);                
                int idx = 1;
                if(versioningSystem != null) {
                    setPropertyComparator(idx++);                    
                    setPropertyComparator(idx++);
                }
                setPropertyComparator(idx++);                
            }    

            private void setPropertyComparator(int idx) {
                ETableColumn etc1 = (ETableColumn) getOutline().getColumnModel().getColumn(idx++);
                etc1.setNestedComparator(new PropertyComparator(etc1));
            }
            
            private void setDefaultColumnSizes() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int width = getWidth();                    
                        getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 20 / 100);
                        if(versioningSystem != null) {
                            getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 10 / 100);                        
                            getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 10 / 100);                        
                            getOutline().getColumnModel().getColumn(3).setPreferredWidth(width * 60 / 100);                        
                        } else {
                            getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 80 / 100);                        
                        }
                    }
                });
            }            
    
            private class NodeComparator implements Comparator<Node> {
                private final ETableColumn etc;
                public NodeComparator(ETableColumn etc) {
                    this.etc = etc;
                }
                @Override
                public int compare(Node n1, Node n2) {
                    if(HistoryRootNode.isLoadNext(n1) || HistoryRootNode.isWait(n1)) {
                        return etc.isAscending() ? 1 : -1;
                    } else if(HistoryRootNode.isLoadNext(n2) || HistoryRootNode.isWait(n2)) {
                        return etc.isAscending() ? -1 : 1;
                    }
                    if(n1 instanceof Comparable) {
                        return ((Comparable)n1).compareTo(n2);
                    }
                    if(n2 instanceof Comparable) {
                        return -((Comparable)n2).compareTo(n1);
                    }
                    return n1.getName().compareTo(n2.getName());
                }
            }
            private class PropertyComparator implements Comparator<TableEntry> {
                private final ETableColumn etc;

                public PropertyComparator(ETableColumn etc) {
                    this.etc = etc;
                }
                @Override
                public int compare(TableEntry e1, TableEntry e2) {
                    if(e1 == null && e2 == null) {
                        return 0;
                    }
                    if(e1 == null) {
                        return -1;
                    }
                    if(e2 == null) {
                        return 1;
                    }
                    Integer so1 = e1.order();
                    Integer so2 = e2.order();
                    int c = so1.compareTo(so2);
                    if(c == 0) {
                        return e1.compareTo(e2);
                    }
                    return etc.isAscending() ? -c : c;
                }
            }
    
            private class NoLeafIconRenderDataProvider implements RenderDataProvider {
                private RenderDataProvider delegate;
                public NoLeafIconRenderDataProvider( RenderDataProvider delegate ) {
                    this.delegate = delegate;
                }

                @Override
                public String getDisplayName(Object o) {
                    Node n = Visualizer.findNode(o);
                    if(HistoryRootNode.isLoadNext(n)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("<html>"); // NOI18N
                        appendHyperlinkHTMLFont(sb);   
                        sb.append(delegate.getDisplayName(o));
                        sb.append("</font></html>"); // NOI18N
                        return sb.toString();
                    }
                    return delegate.getDisplayName(o);
                }

                @Override
                public boolean isHtmlDisplayName(Object o) {
                    Node n = Visualizer.findNode(o);
                    if(HistoryRootNode.isLoadNext(n)) {
                        return true;
                    }
                    return delegate.isHtmlDisplayName(o);
                }

                @Override
                public Color getBackground(Object o) {
                    Color b = delegate.getBackground(o);
                    return b;
                }

                @Override
                public Color getForeground(Object o) {
                    return delegate.getForeground(o);
                }

                @Override
                public String getTooltipText(Object o) {
                    return delegate.getTooltipText(o);
                }

                @Override
                public Icon getIcon(Object o) {
                    final Node n = Visualizer.findNode(o);
                    if(HistoryRootNode.isWait(n)) {
                        return delegate.getIcon(o);
                    }
                    if(getOutline().getOutlineModel().isLeaf(o) || HistoryRootNode.isLoadNext(n))
                        return NO_ICON;
                    return null;
                }

            }
        }     
    }    
    
    private static final Icon NO_ICON = new NoIcon();
    private static class NoIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { }
        @Override
        public int getIconWidth() { return 0; }
        @Override
        public int getIconHeight() { return 0; }
    }

    private boolean containsHyperlink(String message) throws IllegalAccessException {
        int[] spans = getHyperlinkSpans(message);
        return spans != null && spans.length >= 2;
    }
    private int[] getHyperlinkSpans(String message) throws IllegalAccessException {
        List<VCSHyperlinkProvider> providers = History.getInstance().getHyperlinkProviders();
        for (VCSHyperlinkProvider hp : providers) {
            int[] spans = hp.getSpans(message);
            if (spans != null && spans.length >= 2) {
                return spans;
            }
        }
        return null;
    }
            
    private class PropertyRenderer extends DefaultOutlineCellRenderer {
        private final Outline outline;
        public PropertyRenderer(Outline outline) {
            this.outline = outline;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            if((value instanceof Node.Property)) {
                try {
                    String valueString = getDisplayValue((Node.Property) value);
                    valueString = HistoryUtils.computeFitText(table, row, column, valueString);
                    
                    if(filter != null && filter.filtersProperty((Node.Property) value)) {
                        valueString = filter.getRendererValue(valueString);
                    } else {
                        valueString = HistoryUtils.escapeForHTMLLabel(valueString); 
                    }
                    if(value instanceof RevisionNode.MessageProperty) {
                        String[] lines = valueString.split("\n"); // NOI18N
                        if(lines.length > 0) {
                            int[] spans = getHyperlinkSpans(lines[0]);
                            if (spans != null && spans.length >= 2) {
                                StringBuilder sb = new StringBuilder();
                                String line = addHyperlink(HistoryUtils.escapeForHTMLLabel(lines[0]), spans);
                                sb.append(line);
                                for (int i = 1; i < lines.length; i++) {
                                    sb.append("\n"); // NOI18N
                                    sb.append(HistoryUtils.escapeForHTMLLabel(lines[i]));
                                }
                                valueString = sb.toString();
                            }
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>"); // NOI18N
                    sb.append(valueString);
                    sb.append("</html>"); // NOI18N
                    valueString = sb.toString();
                    
                    renderer = super.getTableCellRendererComponent(table, valueString, isSelected, hasFocus, row, column);
                    if(renderer instanceof JComponent) {
                        JComponent comp = (JComponent)renderer;
                        comp.setToolTipText(getTooltip((Node.Property) value));
                    }
                } catch (Exception ex) {
                    History.LOG.log(Level.WARNING, null, ex);
                    renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            renderer.setBackground (isSelected ? outline.getSelectionBackground() : outline.getBackground());
            return renderer;
        }
        
        String getDisplayValue(Node.Property p) throws IllegalAccessException, InvocationTargetException {
            TableEntry value = (TableEntry) p.getValue();
            return value != null ? value.getDisplayValue() : ""; // NOI18N
        }
        
        String getTooltip(Node.Property p) throws IllegalAccessException, InvocationTargetException {
            Object value = p.getValue();
            if(value instanceof TableEntry) {
                return ((TableEntry) value).getTooltip();
            }
            String tooltip = p.toString();
            if(tooltip != null && tooltip.contains("\n")) { // NOI18N
                tooltip = HistoryUtils.escapeForHTMLLabel(tooltip);
                StringBuilder sb = new StringBuilder();
                sb.append("<html>"); // NOI18N
                StringTokenizer st = new StringTokenizer(tooltip, "\n"); // NOI18N
                while(true) {
                    sb.append(st.nextToken());
                    if(st.hasMoreTokens()) {
                        sb.append("<br>"); // NOI18N
                    } else {
                        break;
                    }
                }
                sb.append("</html>"); // NOI18N
                tooltip = sb.toString();
            } 
            return tooltip;
            
        }

        private String addHyperlink(String s, int[] spans) {
            assert spans.length % 2 == 0;
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < spans.length) {
                int start = spans[i++];
                if(i == 1) {
                    sb.append(s.substring(0, start));
                    appendHyperlinkHTMLFont(sb); 
                    sb.append("<u>"); // NOI18N
                }
                int end = spans[i++];
                sb.append(s.substring(start, end));
                if(i == spans.length) {
                    sb.append("</u></font>"); // NOI18N
                    sb.append(s.substring(end));
                }
            }
            return sb.toString();
        }
    }
    
    private class TableMouseListener implements MouseListener, MouseMotionListener {
        private final Outline outline;
        private boolean pressedPopup = false;
        public TableMouseListener(Outline outline) {
            this.outline = outline;
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {}

        @Override
        public void mouseMoved(MouseEvent e) {
            try {
                Object value = getValue(e);
                if(value instanceof MessageProperty) {
                    MessageProperty msg = (MessageProperty) value;
                    if(msg == null || !containsHyperlink(msg.getValue().getDisplayValue())) {
                        outline.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    } else {
                        outline.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                }
            } catch (Exception ex) {
                History.LOG.log(Level.WARNING, null, ex);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath path = outline.getClosestPathForLocation(e.getX(), e.getY());
            if(path == null) {
                return;
            }
            Node n = Visualizer.findNode(path.getPathComponent(1)); // XXX idx 1
            if(n == null) {
                return;
            }
            if(!pressedPopup && HistoryRootNode.isLoadNext(Visualizer.findNode(n))) {
                loadNextAction.actionPerformed(null);
            } else {
                Object value = getValue(e);
                if(!(value instanceof MessageProperty)) {
                    return;
                }
                MessageProperty messageProperty = (MessageProperty) value;
                if(messageProperty == null) {
                    return;
                }
                try {
                    outline.getSelectedRow();

                    HistoryEntry entry = n.getLookup().lookup(HistoryEntry.class);
                    if(entry == null) {
                        return;
                    }
                    String message = entry.getMessage();
                    String author = entry.getUsername();
                    String revision = entry.getRevision();
                    Date date = entry.getDateTime();
                    FileObject[] files = tc.getFiles();
                    VCSFileProxy file = files[0] != null ? VCSFileProxy.createFileProxy(files[0]) : null;
                    MsgTooltipWindow ttw = new MsgTooltipWindow(outline, file, message, revision, author, date);
                    Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, outline);
                    ttw.show(new Point(p.x, p.y));
                } catch (Exception ex) {
                    History.LOG.log(Level.WARNING, null, ex);
                } 
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            pressedPopup = e.isPopupTrigger();
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
                    
        private Object getValue(MouseEvent e) {
            int r = outline.rowAtPoint(e.getPoint());
            int c = outline.columnAtPoint(e.getPoint());
            if(r == -1 || c == -1) {
                return null;
            }
            return outline.getValueAt(r, c);
        }
    }
    
    private class LoadNextAction extends AbstractAction {
        public LoadNextAction() {
            refreshName();
        }
        private void refreshName() {
            final String name;
            if(HistorySettings.getInstance().getLoadAll()) {
                name = NbBundle.getMessage(HistoryRootNode.class,  "LBL_LoadAll"); // NOI18N
            } else {
                name = NbBundle.getMessage(HistoryRootNode.class,  "LBL_LoadNext", HistorySettings.getInstance().getIncrements()); // NOI18N
            }
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    putValue(Action.NAME, name);
                    HistoryRootNode rootNode = getRootNode();
                    if(rootNode != null) {
                        rootNode.refreshLoadNextName();
                    }
                }
            };
            if(EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            loadVCSEntries(History.toProxies(tc.getFiles()), false); 
        }
    }

    private static void appendHyperlinkHTMLFont(StringBuilder sb) {
        sb.append("<font color=#");// NOI18N
        sb.append(Integer.toHexString(ColorManager.getDefault().getLinkColor().getRGB() & 0xffffff));
        sb.append(">"); // NOI18N
    }
}
