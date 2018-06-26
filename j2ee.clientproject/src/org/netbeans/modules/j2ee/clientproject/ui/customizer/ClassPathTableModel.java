/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.openide.util.NbBundle;

public final class ClassPathTableModel extends AbstractTableModel implements ListDataListener {

    private static final long serialVersionUID = 1L;
    private DefaultListModel model;

    public static ClassPathTableModel createTableModel( Iterator it ) {
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

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return NbBundle.getMessage(ClassPathTableModel.class, "LBL_CustomizeLibraries_TableHeader_Library");
        } else {
            return NbBundle.getMessage(ClassPathTableModel.class, "LBL_CustomizeLibraries_TableHeader_Deploy");
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return ClassPathSupport.Item.class;
        } else {
            return Boolean.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0 && getShowItemAsIncludedInDeployment(getItem(rowIndex)) instanceof Boolean;
    }

    public Object getValueAt(int row, int column) {
        ClassPathSupport.Item item = getItem(row);
        if (column == 0) {
            return item;
        } else {
            return getShowItemAsIncludedInDeployment(item);
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column != 1 || !(value instanceof Boolean)) {
            return;
        }

        getItem(row).setAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT, Boolean.toString(value == Boolean.TRUE));
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

    private void setItem(ClassPathSupport.Item item, int index) {
        model.set(index, item);
    }

    private Boolean getShowItemAsIncludedInDeployment(ClassPathSupport.Item item) {
        return Boolean.valueOf("true".equals(item.getAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT)));
    }
}
