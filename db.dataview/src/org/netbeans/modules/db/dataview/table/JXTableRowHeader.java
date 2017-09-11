/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.border.IconBorder;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.TableColumnExt;
import org.netbeans.modules.db.dataview.output.DataViewTableUIModel;

/**
 * The class {@code JXTableRowHeader} is used to create a column which contains
 * row header cells. By default a table will not show a row header. The user may
 * manually add a row header to the {@code JScrollPane} row header view port.
 *
 * @see javax.swing.JScrollPane
 * @author Ahimanikya Satapathy
 */
public final class JXTableRowHeader extends JComponent {

    private static class InternalTableColumnModel extends DefaultTableColumnModel {

        public InternalTableColumnModel() {
            TableColumnExt col = new TableColumnExt(0, 75);
            col.setEditable(false);
            col.setHeaderValue("#");
            col.setToolTipText("Row number");
            col.setSortable(false);
            addColumn(col);
        }
    }

    private static class CountingTableModel implements TableModel, PropertyChangeListener, TableModelListener,RowSorterListener {
        private static final String PROP_ROW_SORTER = "rowSorter";
        private static final String PROP_SORTER = "sorter";
        private static final String PROP_MODEL = "model";
        private int count;
        private TableModel backingTableModel;
        private RowSorter<?> backingSorter;
        private final Set<TableModelListener> listeners = new HashSet<>();
        private final JXTable backingTable;

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            String propertyName = pce.getPropertyName();
            if (PROP_ROW_SORTER.equals(propertyName)
                    || PROP_SORTER.equals(propertyName)) {
                if (backingSorter != null) {
                    backingSorter.removeRowSorterListener(this);
                }
                if (pce.getNewValue() != null) {
                    backingSorter = (RowSorter) pce.getNewValue();
                    backingSorter.addRowSorterListener(this);
                }
                tableDataChanged();
            } else if (PROP_MODEL.equals(propertyName)) {
                if (backingTableModel != null) {
                    backingTableModel.removeTableModelListener(
                            this);
                }
                backingTableModel = (TableModel) pce.getNewValue();
                if (backingTableModel != null) {
                    backingTableModel.addTableModelListener(this);
                }
                tableDataChanged();
            }
        }

        public CountingTableModel(JXTable table) {
            this.backingTable = table;
            backingTable.addPropertyChangeListener(this);
            this.backingTableModel = table.getModel();
            this.backingTableModel.addTableModelListener(this);
            setCount(backingTable.getRowCount());
        }

        private void fireTableDataChanged() {
            for (TableModelListener tml : listeners) {
                tml.tableChanged(new TableModelEvent(this));
            }
        }

        private void setCount(int count) {
            // Only invoke tableChanged event if row count really changed
            // else the selection is cleared (see bug #240958)
            if (count != this.count) {
                this.count = count;
                fireTableDataChanged();
            }
        }

        @Override
        public void addTableModelListener(TableModelListener tl) {
            listeners.add(tl);
        }

