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
package org.netbeans.modules.db.dataview.table;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
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
            TableColumn col = new TableColumn(0, 75);
            col.setHeaderValue("#");
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
        private final JTable backingTable;

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            String propertyName = pce.getPropertyName();
            if (PROP_ROW_SORTER.equals(propertyName)
                    || PROP_SORTER.equals(propertyName)) {
                if (backingSorter != null) {
                    backingSorter.removeRowSorterListener(this);
                }
                backingSorter = null;
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

        public CountingTableModel(JTable table) {
            this.backingTable = table;
            this.backingSorter = this.backingTable.getRowSorter();
            if(this.backingSorter != null) {
                this.backingSorter.addRowSorterListener(this);
            }
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
            SwingUtilities.invokeLater(() -> {
                setCount(backingTable.getRowCount());
                fireTableDataChanged();
            });
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

    private static final Icon ICON_RIGHT_ARROW = new Icon() {

        @Override
        public int getIconWidth() {
            return 10;
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
    
    private static final Icon ICON_CLEAR = new Icon() {

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
        }
    };

    private class RowHeaderColumnRenderer extends JPanel implements TableCellRenderer {

        private final JLabel iconLabel = new JLabel(ICON_CLEAR);
        private final JLabel textLabel = new JLabel("");
        
        @SuppressWarnings("OverridableMethodCallInConstructor")
        public RowHeaderColumnRenderer() {
            super();

            GridBagConstraints iconConstraints = new GridBagConstraints();
            iconConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
            iconConstraints.fill = GridBagConstraints.NONE;
            iconConstraints.weightx = 0;
            iconConstraints.weighty = 0;

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
            labelConstraints.fill = GridBagConstraints.HORIZONTAL;
            labelConstraints.weightx = 1;
            labelConstraints.weighty = 0;
            
            this.setLayout(new GridBagLayout());
            this.add(iconLabel, iconConstraints);
            this.add(textLabel, labelConstraints);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            textLabel.setText(value == null ? "" : value.toString());
            if(isSelected) {
                iconLabel.setIcon(ICON_RIGHT_ARROW);
                this.setBackground(table.getSelectionBackground());
                this.setForeground(table.getSelectionForeground());
            } else {
                iconLabel.setIcon(ICON_CLEAR);
                this.setBackground(table.getBackground());
                this.setForeground(table.getForeground());
            }
            return this;
        }
    }
    /**
     * The headerTable used to create the row header column.
     */
    private final CountingTableModel ctm;
    private final JTable headerTable;
    private final JTable backingTable;

    /**
     * Create a row header from the given {@code JTable}. This row header will
     * have the same {@code TableModel} and {@code ListSelectionModel} as the
     * incoming table.
     *
     * @param table the table for which to produce a row header.
     */
    public JXTableRowHeader(JTable table) {
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
        this.headerTable.getTableHeader().setToolTipText("Row number");
        this.headerTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RowSorter sorter = backingTable.getRowSorter();
                if(sorter instanceof DefaultRowSorter) {
                    ((DefaultRowSorter) sorter).setSortKeys(null);
                }
            }
        });

        add(this.headerTable);
        TableColumn column = this.headerTable.getColumnModel().getColumn(0);

        // pack before setting preferred width.
        this.headerTable.doLayout();

        TableCellRenderer defaultRenderer = createDefaultRenderer();

        Component c = defaultRenderer.getTableCellRendererComponent(
                headerTable, "00000", false, false, 0, 0);              //NOI18N

        column.setPreferredWidth((int) c.getMinimumSize().getWidth() + 10);
        column.setCellRenderer(createDefaultRenderer());
        this.headerTable.setPreferredScrollableViewportSize(new Dimension(
                column.getPreferredWidth(), 0));

        this.headerTable.setInheritsPopupMenu(true);
        this.headerTable.setShowGrid(true);
        this.headerTable.setGridColor(ResultSetJXTable.GRID_COLOR);
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
