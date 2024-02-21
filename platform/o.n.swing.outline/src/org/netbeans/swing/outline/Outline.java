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
package org.netbeans.swing.outline;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;

/** An Outline, or tree-table component.  Takes an instance of OutlineModel,
 * an interface which merges TreeModel and TableModel.
 * <p>
 * Simplest usage:  
 * <ol>
 * <li>Create a standard tree model for the tree node portion of the outline.</li>
 * <li>Implement RowModel.  RowModel is a subset of TableModel - it is passed
 * the value in column 0 of the Outline and a column index, and returns the 
 * value in the column in question.</li>
 * <li>Pass the TreeModel and the RowModel to <code>DefaultOutlineModel.createModel()</code>
 * </ol>
 * This will generate an instance of DefaultOutlineModel which will use the
 * TreeModel for the rows/tree column content, and use the RowModel to provide
 * the additional table columns.
 * <p>
 * It is also useful to provide an implementation of <code>RenderDataProvider</code>
 * to supply icons and affect text display of cells - this covers most of the 
 * needs for which it is necessary to write a custom cell renderer in JTable/JTree.
 * <p>
 * <b>Example usage:</b><br>
 * Assume FileTreeModel is a model which, given a root directory, will 
 * expose the files and folders underneath it.  We will implement a 
 * RowModel to expose the file size and date, and a RenderDataProvider which
 * will use a gray color for uneditable files and expose the full file path as
 * a tooltip.  Assume the class this is implemented in is a 
 * JPanel subclass or other Swing container.
 * <br>
 * XXX todo: clean up formatting &amp; edit for style
 * <pre>{@code
 * public void initComponents() {
 *   setLayout (new BorderLayout());
 *   TreeModel treeMdl = new FileTreeModel (someDirectory);
 *
 *   OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, 
 *       new FileRowModel(), true);
 *   outline = new Outline();
 *   outline.setRenderDataProvider(new FileDataProvider()); 
 *   outline.setRootVisible (true);
 *   outline.setModel (mdl);
 *   add (outline, BorderLayout.CENTER);
 * }
 *  private class FileRowModel implements RowModel {
 *     public Class getColumnClass(int column) {
 *          switch (column) {
 *              case 0 : return Date.class;
 *              case 1 : return Long.class;
 *              default : assert false;
 *          }
 *          return null;
 *      }
 *      
 *      public int getColumnCount() {
 *          return 2;
 *      }
 *      
 *      public String getColumnName(int column) {
 *          return column == 0 ? "Date" : "Size";
 *      }
 *      
 *      public Object getValueFor(Object node, int column) {
 *          File f = (File) node;
 *          switch (column) {
 *              case 0 : return new Date (f.lastModified());
 *              case 1 : return new Long (f.length());
 *              default : assert false;
 *          }
 *          return null;
 *      }
 *      
 *      public boolean isCellEditable(Object node, int column) {
 *          return false;
 *      }
 *      
 *      public void setValueFor(Object node, int column, Object value) {
 *          //do nothing, nothing is editable
 *      }
 *  }
 *  
 *  private class FileDataProvider implements RenderDataProvider {
 *      public java.awt.Color getBackground(Object o) {
 *          return null;
 *      }
 *      
 *      public String getDisplayName(Object o) {
 *          return ((File) o).getName();
 *      }
 *      
 *      public java.awt.Color getForeground(Object o) {
 *          File f = (File) o;
 *          if (!f.isDirectory() && !f.canWrite()) {
 *              return UIManager.getColor ("controlShadow");
 *          }
 *          return null;
 *      }
 *      
 *      public javax.swing.Icon getIcon(Object o) {
 *          return null;
 *      }
 *      
 *      public String getTooltipText(Object o) {
 *          return ((File) o).getAbsolutePath();
 *      }
 *      
 *      public boolean isHtmlDisplayName(Object o) {
 *          return false;
 *      }
 *   }
 * }</pre>
 *
 *
 * @author  Tim Boudreau
 */
public class Outline extends ETable {
    //XXX plenty of methods missing here - add/remove tree expansion listeners,
    //better path info/queries, etc.
    
    // Tooltips larger than this are screwed up.
    private static final int MAX_TOOLTIP_LENGTH = 1000;
    
    private boolean initialized = false;
    private Boolean cachedRootVisible = null;
    private RenderDataProvider renderDataProvider = null;
    private ComponentListener componentListener = null;
    private boolean selectionDisabled = false;
    private boolean rowHeightIsSet = false;
    private int selectedRow = -1;
    private int[] lastEditPosition;

    /** Creates a new instance of Outline */
    public Outline() {
        init();
    }
    
    public Outline(OutlineModel mdl) {
        super (mdl);
        init();
    }
    