        @Override
        public Class<?> getColumnClass(int i) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int i) {
            return "Row number";
        }

        @Override
        public int getRowCount() {
            return this.count;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (backingTableModel instanceof DataViewTableUIModel) {
                // The row passed into this model is the view row number of the
                // backing table to get the right model coordinate in the table
                // model the coordination transformation has to be done
                try {
                    int modelRow = backingTable.convertRowIndexToModel(row);
                    return Integer.toString(
                            ((DataViewTableUIModel) backingTableModel).getTotalRowOffset(modelRow)
                            + 1
                    );
                } catch (IndexOutOfBoundsException ex) {
                    return null;
                }
            } else {
                return Integer.toString(row + 1);
            }
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }

        @Override
        public void removeTableModelListener(TableModelListener tl) {
            listeners.remove(tl);
        }

        @Override
        public void setValueAt(Object o, int i, int i1) {
            throw new NoSuchMethodError();
        }

        private void tableDataChanged() {
            setCount(backingTable.getRowCount());
            fireTableDataChanged();
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            tableDataChanged();
        }

        @Override
        public void sorterChanged(RowSorterEvent rse) {
            tableDataChanged();
        }
    }

    private final PropertyChangeListener backingTableListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (pce.getPropertyName().equals("rowHeight")) {
                headerTable.setRowHeight((Integer) pce.getNewValue());
            }
        }
    };

    public JTableHeader getTableHeader() {
        JTableHeader header = headerTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        return header;
    }

    private static final Icon rightArrow = new Icon() {

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawLine(x + 4, y + 4, x + 4, y + 4);
            g.translate(x + 4, y + 4);
            g.fillPolygon(new Polygon(new int[]{0, 5, 0}, new int[]{-5, 0, 5}, 3));
        }
    };
    private final IconBorder iconBorder = new IconBorder();

    private class RowHeaderColumnRenderer extends DefaultTableRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, rowIndex, columnIndex);

            if (isSelected) {
                iconBorder.setIcon(rightArrow);
                Border origBorder = ((JComponent) comp).getBorder();
                Border border = new CompoundBorder(origBorder, iconBorder);
                ((JComponent) comp).setBorder(border);
                comp.setBackground(table.getSelectionBackground());
                comp.setForeground(table.getSelectionForeground());
            }
            return comp;
        }
    }
    /**
     * The headerTable used to create the row header column.
     */
    private final CountingTableModel ctm;
    private final JXTable headerTable;
    private final JXTable backingTable;

    /**
     * Create a row header from the given {@code JTable}. This row header will
     * have the same {@code TableModel} and {@code ListSelectionModel} as the
     * incoming table.
     *
     * @param table the table for which to produce a row header.
     */
    public JXTableRowHeader(JXTable table) {
        assert table != null : "JXTableRowHeader needs to be instanciated with a JXTable";

        this.backingTable = table;

        ctm = new CountingTableModel(backingTable);

        headerTable = new JXTableDecorator(ctm,
                new JXTableRowHeader.InternalTableColumnModel());

        backingTable.addPropertyChangeListener(backingTableListener);
        headerTable.setRowHeight(backingTable.getRowHeight());
        headerTable.setSelectionModel(backingTable.getSelectionModel());

        setLayout(new GridLayout(1, 1));

        this.headerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.headerTable.getTableHeader().setReorderingAllowed(false);
        this.headerTable.getTableHeader().setResizingAllowed(false);

        add(this.headerTable);
        TableColumn column = this.headerTable.getColumnModel().getColumn(0);

        // pack before setting preferred width.
        this.headerTable.packAll();

        TableCellRenderer defaultRenderer = createDefaultRenderer();

        Component c = defaultRenderer.getTableCellRendererComponent(
                headerTable, "00000", false, false, 0, 0);              //NOI18N

        column.setPreferredWidth((int) c.getMinimumSize().getWidth() + 10);
        column.setCellRenderer(createDefaultRenderer());
        this.headerTable.setPreferredScrollableViewportSize(new Dimension(
                column.getPreferredWidth(), 0));

        this.headerTable.setInheritsPopupMenu(true);
        this.headerTable.setShowGrid(true, true);
        this.headerTable.setGridColor(ResultSetJXTable.GRID_COLOR);
        this.headerTable.setHighlighters(
                HighlighterFactory.createAlternateStriping(
                        ResultSetJXTable.ROW_COLOR, ResultSetJXTable.ALTERNATE_ROW_COLOR));
    }

    /**
     * Returns a default renderer to be used when no row header renderer is
     * defined by the constructor.
     *
     * @return the default row header renderer
     */
    protected TableCellRenderer createDefaultRenderer() {
        // TODO get a rollover enabled renderer
        //return new ColumnHeaderRenderer();
        return new JXTableRowHeader.RowHeaderColumnRenderer();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return headerTable.getToolTipText(event);
    }
}
