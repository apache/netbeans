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
package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.netbeans.modules.editor.bookmarks.BookmarkManager;
import org.netbeans.modules.editor.bookmarks.BookmarkManagerEvent;
import org.netbeans.modules.editor.bookmarks.BookmarkManagerListener;
import org.netbeans.modules.editor.bookmarks.BookmarkUtils;
import org.netbeans.modules.editor.bookmarks.ProjectBookmarks;
import org.netbeans.swing.etable.ETable;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * View of all currently known bookmarks (from all opened projects).
 *
 * @author Miloslav Metelka
 */
public final class BookmarksView extends TopComponent
implements BookmarkManagerListener, PropertyChangeListener, ExplorerManager.Provider
{
    private static final String HELP_ID = "bookmarks_window_csh"; // NOI18N

    /**
     * Invoked from layer.
     * @return bookmarks view instance.
     */
    public static TopComponent create() {
        return new BookmarksView();
    }

    public static BookmarksView openView() {
        BookmarksView bookmarksView = (BookmarksView) WindowManager.getDefault().findTopComponent("bookmarks"); // NOI18N
        if (bookmarksView == null) {
            bookmarksView = (BookmarksView) create();
        }
        bookmarksView.open();
        bookmarksView.requestActive();
        return bookmarksView;
    }

    private static final ActionListener OPEN_ACTION  = (ActionEvent e) -> {
        openView();
    };

    public static ActionListener openAction() {
        return OPEN_ACTION;
    }

    private final transient BookmarksNodeTree nodeTree;
    private final transient ExplorerManager explorerManager;
    private transient boolean treeViewShowing; // Whether viewed as tree or as a table
    private transient JSplitPane splitPane;
    private transient BookmarksTableView tableView;
    private transient BeanTreeView treeView;
    private transient JPanel previewPanel;

    private transient boolean dividerLocationUpdating;

    private transient JToggleButton bookmarksTreeButton;
    private transient JToggleButton bookmarksTableButton;
    private transient JToggleButton showPreviewButton;

    private transient BookmarkInfo displayedBookmarkInfo;

    private transient boolean initialTreeSelectionDone;

    private static final String PREFS_NODE = "BookmarksProperties"; //NOI18N
    private static final Preferences prefs = NbPreferences.forModule(BookmarksView.class).node(PREFS_NODE);
    private static final String TREE_VIEW_VISIBLE_PREF = "treeViewVisible"; //NOI18N
    private static final String PREVIEW_VISIBLE_PREF = "previewVisible"; //NOI18N
    private static final String DIVIDER_LOCATION_PREF = "dividerLocation"; //NOI18N

    @SuppressWarnings("LeakingThisInConstructor")
    BookmarksView() {
//        getActionMap().put("rename", SystemAction.get(RenameAction.class));
        nodeTree = new BookmarksNodeTree();
        explorerManager = new ExplorerManager();
        explorerManager.setRootContext(nodeTree.rootNode());
        ActionMap actionMap = getActionMap();
        actionMap.put("delete", ExplorerUtils.actionDelete(explorerManager, false)); //NOI18N
        associateLookup(ExplorerUtils.createLookup(explorerManager, actionMap));
        explorerManager.addPropertyChangeListener(this);

        // Ctrl+T will toggle the tree/table view
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "toggle-view"); //NOI18N
        actionMap.put("toggle-view", new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                setTreeViewVisible(!treeViewShowing);
            }
        });
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/editor/bookmarks/resources/bookmark_16.png")); // NOI18N
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public String getName () {
        return NbBundle.getMessage (BookmarksView.class, "LBL_BookmarksView");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }

    @Override
    protected String preferredID() {
        return "bookmarks"; // NOI18N
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText () {
        return NbBundle.getMessage (BookmarksView.class, "LBL_BookmarksViewToolTip");// NOI18N
    }

    private void initLayoutAndComponents() {
        if (previewPanel == null) { // Not inited yet
            setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints;
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 1;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = GridBagConstraints.NONE;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.weighty = 0.0;
            add(createLeftToolBar(), gridBagConstraints);

            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setContinuousLayout(true);
            splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, pce -> {
                if(splitPane.getLeftComponent() != null && splitPane.getRightComponent() != null && ! dividerLocationUpdating) {
                    prefs.putInt(DIVIDER_LOCATION_PREF, splitPane.getDividerLocation());
                }
            });

            previewPanel = new JPanel();
            previewPanel.setLayout(new GridLayout(1, 1));
            fixScrollPaneinSplitPaneJDKIssue(previewPanel);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridheight = 1;
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(splitPane, gridBagConstraints);

            // Make treeView visible
            setTreeViewVisible(prefs.getBoolean(TREE_VIEW_VISIBLE_PREF, true));

            BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
            try {
                lockedBookmarkManager.addBookmarkManagerListener(
                    WeakListeners.create(BookmarkManagerListener.class, this, lockedBookmarkManager));
            } finally {
                lockedBookmarkManager.unlock();
            }

            updateSplitPane();
        }
    }

    @Override
    public void bookmarksChanged(final BookmarkManagerEvent evt) {
        updateTreeRootContext(evt);
        if (!initialTreeSelectionDone) {
            SwingUtilities.invokeLater(() -> {
                doInitialSelection();
            });
        }
    }

    private void setTreeViewVisible(boolean treeViewVisible) {
        prefs.putBoolean(TREE_VIEW_VISIBLE_PREF, treeViewVisible);
        if (treeViewVisible != this.treeViewShowing) {
            this.treeViewShowing = treeViewVisible;
            updateSplitPane();
        }
    }

    private void updateSplitPane() {
        TreeOrTableContainer container;
        boolean create;
        dividerLocationUpdating = true;
        try {
            if (treeViewShowing) {
                create = (treeView == null);
                if (create) {
                    container = new TreeOrTableContainer();
                    updateTreeRootContext(null);
                    treeView = new BeanTreeView();
                    container.add(treeView);
                    fixScrollPaneinSplitPaneJDKIssue(treeView);
                    treeView.setRootVisible(false);
                    treeView.setDragSource(false);
                    treeView.setDropTarget(false);
                } else {
                    container = (TreeOrTableContainer) treeView.getParent();
                }

            } else { // Tree view visible
                create = (tableView == null);
                if (create) {
                    tableView = new BookmarksTableView();
                    container = new TreeOrTableContainer();
                    container.add(tableView);
                    rebuildTableEntries();
                    initTableView();
                } else {
                    container = (TreeOrTableContainer) tableView.getParent();
                }
            }
            splitPane.setLeftComponent(container);
            if (!treeViewShowing && create) {
                // Ensure layout is done and we get sane widths when setting up
                // columns
                splitPane.getParent().doLayout();
                splitPane.validate();
                updateTableColumnSizes();
            }
            if (showPreviewButton.isSelected()) {
                splitPane.setRightComponent(previewPanel);
                splitPane.setDividerLocation(prefs.getInt(DIVIDER_LOCATION_PREF, 400));
            } else {
                splitPane.setRightComponent(null);
            }
            bookmarksTreeButton.setSelected(treeViewShowing);
            bookmarksTableButton.setSelected(!treeViewShowing);
            requestFocusTreeOrTable();
        } finally {
            dividerLocationUpdating = false;
        }
    }

    private void updateTreeRootContext(BookmarkManagerEvent evt) {
        boolean structureChange = (evt == null) || evt.isStructureChange();
        if (structureChange) {
            updateNodeTree();

        } else {
            // Only update bookmark node properties (no project access)
            nodeTree.updateBookmarkNodes(evt);
            notifyTableEntriesChanged(evt);
        }
    }

    private void updateNodeTree() {
        nodeTree.updateNodeTree();
        SwingUtilities.invokeLater(this::rebuildTableEntries);
    }

    private void rebuildTableEntries() {
        if (tableView != null) {
            BookmarksTable table = tableView.getTable();
            int selectedIndex = Math.max(table.getSelectedRow(), 0); // If no selection request first row selection
            ((BookmarksTableModel)table.getModel()).setEntries(nodeTree.bookmarkNodes(false));
            selectedIndex = Math.min(selectedIndex, table.getRowCount() - 1);
            if (selectedIndex >= 0) {
                table.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
            }
        }
    }

    private void notifyTableEntriesChanged(BookmarkManagerEvent evt) {
        if (tableView != null) {
            BookmarksTable table = tableView.getTable();
            BookmarksTableModel model = (BookmarksTableModel) table.getModel();
            for (int i = model.getEntryCount() - 1; i >= 0; i--) {
                if (evt.getChange(model.getEntry(i).getBookmarkInfo()) != null) {
                    model.fireTableRowsUpdated(i, i);
                }
            }
        }
    }

    private void initTableView() {
        fixScrollPaneinSplitPaneJDKIssue(tableView);
        // ETable defines "enter" action => change its meaning
        tableView.getTable().getActionMap().put("enter", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                BookmarkInfo selectedBookmark = getTableSelectedBookmark();
                if (selectedBookmark != null) {
                    BookmarkUtils.postOpenEditor(selectedBookmark);
                }
            }
        });
        tableView.getTable().getActionMap().put("delete", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                BookmarkInfo selectedBookmark = getTableSelectedBookmark();
                if (selectedBookmark != null) {
                    BookmarkUtils.removeBookmarkUnderLock(selectedBookmark);
                }
            }
        });
        tableView.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !tableView.getTable().getSelectionModel().isSelectionEmpty()) {
                checkShowPreview();
            }
        });
    }

    private void updateTableColumnSizes() {
        ETable table = tableView.getTable();
        Font font = tableView.getFont();
        FontMetrics fm = tableView.getFontMetrics(font);
        int maxCharWidth = fm.charWidth('A');
        int editingBorder = 4;
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn nameColumn = columnModel.getColumn(0);
        nameColumn.setPreferredWidth(8 * maxCharWidth + editingBorder); // 8 chars for name

        TableColumn keyColumn = columnModel.getColumn(1);
        // Single char for key (but 3 chars to prevent "..." in column header)
        keyColumn.setPreferredWidth(3 * maxCharWidth + editingBorder);
        keyColumn.setMinWidth(keyColumn.getPreferredWidth());

        TableColumn locationColumn = columnModel.getColumn(2);
        Insets insets = tableView.getBorder().getBorderInsets(tableView);
        int remainingWidth = tableView.getParent().getWidth() - insets.left - insets.right;
        remainingWidth -= 2 * columnModel.getColumnMargin();
        remainingWidth -= nameColumn.getPreferredWidth();
        remainingWidth -= keyColumn.getPreferredWidth();
        locationColumn.setPreferredWidth(remainingWidth); // remaining space for location
    }

    void requestFocusTreeOrTable() {
        if (treeViewShowing) {
            treeView.requestFocusInWindow();
        } else if (tableView != null) {
            tableView.getTable().requestFocusInWindow();
        }
        Node selectedNode = getTreeSelectedNode();
        if (selectedNode == null) {
            Children rootChildren = explorerManager.getRootContext().getChildren();
            if (rootChildren.getNodesCount() > 0) {
                try {
                    explorerManager.setSelectedNodes(new Node[] { rootChildren.getNodeAt(0) });
                } catch (PropertyVetoException ex) {
                    // Ignored
                }
            }
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        requestFocusTreeOrTable();
        return isFocusable();
    }

    void refreshView() {
        updateTreeRootContext(null);
        requestFocusTreeOrTable();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        ExplorerUtils.activateActions(explorerManager, true);
        requestFocusTreeOrTable();
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(explorerManager, false);
        super.componentDeactivated();
    }

    @Override
    protected void componentShowing() {
        // Ensure all bookmarks from all projects loaded
        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
        try {
            lockedBookmarkManager.keepOpenProjectsBookmarksLoaded();
        } finally {
            lockedBookmarkManager.unlock();
        }
        initLayoutAndComponents();
        doInitialSelection();
        super.componentShowing();
    }

    void checkShowPreview() {
        BookmarkInfo selectedBookmark = null;
        if (treeViewShowing) {
            Node selectedNode = getTreeSelectedNode();
            if (selectedNode instanceof BookmarkNode) {
                BookmarkNode bmNode = (BookmarkNode) selectedNode;
                selectedBookmark = bmNode.getBookmarkInfo();
            }
        } else {
            selectedBookmark = getTableSelectedBookmark();
        }

        if (selectedBookmark != null) {
            final BookmarkInfo bookmark = selectedBookmark;
            if (bookmark != displayedBookmarkInfo) {
                final FileObject fo = bookmark.getFileBookmarks().getFileObject();
                if (fo != null) {
                    try {
                        DataObject dob = DataObject.find(fo);
                        final EditorCookie ec = dob.getCookie(EditorCookie.class);
                        if (ec != null) {
                            Document doc = ec.getDocument();
                            if (doc == null) {
                                // Open document on background
                                RequestProcessor.getDefault().post(() -> {
                                    try {
                                        final Document d = ec.openDocument();
                                        SwingUtilities.invokeLater(() -> {
                                            showPreview(fo, d, bookmark);
                                        });
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                });
                            } else { // doc != null
                                showPreview(fo, doc, bookmark);
                            }
                        }
                    } catch (DataObjectNotFoundException ex) {
                        // Ignore preview
                    }
                } // else: file does not exist -> ignore preview
            }
        }
    }

    void showPreview(FileObject fo, Document doc, BookmarkInfo bookmarkInfo) {
        if (bookmarkInfo != displayedBookmarkInfo) {
            int lineIndex = bookmarkInfo.getCurrentLineIndex();
            String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
            if (mimeType != null) {
                JEditorPane pane = new JEditorPane();
                EditorKit editorKit = MimeLookup.getLookup(mimeType).lookup(EditorKit.class);
                pane.setEditorKit(editorKit);
                pane.setDocument(doc);
                pane.setEditable(false);
                Component editorComponent;
                EditorUI editorUI = Utilities.getEditorUI(pane);
                if (editorUI != null) {
                    editorComponent = editorUI.getExtComponent();
                } else {
                    editorComponent = new JScrollPane(pane);
                }
                previewPanel.removeAll();
                previewPanel.add(editorComponent);

                int offset = BookmarkUtils.lineIndex2Offset(doc, lineIndex);
                pane.setCaretPosition(offset);
                displayedBookmarkInfo = bookmarkInfo;

                previewPanel.revalidate();
            }
        }
    }

    Node getTreeSelectedNode() {
        Node selectedNode = null;
        if (treeViewShowing) {
            Node[] selectedNodes = explorerManager.getSelectedNodes();
            if (selectedNodes.length > 0) {
                selectedNode = selectedNodes[0];
            }
        }
        return selectedNode;
    }

    BookmarkInfo getTableSelectedBookmark() {
        BookmarksTable table = tableView.getTable();
        int selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());
        if (selectedRowIndex != -1 && selectedRowIndex < table.getRowCount()) {
            return ((BookmarksTableModel)table.getModel()).getEntry(selectedRowIndex).getBookmarkInfo();
        }
        return null;
    }

    /// Perform initial tree expansion and selection
    private void doInitialSelection() {
        if (!initialTreeSelectionDone) {
            if (treeViewShowing) {
                Node selectedNode = getTreeSelectedNode();
                if (selectedNode instanceof BookmarkNode) {
                    initialTreeSelectionDone = true;
                } else {
                    FileObject selectedFileObject = org.openide.util.Utilities.actionsGlobalContext().lookup(FileObject.class);
                    if (selectedFileObject != null) {
                        BookmarkManager lockedBookmarkManager = BookmarkManager.getLocked();
                        try {
                            ProjectBookmarks projectBookmarks = lockedBookmarkManager.getProjectBookmarks(selectedFileObject);
                            Node bNode = nodeTree.findFirstBookmarkNode(projectBookmarks, selectedFileObject);
                            if (bNode != null) {
                                initialTreeSelectionDone = true;
                                try {
                                    explorerManager.setSelectedNodes(new Node[]{bNode});
                                } catch (PropertyVetoException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        } finally {
                            lockedBookmarkManager.unlock();
                        }
                    } else {
                        Lookup.Result<FileObject> result = org.openide.util.Utilities.actionsGlobalContext().lookupResult(FileObject.class);
                        LookupListener onFirstFocus = new LookupListener() {
                            @Override public void resultChanged(LookupEvent ev) {
                                result.removeLookupListener(this);
                                doInitialSelection();
                            }
                        };
                        result.addLookupListener(onFirstFocus);
                    }
                }
            }
        }
    }

    private JToolBar createLeftToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation(SwingConstants.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.setBorderPainted(true);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
        }

        JButton refreshButton = new JButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/refresh.png", false));
        refreshButton.setToolTipText(NbBundle.getMessage(BookmarksView.class, "LBL_toolBarRefreshButtonToolTip"));
        refreshButton.addActionListener((ActionEvent e) -> refreshView());
        toolBar.add(refreshButton);

        toolBar.addSeparator();
        bookmarksTreeButton = new JToggleButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/bookmarksTree.png", false));
        bookmarksTreeButton.setToolTipText(NbBundle.getMessage(BookmarksView.class, "LBL_toolBarTreeViewButtonToolTip"));
        bookmarksTreeButton.addActionListener((ActionEvent e) -> {
            if (!treeViewShowing) {
                setTreeViewVisible(true);
            } else {
                bookmarksTableButton.doClick();
            }
        });
        toolBar.add(bookmarksTreeButton);

        bookmarksTableButton = new JToggleButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/bookmarksTable.png", false));
        bookmarksTableButton.setToolTipText(NbBundle.getMessage(BookmarksView.class, "LBL_toolBarTableViewButtonToolTip"));
        bookmarksTableButton.addActionListener((ActionEvent e) -> {
            if (treeViewShowing) {
                setTreeViewVisible(false);
            } else {
                bookmarksTreeButton.doClick();
            }
        });
        toolBar.add(bookmarksTableButton);
        toolBar.addSeparator();

    	showPreviewButton = new JToggleButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/editor/bookmarks/resources/preview.png", false));
        showPreviewButton.setToolTipText(NbBundle.getMessage(BookmarksView.class, "LBL_toolBarShowPreviewButtonToolTip"));
        showPreviewButton.setSelected(prefs.getBoolean(PREVIEW_VISIBLE_PREF, true));
        showPreviewButton.addActionListener((ActionEvent e) -> {
            prefs.putBoolean(PREVIEW_VISIBLE_PREF, showPreviewButton.isSelected());
            updateSplitPane();
        });
        toolBar.add(showPreviewButton);

        return toolBar;
    }

    private static void fixScrollPaneinSplitPaneJDKIssue(Component c) {
        c.setMinimumSize(new Dimension(10, 10)); // Workaround for JSplitPane-containing-JScrollPane JDK bug
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("selectedNodes".equals(evt.getPropertyName())) { //NOI18N
            checkShowPreview();
        }
    }

    private final class TreeOrTableContainer extends JPanel {

        @SuppressWarnings("LeakingThisInConstructor")
        TreeOrTableContainer() {
            // Use GridLayout since BorderLayout does not behave well inside JSplitPane's left component
            // - it centers the contained component
            setLayout(new GridLayout(1, 1));
            fixScrollPaneinSplitPaneJDKIssue(this);
        }

    }


    private static final class BookmarksTableView extends JScrollPane { // Similar construct to explorer's TableView

        BookmarksTableView() {
            setViewportView(new BookmarksTable());
        }

        BookmarksTable getTable() {
            return (BookmarksTable) getViewport().getView();
        }

    }

}
