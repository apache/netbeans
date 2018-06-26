/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
/**
 * DDTable.java
 *
 * @author Ana von Klopp
 */
package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.event.TableModelEvent;
import org.openide.util.NbBundle;

class DDTable extends JTable implements KeyListener {

    private static final Logger LOG = Logger.getLogger(DDTable.class.getName());
    private String titleKey;
    private Editable editable;
    private String[] headers;
    private final static int margin = 6;
    private Color darkerColor;

    // Handle resizing for larger fonts
    private boolean fontChanged = true;
    private boolean addedRow = true;
    private int rowHeight = 23;
    private static final long serialVersionUID = -155464225493968935L;

    DDTable(String[] headers, String titleKey) {
        this(headers, titleKey, Editable.BOTH);
    }

    DDTable(String[] headers, String titleKey, Editable editable) {
        super(new Object[0][headers.length], headers);
        this.headers = headers;
        this.titleKey = titleKey;
        this.editable = editable;
        this.darkerColor = getBackground().darker();

        setModel(new DDTableModel(headers, editable));
        setColors(editable);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setIntercellSpacing(new Dimension(margin, margin));
        DefaultCellEditor dce = new DefaultCellEditor(new CellText(this));
        dce.setClickCountToStart(1);
        getColumnModel().getColumn(0).setCellEditor(dce);
        getColumnModel().getColumn(1).setCellEditor(dce);
    }

    void setEditable(Editable editable) {
        this.editable = editable;
        setColors(editable);
    }

    Editable getEditable() {
        return this.editable;
    }

    int addRow(String[] values) {
        int i = ((DDTableModel) getModel()).addRow(values);
        if (i == 0) {
            fontChanged = true;
        }
        addedRow = true;
        this.invalidate();
        return i;
    }

    void removeRow(int row) {
        if (isEditing()) {
            getCellEditor().cancelCellEditing();
        }

        ((DDTableModel) getModel()).removeRow(row);
        this.invalidate();

        int maxSelectedRow = getRowCount() - 1;
        if (getSelectedRow() > maxSelectedRow) {
            if (maxSelectedRow >= 0) {
                setRowSelectionInterval(maxSelectedRow, maxSelectedRow);
            } else {
                clearSelection();
            }
        }
    }

    String getColumnKey(int col) {
        return headers[col];
    }

    private void setColors(Editable editable) {
        this.setBorder(BorderFactory.createLoweredBevelBorder());
        this.setBackground(editable == Editable.NEITHER ? darkerColor : Color.white);
    }

    /**
     * Override the getter for the cell editors, so that customized
     * cell editors will show up.
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int col) {
        return super.getCellRenderer(row, col);
    }


    // This method is used by the edit button of the InitParamTable
    void setData(String name, String value, int row) {
        if (getEditingRow() == row) {
            int col = getEditingColumn();
            getCellEditor(row, col).cancelCellEditing();
        }
        ((DDTableModel) getModel()).setData(name, value, row);
    }

    /**
     * Checks whether the cells are editable 
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (editable == Editable.NEITHER) {
            return false;
        }
        if (editable == Editable.VALUE && col == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    @Override
    public void paint(Graphics g) {
        if (fontChanged) {
            LOG.finer("Font changed"); //NOI18N
            fontChanged = false;

            int height = 0;
            FontMetrics fm = g.getFontMetrics(getFont());
            // Add 2 for button border
            // height = fm.getHeight() + 2 + margin;
            height = fm.getHeight() + margin;
            if (height > rowHeight) {
                rowHeight = height;
            }

            LOG.finer("row height is " + rowHeight); //NOI18N

            //triggers paint, just return afterwards
            this.setRowHeight(rowHeight);
            return;
        }

        if (addedRow) {
            addedRow = false;
            LOG.finer("Added row");
            int row = getModel().getRowCount() - 1;
            this.editCellAt(row, 0);
            Component c = getCellEditor(row, 0).getTableCellEditorComponent(this, getValueAt(row, 0),
                    true, row, 0);
            if (c instanceof JTextField) {
                LOG.finer("Trying to request focus");
                ((JTextField) c).requestFocus();
            }
        }
        super.paint(g);
    }

    public void keyPressed(KeyEvent keyEvent) {
    }

    public void keyReleased(KeyEvent keyEvent) {
        LOG.finer("keyReleased()");

        Object o = keyEvent.getSource();
        String s = null;
        if (o instanceof JTextField) {
            LOG.finer("Found text field");
            s = ((JTextField) o).getText().trim();
        }

        int row = getEditingRow();
        int col = getEditingColumn();
        LOG.finer("row=" + row + ", col=" + col);

        setValueAt(s, row, col);
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    static class DDTableModel extends AbstractTableModel {

        private String[] colheaders = null;
        private Object[][] data = null;
        private Editable editable;
        private int numCols;
        private int numRows = 0;
        private static final long serialVersionUID = -5044296029944667379L;

        DDTableModel(String[] headers, Editable editable) {
            this.colheaders = headers;
            this.editable = editable;
            numCols = colheaders.length;
            data = new Object[numRows][numCols];
        }

        @Override
        public String getColumnName(int col) {
            String key = "LBL_"+colheaders[col];
            return NbBundle.getMessage(DDTable.class, key);
        }

        public int getRowCount() {
            return data.length;
        }

        public int getColumnCount() {
            return numCols;
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public int addRow(String[] values) {
            Object[][] data2 = new Object[numRows + 1][numCols];
            int i = 0, j = 0;

            if (numRows > 0) {
                for (j = 0; j < numRows; ++j) {
                    data2[j] = data[j];
                }
            }

            for (i = 0; i < values.length; ++i) {
                data2[j][i] = values[i];
            }

            data = data2;
            numRows++;
            return j;
        }

        public void removeRow(int row) {
            LOG.finer("removeRow(): row is " + row + ", numRows is " + numRows); //NOI18N

            Object[][] data2 = new Object[numRows - 1][numCols];
            int newRowIndex = 0;
            for (int i = 0; i < numRows; ++i) {
                if (i == row) {
                    continue;
                }
                data2[newRowIndex] = data[i];
                newRowIndex++;
                LOG.finer("newRowIndex is " + newRowIndex); //NOI18N
            }
            data = data2;
            numRows--;
        }

        void setData(String name, String value, int row) {
            data[row][0] = name;
            data[row][1] = value;
            fireTableChanged(new TableModelEvent(this, row));
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            LOG.finer("setValueAt(): value = " + value + " at " + row + ", " + col); //NOI18N

            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    } // DDTableModel

    static class CellText extends JTextField {

        private static final long serialVersionUID = 2674682216176560005L;

        public CellText(DDTable table) {
            super();
            addKeyListener(table);
            getAccessibleContext().setAccessibleName(this.getText()); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DDTable.class, "ACSD_ipcell")); // NOI18N
        }
    }
}
