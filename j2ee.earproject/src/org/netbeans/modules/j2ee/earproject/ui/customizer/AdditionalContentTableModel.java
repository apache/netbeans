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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.openide.util.NbBundle;

public final class AdditionalContentTableModel extends AbstractTableModel implements ListDataListener {

    private DefaultListModel model;

    public static AdditionalContentTableModel createTableModel ( Iterator it ) {
        return new AdditionalContentTableModel( ClassPathUiSupport.createListModel( it ) );
    }
    
    public AdditionalContentTableModel(DefaultListModel model) {
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
            return NbBundle.getMessage(AdditionalContentTableModel.class, "TXT_Archive_Item");
        } else {
            return NbBundle.getMessage(AdditionalContentTableModel.class, "TXT_Archive_PathInArchive");
        }
    }

    public Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return ClassPathSupport.Item.class;
        } else {
            return String.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0;
    }

    public Object getValueAt(int row, int column) {
        if (column == 0) {
            return getItem(row);
        } else {
            return getItem(row).getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
        }
    }

    public void setValueAt(Object value, int row, int column) {
        if (column != 1 || !(value instanceof String)) {
            return;
        }
        getItem(row).setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, (String)value);
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
