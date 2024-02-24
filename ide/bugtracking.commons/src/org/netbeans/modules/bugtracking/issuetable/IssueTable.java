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

package org.netbeans.modules.bugtracking.issuetable;

import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableColumn;
import org.openide.util.NbBundle;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.awt.MouseUtils;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * @author Tomas Stupka
 */
public class IssueTable implements MouseListener, AncestorListener, KeyListener, PropertyChangeListener {

    static final Logger LOG = Logger.getLogger(IssueTable.class.getName());
    
    private NodeTableModel  tableModel;
    private JTable          table;
    private final JPanel     component;

    private final TableSorter     sorter;

    private ColumnDescriptor[] descriptors;

    private Filter allFilter;
    private Filter newOrChangedFilter;
    private Filter filter;
    private Filter[] filters;
    private Set<IssueNode> nodes = new HashSet<>();

    private final QueryTableHeaderRenderer queryTableHeaderRenderer;

    private Task storeColumnsTask;
    private final StoreColumnsHandler storeColumnsWidthHandler;
    private final JButton colsButton;
    private boolean savedQueryInitialized;
    private SummaryTextFilter textFilter;

    private static final String CONFIG_DELIMITER = "<=>";                       // NOI18N
    private final FindInQuerySupport findInQuerySupport;
    private boolean isSaved;
    
