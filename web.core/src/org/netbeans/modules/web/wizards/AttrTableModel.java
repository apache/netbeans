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

package org.netbeans.modules.web.wizards;

import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 */
public class AttrTableModel extends AbstractTableModel {

    public AttrTableModel() {
    }

    private String[] colheaders = null;
    private Object[][] data = null;
    private int numCols;
    private int numRows=0;

    /** Creates a new instance of AttrTableModel */
    AttrTableModel(String[] headers) { 
        this.colheaders = headers; 
        numCols = colheaders.length;
        
    }
    
    AttrTableModel(String[] headers, Object[][] data) { 
        this.colheaders = headers; 
        numCols = colheaders.length;
        this.data=data;
        numRows = data.length;
    }
    
    @Override
    public String getColumnName(int col) { 
        String key = "LBL_"+colheaders[col]; //NOI18N
        return NbBundle.getMessage(AttrTableModel.class, key); 
    }

    public int getRowCount() {
        return numRows;
    }
    
    public int getColumnCount() {
        return numCols;
    }

    public Object getValueAt(int row, int col) { 
        return data[row][col];
    }
    
    public int addRow(String name, String type, boolean required, boolean rtexpr) { 
        Object[][] data2 = new Object[numRows+1][numCols]; 
        int i=0, j=0; 

        if(numRows > 0) { 
            for(j=0; j<numRows; ++j) 
                data2[j] = data[j]; 
        }

        data2[j][0] = name;
        data2[j][1] = type;
        data2[j][2] = Boolean.valueOf(required);
        data2[j][3] = Boolean.valueOf(rtexpr);
        data = data2; 
        numRows++;
        return j; 
    }

    public void removeRow(int row) { 
        Object[][] data2 = new Object[numRows-1][numCols]; 
        int newRowIndex = 0; 
        for(int i=0; i<numRows; ++i) { 
            if(i==row) continue; 
            data2[newRowIndex]=data[i]; 
            newRowIndex++;
        }
        data = data2; 
        numRows--;
    }

    public void setData(String name, String value, boolean required, boolean rtexpr, int row) { 
        data[row][0] = name;
        data[row][1] = value;
        data[row][2] = Boolean.valueOf(required);
        data[row][3] = Boolean.valueOf(rtexpr);
        fireTableChanged(new TableModelEvent(this, row)); 
    } 

    @Override
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public Object[][] getAttributes() {
        if (data==null) return new Object[][]{};
        else return data;
    }
}
