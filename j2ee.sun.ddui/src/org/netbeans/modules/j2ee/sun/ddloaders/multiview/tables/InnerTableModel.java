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

package org.netbeans.modules.j2ee.sun.ddloaders.multiview.tables;

import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.*;

/**
 * @author pfiala
 */
public abstract class InnerTableModel extends AbstractTableModel {

    private XmlMultiViewDataSynchronizer synchronizer;
    protected final String[] columnNames;
    private int[] columnWidths;
    private int rowCount = -1;

    public InnerTableModel(XmlMultiViewDataSynchronizer synchronizer, String[] columnNames, int[] columnWidths) {
        this.synchronizer = synchronizer;
        this.columnNames = columnNames;
        this.columnWidths = columnWidths;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public TableCellEditor getCellEditor(int columnIndex) {
        return null;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int[] getColumnWidths() {
        return columnWidths;
    }

    public abstract int addRow();

    public abstract void removeRow(int selectedRow);

    public int getDefaultColumnWidth(int i) {
        return columnWidths[i];
    }

    public void refreshView() {
        fireTableDataChanged();
    }

    protected void tableChanged() {
        if (!checkRowCount()) {
            fireTableDataChanged();
        }
    }

    private boolean checkRowCount() {
        int n = getRowCount();
        if (rowCount == -1) {
            rowCount = n;
        }
        if (n != rowCount) {
            while (rowCount < n) {
                rowCount++;
                fireTableRowsInserted(0, 0);
            }
            while (rowCount > n) {
                rowCount--;
                fireTableRowsDeleted(0, 0);
            }
            return true;
        } else {
            return false;
        }
    }

    protected void modelUpdatedFromUI() {
        if (synchronizer != null) {
            synchronizer.requestUpdateData();
        }
    }

    protected TableCellEditor createComboBoxCellEditor(Object[] items) {
        return createComboBoxCellEditor(items, false);
    }

    private static TableCellEditor createComboBoxCellEditor(Object[] items, final boolean editable) {
        final JComboBox comboBox = new JComboBox(items);
        comboBox.setEditable(editable);
        return new DefaultCellEditor(comboBox);
    }
}