    private void init() {
        initialized = true;
        setDefaultRenderer(Object.class, new DefaultOutlineCellRenderer());
        ActionMap am = getActionMap();
        //make rows expandable with left/rigt arrow keys
        Action a = am.get("selectNextColumn"); //NOI18N
        am.put("selectNextColumn", new ExpandAction(true, a)); //NOI18N
        a = am.get("selectPreviousColumn"); //NOI18N
        am.put("selectPreviousColumn", new ExpandAction(false, a)); //NOI18N
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (getSelectedRowCount() == 1) {
                    selectedRow = getSelectedRow();
                } else {
                    selectedRow = -1;
                }
            }
        });
    }
    
    /** Always returns the default renderer for Object.class for the tree column */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        int c = convertColumnIndexToModel(column);
        TableCellRenderer result;
        if (c == 0) {
            TableColumn tableColumn = getColumnModel().getColumn(column);
            TableCellRenderer renderer = tableColumn.getCellRenderer();
            if (renderer == null) {
                result = getDefaultRenderer(Object.class);
            } else {
                result = renderer;
            }
        } else {
            result = super.getCellRenderer(row, column);
        }
        return result;
    }
    
    /** Get the RenderDataProvider which is providing text, icons and tooltips
     * for items in the tree column.  The default property for this value is
     * null, in which case standard JTable/JTree object -> icon/string 
     * conventions are used */
    public RenderDataProvider getRenderDataProvider() {
        return renderDataProvider;
    }
    
    /** Set the RenderDataProvider which will provide text, icons and tooltips
     * for items in the tree column.  The default is null.  If null, 
     * the data displayed will be generated in the standard JTable/JTree way - 
     * calling <code>toString()</code> on objects in the tree model and 
     * using the look and feel's default tree folder and tree leaf icons.  */
    public void setRenderDataProvider (RenderDataProvider provider) {
        if (provider != renderDataProvider) {
            RenderDataProvider old = renderDataProvider;
            renderDataProvider = provider;
            firePropertyChange ("renderDataProvider", old, provider); //NOI18N
        }
    }
    
    /** Get the TreePathSupport object which manages path expansion for this
     * Outline. */
    TreePathSupport getTreePathSupport () {
        OutlineModel mdl = getOutlineModel();
        if (mdl != null) {
            return mdl.getTreePathSupport();
        } else {
            return null;
        }
    }
    
    /** Get the layout cache which manages layout data for the Outline.
     * <strong>Under no circumstances directly call the methods on the
     * layout cache which change the expanded state - such changes will not
     * be propagated into the table model, and will leave the model and
     * its layout in inconsistent states.  Any calls that affect expanded
     * state must go through <code>getTreePathSupport()</code>.</strong> */
    public final AbstractLayoutCache getLayoutCache () {
        OutlineModel mdl = getOutlineModel();
        if (mdl != null) {
            return mdl.getLayout();
        } else {
            return null;
        }
    }
    
    boolean isTreeColumnIndex (int column) {
        int c = convertColumnIndexToModel(column);
        return c == 0;
    }
    
    public boolean isVisible (TreePath path) {
        if (getTreePathSupport() != null) {
            return getTreePathSupport().isVisible(path);
        }
        return false;
    }
    
    /** Overridden to pass the fixed row height to the tree layout cache */
    @Override
    public void setRowHeight(int val) {
        rowHeightIsSet = true;
        super.setRowHeight(val);
        if (getLayoutCache() != null) {
            getLayoutCache().setRowHeight(val);
        }
    }
    
    /** Set whether or not the root is visible */
    public void setRootVisible (boolean val) {
        if (getOutlineModel() == null) {
            cachedRootVisible = val ? Boolean.TRUE : Boolean.FALSE;
        }
        if (val != isRootVisible()) {
            getLayoutCache().setRootVisible(val);
            if( getLayoutCache().getRowCount() > 0 ) {
                TreePath rootPath = getLayoutCache().getPathForRow(0);
                if( null != rootPath )
                    getLayoutCache().treeStructureChanged(new TreeModelEvent(this, rootPath));
            }
            sortAndFilter();
            firePropertyChange("rootVisible", !val, val); //NOI18N
        }
    }
    
    /** Is the tree root visible.  Default value is true. */
    public boolean isRootVisible() {
        if (getLayoutCache() == null) {
            return cachedRootVisible != null ? 
                cachedRootVisible.booleanValue() : true;
        } else {
            return getLayoutCache().isRootVisible();
        }
    }

    @Override
    public void setRowHeight (int row, int rowHeight) {
        Logger.getLogger (Outline.class.getName ()).warning ("Not supported yet."); // NOI18N
    }


    @Override
    protected TableColumn createColumn(int modelIndex) {
        return new OutlineColumn(modelIndex);
    }

    private JToolTip toolTip = null;

    @Override
    public String getToolTipText(MouseEvent event) {
        try {
            // Required to really get the tooltip text:
            putClientProperty("ComputingTooltip", Boolean.TRUE);

            toolTip = null;
            String tipText = null;
            Point p = event.getPoint();

            // Locate the renderer under the event location
            int hitColumnIndex = columnAtPoint(p);
            int hitRowIndex = rowAtPoint(p);

            if ((hitColumnIndex != -1) && (hitRowIndex != -1)) {
                //Outline tbl = (Outline) table;
                if (convertColumnIndexToModel(hitColumnIndex) == 0) {   // tree column index
                    // For tree column get the tooltip directly from the renderer data provider
                    RenderDataProvider rendata = getRenderDataProvider();
                    if (rendata != null) {
                        Object value = getValueAt(hitRowIndex, hitColumnIndex);
                        if (value != null) {
                            String toolT = rendata.getTooltipText(value);
                            if (toolT != null && (toolT = toolT.trim ()).length () > 0) {
                                tipText = toolT;
                            }
                        }
                    }
                }

                TableCellRenderer renderer = getCellRenderer(hitRowIndex, hitColumnIndex);
                Component component = prepareRenderer(renderer, hitRowIndex, hitColumnIndex);

                // Now have to see if the component is a JComponent before
                // getting the tip
                if (component instanceof JComponent) {
                    // Convert the event to the renderer's coordinate system
                    Rectangle cellRect = getCellRect(hitRowIndex, hitColumnIndex, false);
                    p.translate(-cellRect.x, -cellRect.y);
                    MouseEvent newEvent = new MouseEvent(component, event.getID(),
                                              event.getWhen(), event.getModifiers(),
                                              p.x, p.y,
                                              event.getXOnScreen(),
                                              event.getYOnScreen(),
                                              event.getClickCount(),
                                              event.isPopupTrigger(),
                                              MouseEvent.NOBUTTON);

                    if (tipText == null) {
                        tipText = ((JComponent)component).getToolTipText(newEvent);
                    }
                    toolTip = ((JComponent)component).createToolTip();
                }
            }

            // No tip from the renderer get our own tip
            if (tipText == null)
                tipText = getToolTipText();

            if (tipText != null) {
                tipText = tipText.trim();
                if (tipText.length() > MAX_TOOLTIP_LENGTH &&
                    !tipText.regionMatches(false, 0, "<html>", 0, 6)) {   // Do not cut HTML tooltips

                    tipText = tipText.substring(0, MAX_TOOLTIP_LENGTH) + "...";
                }
            }
            return tipText;
        } finally {
            putClientProperty("ComputingTooltip", Boolean.FALSE);
        }
        //return super.getToolTipText(event);
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip t = toolTip;
        toolTip = null;
        if (t != null) {
            t.addMouseMotionListener(new MouseMotionAdapter() { // #233642

                boolean initialized = false;

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (!initialized) {
                        initialized = true; // ignore the first event
                    } else {
                        // hide the tooltip if mouse moves over it
                        ToolTipManager.sharedInstance().mousePressed(e);
                    }
                }
            });
            return t;
        } else {
            return super.createToolTip();
        }
    }
    
    private transient Map<TreePath, RowMapping> tempSortMap = null;
    private final transient Object tempSortMapLock = new Object();

    /**
     * Sorts the rows of the tree table.
     */
    @Override
    protected void sortAndFilter() {
        TableColumnModel tcm = getColumnModel();
        if (tcm instanceof ETableColumnModel) {
            ETableColumnModel etcm = (ETableColumnModel) tcm;
            Comparator<RowMapping> c = etcm.getComparator();
            if (c != null) {
                TableModel model = getModel();
                int noRows = model.getRowCount();
                //System.err.println("sortAndFilter: Number of rows = "+noRows);
                List<RowMapping> rows = new ArrayList<RowMapping>();
                synchronized (tempSortMapLock) {
                    if (tempSortMap != null) {
                        return ; // Sorting right now
                    }
                    Map<TreePath, RowMapping> tsm = new HashMap<TreePath, RowMapping>();
                    for (int i = 0; i < noRows; i++) {
                        if (acceptByQuickFilter(model, i)) {
                            TreePath tp = getLayoutCache().getPathForRow(i);
                            RowMapping rm = new RowMapping(i, model, this);
                            //System.err.println("               RowMapping("+i+") = "+rm);
                            tsm.put(tp, rm);
                            rows.add(rm);
                        }
                    }
                    tempSortMap = tsm;
                    rows.sort(c);
                    tempSortMap = null;
                }
                int [] res = new int[rows.size()];
                int [] invRes = new int[noRows]; // carefull - this one is bigger!
                for (int i = 0; i < res.length; i++) {
                    RowMapping rm = rows.get(i);
                    int rmi = rm.getModelRowIndex();
                    res[i] = rmi;
                    invRes[rmi] = i;
                }
                int[] oldRes = sortingPermutation;
                int[] oldInvRes = inverseSortingPermutation;
                //System.err.println(" SETTING PERMUTATION = "+Arrays.toString(res));
                //System.err.println(" SETTING INV.PERMUT. = "+Arrays.toString(invRes));
                sortingPermutation = res;
                inverseSortingPermutation = invRes;
                //adjustSelectedRows(oldRes, oldInvRes, res, invRes);
            }
        }
    }
    
    /**
     * An Outline implementation of table column.
     */
    protected class OutlineColumn extends ETableColumn {

        /**
         * Create a new outline column
         * @param modelIndex The column's model index
         */
        public OutlineColumn(int modelIndex) {
            super(modelIndex, Outline.this);
        }

        @Override
        protected Comparator<RowMapping> getRowComparator(int column, boolean ascending) {
            return new OutlineRowComparator(column, ascending);
        }
        @Override
        public boolean isHidingAllowed() {
            return getModelIndex() != 0;
        }
        @Override
        public boolean isSortingAllowed() {
            return true;
        }
        /**
         * Comparator used for sorting the rows according to value in
         * a given column. Operates on the RowMapping objects.
         */
        private class OutlineRowComparator extends RowComparator {
            private boolean ascending = true;
            public OutlineRowComparator(int column, boolean ascending) {
                super(column);
                this.ascending = ascending;
            }
            @Override
            @SuppressWarnings("unchecked")
            public int compare(RowMapping rm1, RowMapping rm2) {
                int index1 = rm1.getModelRowIndex();
                int index2 = rm2.getModelRowIndex();
                if (index1 == index2) {
                    return 0;
                }
                TreePath tp1 = getLayoutCache().getPathForRow(index1);
                TreePath tp2 = getLayoutCache().getPathForRow(index2);
                if (tp1 == null) {
                    if (tp2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else if (tp2 == null) {
                    return 1;
                }
                if (tp1.isDescendant(tp2)) {
                    return -1;
                }
                if (tp2.isDescendant(tp1)) {
                    return 1;
                }
                boolean tp1Changed = false;
                boolean tp2Changed = false;
                TreePath parent1 = tp1.getParentPath();
                TreePath parent2 = tp2.getParentPath();
                if (parent1 != null && parent2 != null && parent1.equals(parent2) &&
                        getOutlineModel().isLeaf(tp1.getLastPathComponent()) &&
                        getOutlineModel().isLeaf(tp2.getLastPathComponent())) {
                    return ascending ? super.compare(rm1, rm2) : - super.compare(rm1, rm2);
                }
                while (tp1.getPathCount() < tp2.getPathCount()) {
                    tp2 = tp2.getParentPath();
                    tp2Changed = true;
                }
                while (tp1.getPathCount() > tp2.getPathCount()) {
                    tp1 = tp1.getParentPath();
                    tp1Changed = true;
                }
                parent1 = tp1.getParentPath();
                parent2 = tp2.getParentPath();
                while (parent1 != null && parent2 != null && !parent1.equals(parent2)) {
                    tp1 = parent1;
                    tp2 = parent2;
                    parent1 = tp1.getParentPath();
                    parent2 = tp2.getParentPath();
                    tp1Changed = true;
                    tp2Changed = true;
                }
                if (tp1Changed || tp2Changed) {
                    return compare(tempSortMap.get(tp1), tempSortMap.get(tp2));
                } else {
                    return ascending ? super.compare(rm1, rm2) : - super.compare(rm1, rm2);
                }
            }
        }
    }
    
    /** Overridden to throw an exception if the passed model is not an instance
     * of <code>OutlineModel</code> (with the exception of calls from the 
     * superclass constructor) */
    @Override
    public void setModel (TableModel mdl) {
        if (initialized && (!(mdl instanceof OutlineModel))) {
            throw new IllegalArgumentException (
                "Table model for an Outline must be an instance of " +
                "OutlineModel"); //NOI18N
        }
        if (mdl instanceof OutlineModel) {
            AbstractLayoutCache layout = ((OutlineModel) mdl).getLayout();
            if (cachedRootVisible != null) {
                
                layout.setRootVisible(
                    cachedRootVisible.booleanValue());
                
            }
            
            layout.setRowHeight(getRowHeight());
            
            if (((OutlineModel) mdl).isLargeModel()) {
                addComponentListener (getComponentListener());
                layout.setNodeDimensions(new ND());
            } else {
                if (componentListener != null) {
                    removeComponentListener (componentListener);
                    componentListener = null;
                }
            }
        }
        
        super.setModel(mdl);
    }
    
    /** Convenience getter for the <code>TableModel</code> as an instance of
     * OutlineModel.  If no OutlineModel has been set, returns null. */
    public OutlineModel getOutlineModel() {
        TableModel mdl = getModel();
        if (mdl instanceof OutlineModel) {
            return (OutlineModel) getModel();
        } else {
            return null;
        }
    }
    
    /** Expand a tree path */
    public void expandPath (TreePath path) {
        getTreePathSupport().expandPath (path);
    }
    
    public boolean isExpanded (TreePath path) {
        return getTreePathSupport().isExpanded(path);
    }

    /**
     * Collapse the given tree path.
     * @param path The tree path to collapse.
     */
    public void collapsePath (TreePath path) {
        getTreePathSupport().collapsePath (path);
    }

    /**
     * Get the UI bounds of the given tree path.
     * @param path The tree path to get the bounds for.
     * @return The bounds.
     */
    public Rectangle getPathBounds(TreePath path) {
        Insets i = getInsets();
        Rectangle bounds = getLayoutCache().getBounds(path, null);

        if(bounds != null && i != null) {
            bounds.x += i.left;
            bounds.y += i.top;
        }
        return bounds;
    }   

    /**
     * Find the tree path that is closest to the given position.
     * @param x The X coordinate of the position
     * @param y The Y coordinate of the position
     * @return The closest tree path
     */
    public TreePath getClosestPathForLocation(int x, int y) {
        Insets i = getInsets();
        TreePath tp;
        if (i != null) {
            tp = getLayoutCache().getPathClosestTo(x - i.left, y - i.top);
        } else {
            tp = getLayoutCache().getPathClosestTo(x,y);
        }
        int row = getLayoutCache().getRowForPath(tp);
        // The UI row needs to be converted to the model row.
        row = convertRowIndexToModel(row);
        // Now we can get the correct path from the model row:
        tp = getLayoutCache().getPathForRow(row);
        return tp;
    }
    
    private KeyStroke lastProcessedKeyStroke;
    
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                        int condition, boolean pressed) {
        lastProcessedKeyStroke = ks;
        try {
            return super.processKeyBinding(ks, e, condition, pressed);
        } finally {
            lastProcessedKeyStroke = null;
        }
    }
    
    @Override
    public boolean editCellAt (int row, int column, EventObject e) {
        //If it was on column 0, it may be a request to expand a tree
        //node - check for that first.
        boolean isTreeColumn = isTreeColumnIndex(column);
        if (isTreeColumn && e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            TreePath path = getLayoutCache().getPathForRow(convertRowIndexToModel(row));
            if (path != null && !getOutlineModel().isLeaf(path.getLastPathComponent())) {
                int handleWidth = DefaultOutlineCellRenderer.getExpansionHandleWidth();
                Insets ins = getInsets();
                int nd = path.getPathCount() - (isRootVisible() ? 1 : 2);
                if (nd < 0) {
                    nd = 0;
                }
                int handleStart = ins.left + (nd * DefaultOutlineCellRenderer.getNestingWidth());
                int handleEnd = ins.left + handleStart + handleWidth;
                // Translate 'x' to position of column if non-0:
                int columnStart = getCellRect(row, column, false).x;
                handleStart += columnStart;
                handleEnd += columnStart;

                TableColumn tableColumn = getColumnModel().getColumn(column);
                TableCellEditor columnCellEditor = tableColumn.getCellEditor();
                if ((me.getX() > ins.left && me.getX() >= handleStart && me.getX() <= handleEnd) ||
                    (me.getClickCount() > 1 && columnCellEditor == null)) {

                    boolean expanded = getLayoutCache().isExpanded(path);
                    //me.consume();  - has no effect!
                    //System.err.println("  event consumed.");
                    if (!expanded) {
                        getTreePathSupport().expandPath(path);
                        
                        Object ourObject = path.getLastPathComponent();
                        int cCount = getOutlineModel().getChildCount(ourObject);
                        if (cCount > 0) {
                            int lastRow = row;
                            for (int i = 0; i < cCount; i++) {
                                Object child = getOutlineModel().getChild(ourObject, i);
                                TreePath childPath = path.pathByAddingChild(child);
                                int childRow = getLayoutCache().getRowForPath(childPath);
                                childRow = convertRowIndexToView(childRow);
                                if (childRow > lastRow) {
                                    lastRow = childRow;
                                }
                            }
                            int firstRow = row;
                            Rectangle rectLast = getCellRect(lastRow, 0, true);
                            Rectangle rectFirst = getCellRect(firstRow, 0, true);
                            Rectangle rectFull = new Rectangle(
                                    rectFirst.x,
                                    rectFirst.y,
                                    rectLast.x + rectLast.width - rectFirst.x,
                                    rectLast.y + rectLast.height - rectFirst.y);
                            scrollRectToVisible(rectFull);
                        }
                        
                    } else {
                        getTreePathSupport().collapsePath(path);
                    }
                    selectionDisabled = true;
                    return false;
                }
            }
            // It may be a request to check/uncheck a check-box
            if (checkAt(row, column, me)) {
                return false;
            }
        } else if (isTreeColumn && e instanceof ActionEvent) {
            if (((ActionEvent) e).getModifiers() == 0 &&
                lastProcessedKeyStroke != null) {
                
                if (lastProcessedKeyStroke.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (checkAt(row, column, null)) {
                        return false;
                    }
                }
            }
        }
            
        boolean res = false;
        if (!isTreeColumn || e instanceof MouseEvent && row >= 0 && isEditEvent(row, column, (MouseEvent) e)) {
            res = super.editCellAt(row, column, e);
        }
        if( res && isTreeColumn && row >= 0 && null != getEditorComponent() ) {
            configureTreeCellEditor(getEditorComponent(), row, column);
        }
        if (e == null && !res && isTreeColumn) {
            // Handle SPACE
            checkAt(row, column, null);
        }
        return res;
    }

    private boolean isEditEvent(int row, int column, MouseEvent me) {
        if (me.getClickCount() > 1) {
            return true;
        }
        boolean noModifiers = me.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK;
        if (lastEditPosition != null && selectedRow == row && noModifiers &&
            lastEditPosition[0] == row && lastEditPosition[1] == column) {

            int handleWidth = DefaultOutlineCellRenderer.getExpansionHandleWidth();
            Insets ins = getInsets();
            TreePath path = getLayoutCache().getPathForRow(convertRowIndexToModel(row));
            int nd = path.getPathCount() - (isRootVisible() ? 1 : 2);
            if (nd < 0) {
                nd = 0;
            }
            int handleStart = ins.left + (nd * DefaultOutlineCellRenderer.getNestingWidth());
            int handleEnd = ins.left + handleStart + handleWidth;
            // Translate 'x' to position of column if non-0:
            int columnStart = getCellRect(row, column, false).x;
            handleStart += columnStart;
            handleEnd += columnStart;
            if (me.getX() >= handleEnd) {
                lastEditPosition = null;
                return true;
            }
        }
        lastEditPosition = new int[] { row, column };
        return false;
    }

    /**
     * Perform a selection/deselection of a check box on the given row and column,
     * if a check box exists on the given position.
     * @param row The row of the check box
     * @param column The column of the check box
     * @param me The mouse event that performs the check, or <code>null</code>.
     * @return <code>true</code> if a {@link CheckRenderDataProvider} is found
     *         on the given row and column, is checkable and enabled and the
     *         mouse event is either <code>null</code> or upon the check-box
     *         location. Returns <code>false</code> otherwise.
     * @since 1.25
     */
    protected final boolean checkAt(int row, int column, MouseEvent me) {
        RenderDataProvider render = getRenderDataProvider();
        TableCellRenderer tcr = getDefaultRenderer(Object.class);
        if (render instanceof CheckRenderDataProvider && tcr instanceof DefaultOutlineCellRenderer) {
            CheckRenderDataProvider crender = (CheckRenderDataProvider) render;
            DefaultOutlineCellRenderer ocr = (DefaultOutlineCellRenderer) tcr;
            Object value = getValueAt(row, column);
            if (value != null && crender.isCheckable(value) && crender.isCheckEnabled(value)) {
                boolean chBoxPosition;
                if (me == null) {
                    chBoxPosition = true;
                } else {
                    int handleWidth = DefaultOutlineCellRenderer.getExpansionHandleWidth();
                    int chWidth = ocr.getTheCheckBoxWidth();
                    Insets ins = getInsets();
                    TreePath path = getLayoutCache().getPathForRow(convertRowIndexToModel(row));
                    int nd = path.getPathCount() - (isRootVisible() ? 1 : 2);
                    if (nd < 0) {
                        nd = 0;
                    }
                    int chStart = ins.left + (nd * DefaultOutlineCellRenderer.getNestingWidth()) + handleWidth;
                    int chEnd = chStart + chWidth;
                    //TODO: Translate x/y to position of column if non-0

                    chBoxPosition = (me.getX() > ins.left && me.getX() >= chStart && me.getX() <= chEnd);
                }
                if (chBoxPosition) {
                    Boolean selected = crender.isSelected(value);
                    if (selected == null || Boolean.TRUE.equals(selected)) {
                        crender.setSelected(value, Boolean.FALSE);
                    } else {
                        crender.setSelected(value, Boolean.TRUE);
                    }
                    Rectangle r = getCellRect(row, column, true);
                    repaint (r.x, r.y, r.width, r.height);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Configure the cell editor.
     * This method allows to override the configuration of cell editor when cell editing is initiated.
     * 
     * @param editor The editor component
     * @param row Editor's row
     * @param column Editor's column
     */
    protected void configureTreeCellEditor( Component editor, int row, int column ) {
        if( !(editor instanceof JComponent) ) {
            return;
        }
        TreeCellEditorBorder b = new TreeCellEditorBorder();
        TreePath path = getLayoutCache().getPathForRow(convertRowIndexToModel(row));
        Object o = getValueAt(row, column);
        RenderDataProvider rdp = getRenderDataProvider();
        TableCellRenderer tcr = getDefaultRenderer(Object.class);
        if (rdp instanceof CheckRenderDataProvider && tcr instanceof DefaultOutlineCellRenderer) {
            CheckRenderDataProvider crender = (CheckRenderDataProvider) rdp;
            DefaultOutlineCellRenderer ocr = (DefaultOutlineCellRenderer) tcr;
            Object value = getValueAt(row, column);
            if (value != null && crender.isCheckable(value) && crender.isCheckEnabled(value)) {
                b.checkWidth = ocr.getTheCheckBoxWidth();
                b.checkBox = ocr.setUpCheckBox(crender, value, ocr.createCheckBox());
            }
        }
        b.icon = rdp.getIcon(o);
        b.nestingDepth = Math.max( 0, path.getPathCount() - (isRootVisible() ? 1 : 2) );
        b.isLeaf = getOutlineModel().isLeaf(o);
        b.isExpanded = getLayoutCache().isExpanded(path);
        
        ((JComponent)editor).setBorder(b);
    }
    
    /** Computes row height ...
     */
    @Override
    public void addNotify () {
        super.addNotify ();
        if (!rowHeightIsSet) {
            calcRowHeight();
        }
    }

    /** Calculate the height of rows based on the current font. */
    private void calcRowHeight() {
        //Users of themes can set an explicit row height, so check for it
        Integer i = (Integer) UIManager.get("netbeans.outline.rowHeight"); //NOI18N
        
        int rHeight = 20;
        if (i != null) {
            rHeight = i.intValue();
        } else {
            //Derive a row height to accomodate the font and expando icon
            Font f = getFont();
            FontMetrics fm = getFontMetrics(f);
            int h = Math.max (fm.getHeight () + fm.getMaxDescent (),
                DefaultOutlineCellRenderer.getExpansionHandleHeight ());
            rHeight = Math.max (rHeight, h) + 2; // XXX: two pixels for cell's insets
        }
        //Set row height.  If displayable, this will generate a new call
        //to paint()
        setRowHeight(rHeight);
    }    
    
    @Override
    public void tableChanged(TableModelEvent e) {
//        System.err.println("Table got tableChanged " + e);
        super.tableChanged(e);
//        System.err.println("row count is " + getRowCount());
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if (selectionDisabled) {
            //selectionDisabled = false;
            //System.err.println("\nSelection DISABLED.");
            return ;
        }
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        switch(e.getID()) {
          case MouseEvent.MOUSE_PRESSED:
              selectionDisabled = false;
              break;
          case MouseEvent.MOUSE_RELEASED:
              selectionDisabled = false;
              break;
          case MouseEvent.MOUSE_CLICKED:
              selectionDisabled = false;
              break;
          case MouseEvent.MOUSE_ENTERED:
              selectionDisabled = false;
              break;
          case MouseEvent.MOUSE_EXITED:
              selectionDisabled = false;
              break;
          case MouseEvent.MOUSE_MOVED:
              break;
          case MouseEvent.MOUSE_DRAGGED:
              if (selectionDisabled) {
                  //System.err.println("\nDrag DISABLED.");
                  return ;
              }
              break;
          case MouseEvent.MOUSE_WHEEL:
              break;
        }
        super.processMouseEvent(e);
    }



    /** Create a component listener to handle size changes if the table model
     * is large-model */
    private ComponentListener getComponentListener() {
        if (componentListener == null) {
            componentListener = new SizeManager();
        }
        return componentListener;
    }
    
    private JScrollPane getScrollPane() {
        JScrollPane result = null;
        if (getParent() instanceof JViewport) {
            if (((JViewport) getParent()).getParent() instanceof JScrollPane) {
                result = (JScrollPane) ((JViewport) getParent()).getParent();
            }
        }
        return result;
    }
    
    private void change() {
        revalidate();
        repaint();
    }
    
    private class ND extends AbstractLayoutCache.NodeDimensions {
        
        @Override
        public Rectangle getNodeDimensions(Object value, int row, int depth, 
            boolean expanded, Rectangle bounds) {
                int wid = Outline.this.getColumnModel().getColumn(0).getPreferredWidth();
                bounds.setBounds (0, row * getRowHeight(), wid, getRowHeight());
                return bounds;
        }
        
    }
    
    
    /** A component listener.  If we're a large model table, we need
     * to inform the FixedHeightLayoutCache when the size changes, so it
     * can update its mapping of visible nodes */
    private class SizeManager extends ComponentAdapter implements ActionListener {
	protected Timer timer = null;
	protected JScrollBar scrollBar = null;
        
        @Override
        public void componentMoved(ComponentEvent e) {
	    if(timer == null) {
		JScrollPane   scrollPane = getScrollPane();

		if(scrollPane == null) {
		    change();
                } else {
		    scrollBar = scrollPane.getVerticalScrollBar();
		    if(scrollBar == null || 
			!scrollBar.getValueIsAdjusting()) {
			// Try the horizontal scrollbar.
			if((scrollBar = scrollPane.getHorizontalScrollBar())
			    != null && scrollBar.getValueIsAdjusting()) {
                                
			    startTimer();
                        } else {
			    change();
                        }
		    } else {
			startTimer();
                    }
		}
	    }
        }
        
	protected void startTimer() {
	    if(timer == null) {
		timer = new Timer(200, this);
		timer.setRepeats(true);
	    }
	    timer.start();
	}        
        
        @Override
	public void actionPerformed(ActionEvent ae) {
	    if(scrollBar == null || !scrollBar.getValueIsAdjusting()) {
		if(timer != null)
		    timer.stop();
		change();
		timer = null;
		scrollBar = null;
	    }
	}        
        
        @Override
        public void componentHidden(ComponentEvent e) {
        }
        
        @Override
        public void componentResized(ComponentEvent e) {
        }
        
        @Override
        public void componentShown(ComponentEvent e) {
        }
    }

    private static class TreeCellEditorBorder implements Border {
        private Insets insets = new Insets(0,0,0,0);
        private boolean isLeaf;
        private boolean isExpanded;
        private Icon icon;
        private int nestingDepth;
        private final int ICON_TEXT_GAP = new JLabel().getIconTextGap();
        private int checkWidth;
        private JCheckBox checkBox;
        
        @Override
        public Insets getBorderInsets(Component c) {
            insets.left = (nestingDepth *
                DefaultOutlineCellRenderer.getNestingWidth())
                +DefaultOutlineCellRenderer.getExpansionHandleWidth()+1;
            insets.left += checkWidth + ((icon != null) ? icon.getIconWidth() + ICON_TEXT_GAP : 0);
            insets.top = 1;
            insets.right = 1;
            insets.bottom = 1;
            return insets;
        }
        
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
        
        @Override
        public void paintBorder(Component c, java.awt.Graphics g, int x, int y, int width, int height) {
            int iconY;
            int iconX = nestingDepth * DefaultOutlineCellRenderer.getNestingWidth();
            if( !isLeaf ) {
                Icon expIcon = isExpanded
                    ? DefaultOutlineCellRenderer.getExpandedIcon() 
                    : DefaultOutlineCellRenderer.getCollapsedIcon();
                if (expIcon.getIconHeight() < height) {
                    iconY = (height / 2) - (expIcon.getIconHeight() / 2);
                } else {
                    iconY = 0;
                }
                expIcon.paintIcon(c, g, iconX, iconY);
            }
            iconX += DefaultOutlineCellRenderer.getExpansionHandleWidth() + 1;
            
            if (null != checkBox) {
                java.awt.Graphics chbg = g.create(iconX, y, checkWidth, height);
                checkBox.paint(chbg);
                chbg.dispose();
            }
            iconX += checkWidth;

            if( null != icon ) {
                if (icon.getIconHeight() < height) {
                    iconY = (height / 2) - (icon.getIconHeight() / 2);
                } else {
                    iconY = 0;
                }
                icon.paintIcon(c, g, iconX, iconY);
            }
        }
    }
    
    private class ExpandAction extends AbstractAction {
        private boolean expand;
        private Action origAction;
        public ExpandAction( boolean expand, Action orig ) {
            this.expand = expand;
            this.origAction = orig;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if( getSelectedRowCount() == 1 && isTreeColumnIndex (getSelectedColumn()) ) {
                TreePath selPath = getLayoutCache().getPathForRow(convertRowIndexToModel (getSelectedRow ()));
                if( null != selPath 
                        && !getOutlineModel().isLeaf(selPath.getLastPathComponent()) ) {
                    boolean expanded = getLayoutCache().isExpanded(selPath);
                    if( expanded && !expand ) {
                        collapsePath(selPath);
                        return;
                    } else if( !expanded && expand ) {
                        expandPath(selPath);
                        return;
                    } else if (expanded && expand && getOutlineModel().getChildCount(selPath.getLastPathComponent()) > 0) {
                        int row = getSelectedRow() + 1;
                        if (row < getRowCount()) {
                            selectCell(row, getSelectedColumn());
                            return ;
                        }
                    } else if (!expanded && !expand) {
                        TreePath parentPath = selPath.getParentPath();
                        if (parentPath != null) {
                            int row = convertRowIndexToView(getLayoutCache().getRowForPath(parentPath));
                            selectCell(row, getSelectedColumn());
                            return ;
                        }
                    }
                } else if( null != selPath 
                           && getOutlineModel().isLeaf(selPath.getLastPathComponent()) ) {
                    if (!expand) {
                        TreePath parentPath = selPath.getParentPath();
                        if (parentPath != null) {
                            int row = convertRowIndexToView(getLayoutCache().getRowForPath(parentPath));
                            selectCell(row, getSelectedColumn());
                            return ;
                        }
                    }
                }

            }
            if( null != origAction )
                origAction.actionPerformed(e);
        }
        
        private void selectCell(int row, int col) {
            changeSelection(row, col, false, false);
            scrollRectToVisible(getCellRect(row, col, false));
        }
    }
}