    private static final Comparator<IssueNode<Object>.IssueProperty<Object>> nodeComparator = new Comparator<IssueNode<Object>.IssueProperty<Object>>() {
        @Override
        public int compare(IssueNode<Object>.IssueProperty<Object> p1, IssueNode<Object>.IssueProperty<Object> p2) {
            Integer sk1 = (Integer) p1.getValue("sortkey"); // NOI18N
            if (sk1 != null) {
                Integer sk2 = (Integer) p2.getValue("sortkey"); // NOI18N
                return sk2 != null ? sk1.compareTo(sk2) : 1;
            } else {
                try {
                    return p1.compareTo(p2);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, null, e);
                    return 0;
                }
            }
        }
    };
    private RequestProcessor rp;
    private final String repositoryId;

    public IssueTable(String repositoryId, String queryName, QueryController controller, ColumnDescriptor[] descriptors, final boolean isSaved) {
        this(repositoryId, queryName, controller, descriptors, isSaved, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    public IssueTable(String repositoryId, String queryName, QueryController controller, ColumnDescriptor[] descriptors, final boolean isSaved, int vsbPolicy, int hsbPolicy) {
        assert descriptors != null;
        assert descriptors.length > 0;

        
        if(queryName == null) {
            queryName = "#find#issues#hitlist#table#";               // NOI18N
        }
        this.repositoryId = repositoryId + ":" + queryName;      // NOI18N
        
        controller.addPropertyChangeListener(this);
        
        this.descriptors = descriptors;
        this.component = new JPanel() {
            @Override
            public void requestFocus() {
                table.requestFocus();
            }
        };
        this.isSaved = isSaved;
        
        initFilters();

        /* table */
        tableModel = new NodeTableModel();
        sorter = new TableSorter(tableModel, this);
        sorter.setColumnComparator(Node.Property.class, nodeComparator);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        table.setRowHeight(table.getRowHeight() * 6 / 5);
        
        JScrollPane tableScrollPane = new JScrollPane(table, vsbPolicy, hsbPolicy);
        tableScrollPane.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        tableScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        ImageIcon ic = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/commons/resources/columns_16.png", true)); // NOI18N
        colsButton = new javax.swing.JButton(ic);
        colsButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TreeTableView.class, "ACN_ColumnsSelector")); //NOI18N
        colsButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TreeTableView.class, "ACD_ColumnsSelector")); //NOI18N
        colsButton.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if(tableModel.selectVisibleColumns()) {
                        setDefaultColumnSizes();
                        storeColumnsTask.schedule(1000);
                    }
                }
            }
        );
        tableScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, colsButton);

        /* find bar */
        findInQuerySupport = FindInQuerySupport.create(this);
        FindInQueryBar findBar = findInQuerySupport.getFindBar();
        
        initComponents(tableScrollPane, findBar);       
        
        table.addMouseListener(this);
        table.addKeyListener(this);
        table.addAncestorListener(findInQuerySupport.getAncestorListener());
        cellRenderer = new QueryTableCellRenderer(this, isSaved);
        table.setDefaultRenderer(Node.Property.class, cellRenderer);
        queryTableHeaderRenderer = new QueryTableHeaderRenderer(table.getTableHeader().getDefaultRenderer(), this);
        queryTableHeaderRenderer.setSaved(isSaved);
        table.getTableHeader().setDefaultRenderer(queryTableHeaderRenderer);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(IssueTable.class, "ACSN_IssueTable")); // NOI18N
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(IssueTable.class, "ACSD_IssueTable")); // NOI18N

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initColumns();
            }
        });
        table.getTableHeader().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                table.getColumnModel().addColumnModelListener(tcml);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                table.getColumnModel().removeColumnModelListener(tcml);
            }
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction"); // NOI18N
        UIUtils.fixFocusTraversalKeys(table);

        storeColumnsWidthHandler = new StoreColumnsHandler();
        storeColumnsTask = getRequestProcessor().create(storeColumnsWidthHandler);
    }
    private final QueryTableCellRenderer cellRenderer;

    private void initComponents(JScrollPane tablePane, FindInQueryBar findBar) {
        GroupLayout layout = new GroupLayout(component);
        component.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(findBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tablePane, GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(tablePane, GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(findBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }
 
    /**
     * Returns the issue table filters
     * @return
     */
    public Filter[] getDefinedFilters() {
        return filters;
    }

    public Filter getAllFilter() {
        return allFilter;
    }
    
    public Filter getNewOrChangedFilter() {
        return newOrChangedFilter;
    }
    
    /**
     * Reset the filter criteria set in
     * {@link #setFilterBySummary(java.lang.String, boolean, boolean, boolean) }
     */
    public void resetFilterBySummary() {
        setFilterIntern(filter);
    }

    /**
     * Switch highlighting in rows matching the filter criteria set in
     * {@link #setFilterBySummary(java.lang.String, boolean, boolean, boolean) }
     * @param on
     */
    public void switchFilterBySummaryHighlight(boolean on) {
        assert textFilter != null;
        if(textFilter == null) {
            return;
        }
        textFilter.setHighlighting(on);
        table.repaint();
    }

    /**
     * Given values are used to filter the current issue hitlist
     *
     * @param searchText
     * @param regular
     * @param wholeWords
     * @param matchCase
     */
    public void setFilterBySummary(String searchText, boolean regular, boolean wholeWords, boolean matchCase) {
        if(textFilter == null) {
            textFilter = new SummaryTextFilter();
        }
        textFilter.setText(searchText, regular, wholeWords, matchCase);
        setFilterIntern(textFilter);
    }

    /**
     * Sets the renderer in the underlying JTable
     * @param renderer
     */
    public void setRenderer(TableCellRenderer renderer) {
        table.setDefaultRenderer(Node.Property.class, renderer);
    }

    /**
     * Gets the renderer from the underlying JTable
     * @return
     */
    public TableCellRenderer getRenderer() {
        return table.getDefaultRenderer(Node.Property.class);
    }

    /**
     * Sets a filter on the current issue hitlist
     * @param filter
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
        setFilterIntern(filter);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(QueryController.PROP_CHANGED)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    isSaved = true;
                    cellRenderer.setSaved(true);
                    queryTableHeaderRenderer.setSaved(true);
                    initColumns();
                }
            });
        }
    }

    /**
     * Returns a UI component holding this tables visual representation
     * @return
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * Sets visible columns in the Versioning table.
     */
    public final void initColumns() {
        if(savedQueryInitialized) {
            return;
        }
        setModelProperties();
        if(descriptors.length > 0) {
            Map<Integer, Integer> sorting = getColumnSorting();
            if(descriptors.length > 1) {
                for (int i = 0; i < descriptors.length; i++) {
                    int visibleIdx = tableModel.getVisibleIndex(i);
                    Integer order = sorting.get(visibleIdx);
                    if(order != null) {
                        sorter.setSortingStatus(visibleIdx, order); 
                    } else {
                        if(i == 0) {
                            sorter.setSortingStatus(0, TableSorter.ASCENDING); // default sorting by first column
                        } else {
                            sorter.setColumnComparator(i, null);
                            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
                        }                        
                    }
                }
            }
        }
        setDefaultColumnSizes();
        if(isSaved) {
            savedQueryInitialized = true;
        }
    }

    /**
     * Callback from sorter. It also throws an event when the order is changed, unfortunately
     * that also applies for changes caused by refreshing a query and there is no way to 
     * distinguish between those events. 
     */
    void sortOrderChanged() {
        // sorting changed
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < sorter.getColumnCount(); i++) {
            if(i > 0) {
                sb.append(CONFIG_DELIMITER);
            }
            sb.append(i).append(CONFIG_DELIMITER).append(sorter.getSortingStatus(i));
        }
        storeColumnSorting(repositoryId, sb.toString());
    }

    private Map<Integer, Integer> getColumnSorting() {
        String sortingString = getColumnSorting(repositoryId);
        if(sortingString == null || sortingString.equals("")) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> map = new HashMap<>();
        String[] sortingArray = sortingString.split(CONFIG_DELIMITER);
        for (int i = 0; i < sortingArray.length; i+=2) {
            try {
                map.put(Integer.parseInt(sortingArray[i]),
                        Integer.parseInt(sortingArray[i + 1]));
            } catch (    NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOG.log(Level.FINE, null, e);
            }
        }
        return map;
    }

    private void initFilters() {
        allFilter = Filter.getAllFilter();
        newOrChangedFilter = Filter.getNotSeenFilter();
        filters = new Filter[]{allFilter, newOrChangedFilter};
        filter = allFilter;
    }
    
    int getSeenColumnIdx() {
        return tableModel.getIndexForPropertyName(IssueNode.LABEL_NAME_SEEN);
    }

    int getRecentChangesColumnIdx() {
        return tableModel.getIndexForPropertyName(IssueNode.LABEL_RECENT_CHANGES);
    }

    private void setFilterIntern(Filter filter) {
        final List<IssueNode> filteredNodes = new ArrayList<>(nodes.size());
        for (IssueNode node : nodes) {
            if (filter == null || filter.accept(node)) {
                filteredNodes.add(node);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setTableModel(filteredNodes.toArray(new IssueNode[0]));
            }
        });
    }

    SummaryTextFilter getSummaryFilter() {
        return textFilter;
    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("Issue table", 5);
        }
        return rp; 
    }

    private static class CellAction implements ActionListener {
        private final Rectangle bounds;
        private final ActionListener listener;
        public CellAction(Rectangle bounds, ActionListener listener) {
            this.bounds = bounds;
            this.listener = listener;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CellAction other = (CellAction) obj;
            if (this.bounds != other.bounds && (this.bounds == null || !this.bounds.equals(other.bounds))) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");             // NOI18N
            sb.append("bounds=");       // NOI18N
            sb.append(bounds);
            sb.append("]");             // NOI18N
            return sb.toString();
        }
        public void actionPerformed(ActionEvent e) {
            listener.actionPerformed(e);
        }
    }

    private class Cell {
        private final int row;
        private final int column;
        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cell other = (Cell) obj;
            if (this.row != other.row) {
                return false;
            }
            if (this.column != other.column) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            return toString().hashCode();
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[");         // NOI18N
            sb.append("row=");      // NOI18N
            sb.append(row);
            sb.append(",column=");  // NOI18N
            sb.append(column);
            sb.append("]");         // NOI18N
            return sb.toString();
        }
    }
    private final Map<Cell, Set<CellAction>> cellActions = new HashMap<Cell, Set<CellAction>>();

    public void addCellAction(int row, int column, Rectangle bounds, ActionListener l) {
        synchronized(cellActions) {
            Cell cell = new Cell(row, column);
            Set<CellAction> actions = cellActions.get(cell);
            if(actions == null) {
                actions = new HashSet<CellAction>(1);
                cellActions.put(cell, actions);
            }
            actions.add(new CellAction(bounds, l));
        }
    }

    public void removeCellActions(int row, int column) {
        Cell cell = new Cell(row, column);
        synchronized(cellActions) {
            cellActions.remove(cell);
        }
    }

    void setDefaultColumnSizes() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int[] widths = getColumnWidths(repositoryId);
                Map<String, Integer> persistedColumnsMap = getPersistedColumnValues();
                if(persistedColumnsMap.size() > 0) {
                    final TableColumnModel columnModel = table.getColumnModel();
                    int columnCount = columnModel.getColumnCount();
                    for (int i = 0; i < columnCount; i++) {
                        String id = tableModel.getColumnId(i);
                        Integer w = persistedColumnsMap.get(id);
                        if(w != null && w > 0) {
                            setColumnWidth(i, w);
                        }
                    }
                } else if(widths != null && widths.length > 0) {
                    // XXX for backward comp. remove together with BugtrackingConfig.getInstance().getColumnWidths
                    int columnCount = table.getColumnModel().getColumnCount();
                    for (int i = 0; i < widths.length && i < columnCount; i++) {
                        int w = widths[i];
                        if(w > 0) {
                            setColumnWidth(i, w);
                        }
                    }
                } else {
                    ColumnDescriptor[] visibleDescriptors = getVisibleDescriptors();
                    for (int i = 0; i < visibleDescriptors.length; i++) {
                        ColumnDescriptor desc = visibleDescriptors[i];
                        int w = desc.getWidth();
                        if(w > 0) {
                            setColumnWidth(i, w);
                        } else if(w == 0) {
                            setWidthForFit(i);
                        }
                    }
                    if(isSaved) {
                        int idx = getRecentChangesColumnIdx();
                        if(idx > -1) {
                            int w = UIUtils.getColumnWidthInPixels(25, table);
                            setColumnWidth(getRecentChangesColumnIdx(), w);
                        }
                    }
                }

                if(isSaved) {
                    int seenIdx = getSeenColumnIdx();
                    table.getColumnModel().getColumn(seenIdx).setMaxWidth(28);
                    table.getColumnModel().getColumn(seenIdx).setPreferredWidth(28);
                }
            }

            private void setColumnWidth(int i, int w) {
                table.getColumnModel().getColumn(i).setMinWidth(10);
                table.getColumnModel().getColumn(i).setMaxWidth(10000);
                table.getColumnModel().getColumn(i).setPreferredWidth(w);
            }

            private void setWidthForFit(int i) {
                TableColumn c = table.getColumnModel().getColumn(i);
                Component comp = queryTableHeaderRenderer.getTableCellRendererComponent(table, c.getHeaderValue(), false, false, 0, i);
                if(comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    int w = label.getPreferredSize().width;
                    if(w > -1) {
                        setColumnWidth(i, w);
                    }
                }
            }

        };
        if(EventQueue.isDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);            
        }
    }

    private ColumnDescriptor[] getVisibleDescriptors() {
        List<ColumnDescriptor> visible = new LinkedList<ColumnDescriptor>();
        for (ColumnDescriptor d : descriptors) {
            if(d.isVisible()) {
                visible.add(d);
            }
        }
        return visible.toArray(new ColumnDescriptor[0]);
    }

    @Override
    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    @Override
    public void ancestorMoved(AncestorEvent event) { }

    @Override
    public void ancestorRemoved(AncestorEvent event) { }

    private void setModelProperties() {
        List<ColumnDescriptor> properties = new ArrayList<>(descriptors.length + (isSaved ? 2 : 0));
        int i = 0;
        for (; i < descriptors.length; i++) {
            ColumnDescriptor desc = descriptors[i];
            properties.add(desc);
        }
        if(isSaved) {
            properties.add(new RecentChangesDescriptor());
            properties.add(new SeenDescriptor());
        }

        // set visibility dependeing on persisted values
        Map<String, Integer> persistedColumnsMap = getPersistedColumnValues();
        if(persistedColumnsMap.size() > 0) {
            for (ColumnDescriptor cd : properties) {
                if(!cd.getName().equals(IssueNode.LABEL_NAME_SEEN)) { // always show seen, no matter if persisted or not
                    cd.setVisible(persistedColumnsMap.containsKey(cd.getName()));
                }
            }
        }
        descriptors = properties.toArray(new ColumnDescriptor[0]);
        tableModel.setProperties(descriptors);        
    }

    private Map<String, Integer> getPersistedColumnValues() {
        String columns = getColumns(repositoryId);
        String[] visibleColumns = columns.split(CONFIG_DELIMITER);                         // NOI18N
        if(visibleColumns.length <= 1) {
            return Collections.emptyMap();
        }
        Map<String, Integer> ret = new HashMap<String, Integer>();
        for (int i = 0; i < visibleColumns.length; i=i+2) {
            try {
                ret.put(visibleColumns[i], Integer.parseInt(visibleColumns[i + 1]));
            } catch (NumberFormatException nfe) {
                ret.put(visibleColumns[i], -1);
                LOG.log(Level.WARNING, visibleColumns[i], nfe);
            }
        }
        return ret;
    }

    private void setTableModel(IssueNode[] nodes) {
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int column = table.columnAtPoint(e.getPoint());
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (row == -1) return;
            row = sorter.modelIndex(row);
            if(MouseUtils.isDoubleClick(e)) {
                Action action = tableModel.getNodes()[row].getPreferredAction();
                if (action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            } else {

                // seen column
                if(column == getSeenColumnIdx()) {
                    final IssueNode in = (IssueNode) tableModel.getNodes()[row];
                    getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            IssueStatusProvider.Status status = in.getStatus();
                            in.setSeen(status != IssueStatusProvider.Status.SEEN);
                        }
                    });
                }
                // check for action
                CellAction[] actions = null;
                synchronized(cellActions) {
                    Cell cell = new Cell(row, column);
                    Set<CellAction> set = cellActions.get(cell);
                    actions = set != null ? set.toArray(new CellAction[0]) : null;
                }
                if(actions != null) {
                    for (CellAction cellAction : actions) {
                        cellAction.actionPerformed(null);
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\n') {                                     // NOI18N
            int row = table.getSelectedRow();
            if (row != -1) {
                row = sorter.modelIndex(row);
                Action action = tableModel.getNodes()[row].getPreferredAction();
                if (action.isEnabled()) {
                    action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == '\n') {                                     // NOI18N
            int row = table.getSelectedRow();
            if (row != -1) {
                // Hack for bug 4486444
                e.consume();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    public void addNode(final IssueNode node) {
        nodes.add(node);
        if(filter == null || filter.accept(node)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tableModel.insertNode(node);
                }
            });
        }
    }
    public void started() {
        nodes.clear();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueTable.this.setTableModel(new IssueNode[0]);
            }
        });
    }

    private static class SeenDescriptor extends ColumnDescriptor<Boolean> {
        public SeenDescriptor() {
            super(IssueNode.LABEL_NAME_SEEN, Boolean.class, "", NbBundle.getBundle(IssueTable.class).getString("CTL_Issue_Seen_Desc"), -1, true, true); // NOI18N
        }
    }

    private static class RecentChangesDescriptor extends ColumnDescriptor<String> {
        public RecentChangesDescriptor() {
            super(IssueNode.LABEL_RECENT_CHANGES, String.class, NbBundle.getBundle(IssueTable.class).getString("CTL_Issue_Recent"), NbBundle.getBundle(IssueTable.class).getString("CTL_Issue_Recent_Desc"), -1, true, true); // NOI18N
        }
    }

    private class StoreColumnsHandler implements Runnable {
        @Override
        public void run() {            
            TableColumnModel cm = table.getColumnModel();
            int count = cm.getColumnCount();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                if(!tableModel.getColumnId(i).equals(IssueNode.LABEL_NAME_SEEN)) {
                    sb.append(tableModel.getColumnId(i));
                    sb.append(CONFIG_DELIMITER);
                    sb.append(cm.getColumn(i).getWidth());
                    if(i < count - 1) {
                        sb.append(CONFIG_DELIMITER);
                    }
                }
            }
            storeColumns(repositoryId, sb.toString());
        }
    }

    private TableColumnModelListener tcml = new TableColumnModelListener() {
        @Override
        public void columnAdded(TableColumnModelEvent e) {}
        @Override
        public void columnRemoved(TableColumnModelEvent e) {}
        @Override
        public void columnMoved(TableColumnModelEvent e) {
            int from = e.getFromIndex();
            int to = e.getToIndex();
            if(from == to) {
                return;
            }
            table.getTableHeader().getColumnModel().getColumn(from).setModelIndex(from);
            table.getTableHeader().getColumnModel().getColumn(to).setModelIndex(to);
            tableModel.moveColumn(from, to);
        }
        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {}
        @Override
        public void columnMarginChanged(ChangeEvent e) {
            storeColumnsTask.schedule(1000);
        }
    };

    private static final String COLUMN_WIDTH_PREFIX  = "bugtracking.issuetable.columnwidth";  // NOI18N
    private static final String COLUMN_SORTING_PREFIX = "bugtracking.issuetable.columnsorting";  // NOI18N
    
    public Preferences getPreferences() {
        // legacy - use some public bugtracking type, 
        // to acces the previously (before 8.0) used preferences location
        return NbPreferences.forModule(BugtrackingSupport.class);
    }

    public void storeColumns(String key, String columns) {
        getPreferences().put(COLUMN_WIDTH_PREFIX + "." + key, columns); // NOI18N
    }

    public String getColumns(String key) {
        return getPreferences().get(COLUMN_WIDTH_PREFIX + "." + key, ""); // NOI18N
    }

    @Deprecated
    public int[] getColumnWidths(String key) {
        List<Integer> retval = new ArrayList<>();
        try {
            String[] keys = getPreferences().keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(COLUMN_WIDTH_PREFIX + "." + key + ".")) { // NOI18N
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));    // NOI18N
                    int value = getPreferences().getInt(k, -1);
                    retval.add(idx, value);
                    getPreferences().remove(k);
                }
            }
            int[] ret = new int[retval.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = retval.get(i);
            }
            return ret;
        } catch (NumberFormatException | BackingStoreException ex) {
            LOG.log(Level.INFO, null, ex);
            return new int[0];
        }
    }

    public void storeColumnSorting(String columnsKey, String sorting) {
        getPreferences().put(COLUMN_SORTING_PREFIX + "." + columnsKey, sorting); // NOI18N
    }

    public String getColumnSorting(String key) {
        return getPreferences().get(COLUMN_SORTING_PREFIX + "." + key, ""); // NOI18N
    }    
}

