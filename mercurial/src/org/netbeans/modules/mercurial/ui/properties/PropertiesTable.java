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
package org.netbeans.modules.mercurial.ui.properties;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.versioning.util.TableSorter;
import org.openide.util.NbBundle;

/**
 *
 * @author Padraig O'Briain
 */
public class PropertiesTable implements AncestorListener, TableModelListener {
    
    static public final String[] PROPERTIES_COLUMNS = new String[] {PropertiesTableModel.COLUMN_NAME_NAME, PropertiesTableModel.COLUMN_NAME_VALUE};
            
    private PropertiesTableModel tableModel;
    private JTable table;
    private JComponent component;
    private String[] columns;
    
    /** Creates a new instance of PropertiesTable */
    public PropertiesTable(JLabel label, String[] columns) {
        init(label, columns);
    }
    
    private void init(JLabel label, String[] columns) {
        tableModel = new PropertiesTableModel(columns);
        tableModel.addTableModelListener(this);
        table = new JTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(String.class, new PropertiesTableCellRenderer());
        //table.setDefaultEditor(CommitOptions.class, new CommitOptionsCellEditor());
        table.setRowHeight(table.getRowHeight());
        table.addAncestorListener(this);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        component.setPreferredSize(new Dimension(340, 150));
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PropertiesTable.class, "ACSD_PropertiesTable")); // NOI18N        
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PropertiesTable.class, "ACSN_PropertiesTable")); // NOI18N        
        label.setLabelFor(table);
        setColumns(columns);
    }
    
    public void setColumns(String[] clmns) {
        if (Arrays.equals(columns, clmns))
            return;
        columns = clmns;
        tableModel.setColumns(clmns);
        setDefaultColumnSize();
    }
    
    public JTable getTable() {
        return table;
    }
    
    private void setDefaultColumnSize() {
        int width = table.getWidth();
        TableColumnModel columnModel = table.getColumnModel();
        if (columns == null || columnModel == null)
            return;
        if (columnModel.getColumnCount() != columns.length)
            return;
        for (int i = 0; i < columns.length; i++) {
            String col = columns[i];                                
            if (col.equals(PropertiesTableModel.COLUMN_NAME_NAME)) {
                columnModel.getColumn(i).setPreferredWidth(width * 20 / 100);
            } else if (col.equals(PropertiesTableModel.COLUMN_NAME_VALUE)) {
                columnModel.getColumn(i).setPreferredWidth(width * 40 / 100);
            }
        }
    }
    
    public TableModel getTableModel() {
        return tableModel;
    }
    
    public void dataChanged() {
        int idx = table.getSelectedRow();
        tableModel.fireTableDataChanged();
        if (idx != -1) {
            table.getSelectionModel().addSelectionInterval(idx, idx);
        }    
    }
    
    public int[] getSelectedItems() {
        return table.getSelectedRows();
    }
     
    public HgPropertiesNode[] getNodes() {
        return tableModel.getNodes();
    }
    
    public void setNodes(HgPropertiesNode[] nodes) {
        tableModel.setNodes(nodes);
    }
    
    public JComponent getComponent() {
        return component;
    }
    
    public void ancestorAdded(AncestorEvent arg0) {
        setDefaultColumnSize();
    }

    public void ancestorRemoved(AncestorEvent arg0) {
    }

    public void ancestorMoved(AncestorEvent arg0) {
    }

    public void tableChanged(TableModelEvent event) {
        table.repaint();
    }
    

    public class PropertiesTableCellRenderer extends DefaultTableCellRenderer {
           
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
            Component renderer =  super.getTableCellRendererComponent(table, value, hasFocus, hasFocus, rowIndex, columnIndex);
            if (renderer instanceof JComponent) {
                String strValue = tableModel.getNode(rowIndex).getValue(); 
                ((JComponent) renderer).setToolTipText(strValue);
            }
            setToolTipText(value.toString());
            return renderer;
        }
    }
    
    
}
