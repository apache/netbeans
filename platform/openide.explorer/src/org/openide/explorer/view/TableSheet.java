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
package org.openide.explorer.view;

import org.openide.explorer.propertysheet.*;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.awt.*;
import java.awt.event.MouseEvent;

import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.openide.util.NbBundle;


/** Table view of node properties. Table header displays property display names and each cell
* contains (<code>PropertyPanel</code>) for displaying/editting of properties. Each property
* row belongs to one node.
*
* @author Jan Rojcek
*/
class TableSheet extends JScrollPane {
    /** table */
    protected transient JTable table;

    /** model */
    private transient NodeTableModel tableModel;
    
    private transient TableSheetCell tableCell;

    /** Create table view with default table model.
     */
    public TableSheet() {
        tableModel = new NodeTableModel();
        initializeView();
    }

    /** Create table view with users table model.
     */
    public TableSheet(NodeTableModel tableModel) {
        this.tableModel = tableModel;
        initializeView();
    }

    private void initializeView() {
        table = createTable();
        initializeTable();

        setViewportView(table);

        // do not care about focus
        setRequestFocusEnabled(false);

        table.getAccessibleContext().setAccessibleName(
            NbBundle.getMessage(TableSheet.class, "ACS_TableSheet")
        );
        table.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(TableSheet.class, "ACSD_TableSheet")
        );
    }

    /** Set rows.
     * @param props rows
     */
    public void setNodes(Node[] nodes) {
        tableModel.setNodes(nodes);
    }

    /** Set columns.
     * @param nodes columns
     */
    public void setProperties(Property[] props) {
        tableModel.setProperties(props);
    }

    /** Sets resize mode of table.
     *
     * @param mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final void setAutoResizeMode(int mode) {
        table.setAutoResizeMode(mode);
    }

    /** Sets resize mode of table.
     *
     * @param mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final int getAutoResizeMode() {
        return table.getAutoResizeMode();
    }

    /** Sets preferred width of table column
     * @param index column index
     * @param width preferred column width
     */
    public final void setColumnPreferredWidth(int index, int width) {
        table.getColumnModel().getColumn(index).setPreferredWidth(width);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());
    }

    /** Gets preferred width of table column
     * @param index column index
     * @param width preferred column width
     */
    public final int getColumnPreferredWidth(int index) {
        return table.getColumnModel().getColumn(index).getPreferredWidth();
    }

    /** Allows to subclasses provide its own table.
     * @param tm node table model
     * @return table which will be placed into scroll pane
     */
    JTable createTable() {
        return new JTable();
    }

    /** Allows to subclasses initialize table
     * @param t
     */
    private void initializeTable() {
        table.setModel(tableModel);

        tableCell = new TableSheetCell(tableModel);
        table.setDefaultRenderer(Node.Property.class, tableCell);
        table.setDefaultEditor(Node.Property.class, tableCell);
        table.getTableHeader().setDefaultRenderer(tableCell);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        if (UIManager.getColor("Panel.background") != null) { // NOI18N
            table.setBackground(UIManager.getColor("Panel.background")); // NOI18N
            table.setSelectionBackground(UIManager.getColor("Panel.background")); // NOI18N
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(TableSheet.class, key);
    }
    
    @Override
    public void updateUI() {
        super.updateUI();
        if( null != tableCell )
            tableCell.updateUI();
    }

    /** Synchronized table view with other view (TreeView, ListView). Two views (scroll panes)
     * have only one vertical scroll bar. Right view is allways table view, left view could be any
     * scroll pane. Use ControlledTableView.compoundScrollPane() to get compound view.
     */
    static class ControlledTableView extends TableSheet {
        /** Scroll pane which controls vertical scroll bar */
        JScrollPane controllingView;

        /** Table like header of controlling view */
        Component header;

        /** Compound scroll pane used in MouseDragHandler */
        JPanel compoundScrollPane;

        /** Creates controlled scroll pane with <code>contView</code> on the left, table view
         * on the right
         */
        ControlledTableView(JScrollPane contrView) {
            super();
            this.controllingView = contrView;
            initializeView();
        }

        /** Creates controlled scroll pane with <code>contView</code> on the left, table view
         * on the right.
         */
        ControlledTableView(JScrollPane contrView, NodeTableModel ntm) {
            super(ntm);
            this.controllingView = contrView;
            initializeView();
        }

        /** Validate root is outer scroll pane. */
        @Override
        public boolean isValidateRoot() {
            return false;
        }

        /** initialize view
         */
        private void initializeView() {
            // adjustment of controlling view
            Component comp = controllingView.getViewport().getView();
            controllingView.setViewportView(comp);

            if (UIManager.getColor("Table.background") != null) { // NOI18N
                getViewport().setBackground(UIManager.getColor("Table.background")); // NOI18N
            }

            // both views share one vertical scrollbar
            setVerticalScrollBar(controllingView.getVerticalScrollBar());

            ScrollPaneLayout spl = new EnablingScrollPaneLayout(controllingView);
            setLayout(spl);
            spl.syncWithScrollPane(this);

            spl = new EnablingScrollPaneLayout(this);
            controllingView.setLayout(spl);
            spl.syncWithScrollPane(controllingView);

            table.setBorder(null);

            // table like header
            header = new JTable().getTableHeader().getDefaultRenderer().getTableCellRendererComponent(
                    null, " ", false, false, 0, 0
                ); // NOI18N

            MouseInputListener mouseHandler = new MouseDragHandler();
            header.addMouseListener(mouseHandler);
            header.addMouseMotionListener(mouseHandler);
        }

        /** Overriden to return table with controlled height
         * @param tm table model
         * @return table
         */
        @Override
        JTable createTable() {
            return new ATable();
        }

        JTable getTable() {
            return table;
        }

        /** Overriden because I can't set border to null by calling setBorder(null).
         * @param border
         */
        @Override
        public void setBorder(Border border) {
            super.setBorder(null);
        }

        /** Is used to synchronize table row height with left view.
         */
        void setRowHeight(int h) {
            table.setRowHeight(h);
            getVerticalScrollBar().setUnitIncrement(h);
        }

        /** Sets text of table like header above left scroll pane.
         */
        void setHeaderText(String text) {
            if (header instanceof JLabel) {
                ((JLabel) header).setText(text);
            }
        }

        /** Sets preferred size of left scroll pane.
         */
        void setControllingViewWidth(int width) {
            controllingView.setPreferredSize(new Dimension(width, 0));
        }

        /** Gets preferred size of left scroll pane.
         */
        int getControllingViewWidth() {
            return controllingView.getPreferredSize().width;
        }

        /*
                public void setPreferredSize(Dimension prefSize) {
                    table.setPreferredScrollableViewportSize(prefSize);
                }
        */

        /** Returns component which contains two synchronized scroll panes. Above left one is
         * placed table like header.
         */
        JComponent compoundScrollPane() {
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.add(header, BorderLayout.NORTH);
            leftPanel.add(controllingView, BorderLayout.CENTER);

            compoundScrollPane = new CompoundScrollPane();
            compoundScrollPane.setLayout(new BorderLayout());
            compoundScrollPane.add(leftPanel, BorderLayout.CENTER);
            compoundScrollPane.add(this, BorderLayout.EAST);

            return compoundScrollPane;
        }

        private class ATable extends JTable {
            private boolean trytorevalidate = true;

            public ATable() {
                // fix for JTable bug - JTable consumes Esc key which is wrong
                // after JDK bug 4624483 is fixed this workaround can
                // be removed
                getActionMap().put("cancel", new OurCancelEditingAction()); // NOI18N
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                Dimension pref = super.getPreferredScrollableViewportSize();

                if ((this.getAutoResizeMode() != JTable.AUTO_RESIZE_OFF) && (getParent() != null)) {
                    Insets insets = getParent().getInsets();
                    Dimension size = getParent().getSize();
                    pref.height = size.height - insets.top - insets.bottom;
                }

                return pref;
            }

            /** Try to revalidate once again because we want table to have
             * width that it asked for.
             */
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, width, height);

                if (this.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
                    return;
                }

                if (trytorevalidate && (width != getPreferredScrollableViewportSize().width)) {
                    trytorevalidate = false;
                    compoundScrollPane.validate();
                    trytorevalidate = true;
                }
            }

            private class OurCancelEditingAction extends AbstractAction {
                OurCancelEditingAction() {
                }

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    JTable table = (JTable) e.getSource();
                    table.removeEditor();
                }

                @Override
                public boolean isEnabled() {
                    return ATable.this.isEditing();
                }
            }
        }

        private class MouseDragHandler extends MouseInputAdapter {
            boolean dragging = false;
            int lastMouseX;

            MouseDragHandler() {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                dragging = canResize(p);
                lastMouseX = p.x;
            }

            private void setCursor(Cursor c) {
                if (header.getCursor() != c) {
                    header.setCursor(c);
                }
            }

            private boolean canResize(Point mousePoint) {
                return mousePoint.x >= (header.getSize().width - 3);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (canResize(e.getPoint()) || dragging) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int mouseX = e.getX();
                int deltaX = lastMouseX - mouseX;

                if (deltaX == 0) {
                    return;
                }

                if (dragging) {
                    Dimension size = table.getPreferredScrollableViewportSize();
                    int parentWidth = compoundScrollPane.getWidth();
                    int tableMinWidth = table.getMinimumSize().width;

                    int newWidth;

                    if ((size.width + deltaX) > (parentWidth - 20)) {
                        newWidth = parentWidth - 20;
                    } else if ((size.width + deltaX) < tableMinWidth) {
                        newWidth = tableMinWidth;
                    } else {
                        newWidth = size.width + deltaX;
                    }

                    table.setPreferredScrollableViewportSize(new Dimension(newWidth, size.height));
                    lastMouseX = lastMouseX - (newWidth - size.width);

                    table.revalidate();
                    table.repaint();
                } else {
                    lastMouseX = mouseX;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        }
    }

    /** Scrollable (better say not scrollable) pane. Used as container for
     * left (controlling) and rigth (controlled) scroll panes.
     */
    private static class CompoundScrollPane extends JPanel implements Scrollable {
        CompoundScrollPane() {
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        public boolean getScrollableTracksViewportHeight() {
            return true;
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }
    }

    /** Makes visible horizontal scroll bar of dependent scrollpane. Enables/disables
     * horizontal scrollbar of parent scrollpane.
     */
    private static final class EnablingScrollPaneLayout extends ScrollPaneLayout {
        JScrollPane dependentScrollPane;

        public EnablingScrollPaneLayout(JScrollPane scrollPane) {
            dependentScrollPane = scrollPane;
        }

        @Override
        public void layoutContainer(Container parent) {
            super.layoutContainer(parent);

            Component view = (viewport != null) ? viewport.getView() : null;
            Dimension viewPrefSize = (view != null) ? view.getPreferredSize() : new Dimension(0, 0);
            Dimension extentSize = (viewport != null) ? viewport.toViewCoordinates(viewport.getSize())
                                                      : new Dimension(0, 0);

            boolean viewTracksViewportWidth = (view instanceof Scrollable) &&
                ((Scrollable) view).getScrollableTracksViewportWidth();
            boolean hsbNeeded = !viewTracksViewportWidth && (viewPrefSize.width > extentSize.width);

            // enable horizontal scrollbar only if it is needed
            if (hsb != null) {
                hsb.setEnabled(hsbNeeded);
            }

            // make dependent horizontal scrollbar visible by setting scrollbar policy
            JScrollPane scrollPane = (JScrollPane) parent;

            if (scrollPane.getHorizontalScrollBarPolicy() != JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS) {
                int newPolicy = hsbNeeded ? JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
                                          : JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;

                if (newPolicy != dependentScrollPane.getHorizontalScrollBarPolicy()) {
                    dependentScrollPane.setHorizontalScrollBarPolicy(newPolicy);
                    dependentScrollPane.getViewport().invalidate();
                }
            }
        }
    }
}
