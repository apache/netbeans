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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Pis
 */
public class PropertiesTableModel extends AbstractTableModel {
    
    public static final String COLUMN_NAME_NAME = "name"; // NOI18N
    public static final String COLUMN_NAME_VALUE = "value"; // NOI18N
    
    private HgPropertiesNode[] nodes;
    private String[] columns;
    
    private static final Map<String, String[]> columnLabels = new HashMap<String, String[]>(2); 
    
    {
        ResourceBundle loc = NbBundle.getBundle(PropertiesTableModel.class);    
        columnLabels.put(COLUMN_NAME_NAME, new String[] {loc.getString("CTL_PropertiesTable_Column_Name"), loc.getString("CTL_PropertiesTable_Column_Name")}); // NOI18N
        columnLabels.put(COLUMN_NAME_VALUE, new String[] {loc.getString("CTL_PropertiesTable_Column_Value"), loc.getString("CTL_PropertiesTable_Column_Value")}); // NOI18N
    }
    
    /** Creates a new instance of PropertiesTableModel */
    public PropertiesTableModel(String[] clms) {
        if (Arrays.equals(columns, clms))
            return;
        setColumns(clms);
        setNodes(new HgPropertiesNode[0]);
    }
    
    public void setColumns(String[] clms) {
        this.columns = clms;
        fireTableStructureChanged();
    }
    
    public void setNodes(HgPropertiesNode[] nodes) {
        this.nodes = nodes;
        fireTableDataChanged();
    }
    
    public HgPropertiesNode[] getNodes() {
        return nodes;
    }
    
    public HgPropertiesNode getNode(int row) {
        return nodes[row];
    }
    
    public int getRowCount() {
        return nodes.length;
    }

    public String getColumnName(int column) {
        return columnLabels.get(columns[column])[0];
    }
    
    public int getColumnCount() {
        return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        String clm = columns[columnIndex];
        if (clm.equals(COLUMN_NAME_NAME)) {
            return nodes[rowIndex].getName();
        } else if (clm.equals(COLUMN_NAME_VALUE)) {
            return nodes[rowIndex].getValue();
        }
        throw new IllegalArgumentException("The column index is out of index: " + columnIndex); // NOI18N
    }

    
}
