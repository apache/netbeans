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

package org.netbeans.modules.web.project.ui.customizer;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.openide.util.NbBundle;

public final class ClassPathTableModel extends AbstractTableModel implements ListDataListener {

    private DefaultListModel model;

    public static ClassPathTableModel createTableModel ( Iterator it ) {
        return new ClassPathTableModel( ClassPathUiSupport.createListModel( it ) );
    }
    
    public ClassPathTableModel(DefaultListModel model) {
        super();
        this.model = model;
        model.addListDataListener(this);
    }

    public DefaultListModel getDefaultListModel() {
        return model;
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return model.getSize();
    }

    public String getColumnName(int column) {
        if (column == 0) {
            return NbBundle.getMessage(ClassPathTableModel.class, "LBL_CustomizeCompile_TableHeader_Name");
        } else {
            return NbBundle.getMessage(ClassPathTableModel.class, "LBL_CustomizeCompile_TableHeader_Deploy");
        }
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return ClassPathSupport.Item.class;
        } else {
            return Boolean.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return getItem(row);
        } else {
            String pathInWar = getItem(row).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
            return (ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB.equals(pathInWar) || ClassPathSupportCallbackImpl.PATH_IN_WAR_DIR.equals(pathInWar)) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    public void setValueAt(Object value, int row, int column) {
        if (column != 1 || !(value instanceof Boolean)) {
            return;
        }
        if (Boolean.TRUE.equals(value)) {
            ClassPathSupport.Item item = getItem(row);
            String pathInWar = (item.getType() == ClassPathSupport.Item.TYPE_JAR && item.getResolvedFile().isDirectory()) || 
                    (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY && Utils.isLibraryDirectoryBased(item)) ? ClassPathSupportCallbackImpl.PATH_IN_WAR_DIR : ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB;
            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, pathInWar);
        } else {
            getItem(row).setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, ClassPathSupportCallbackImpl.PATH_IN_WAR_NONE);
        }
        fireTableCellUpdated(row, column);
    }

    public void contentsChanged(ListDataEvent e) {
        fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
    }

    public void intervalAdded(ListDataEvent e) {
        fireTableRowsInserted(e.getIndex0(), e.getIndex1());
    }

    public void intervalRemoved(ListDataEvent e) {
        fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
    }

    private ClassPathSupport.Item getItem(int index) {
        return (ClassPathSupport.Item) model.get(index);
    }
}
