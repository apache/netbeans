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
/*
 * PropertiesTableModel.java
 *
 * Created on October 1, 2003, 9:18 AM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.util.Vector;
import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author  nityad
 */
public class PropertiesTableModel extends javax.swing.table.AbstractTableModel {
    private static java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); // NOI18N
    private Vector data = null;
    /** Creates a new instance of PropertiesTableModel */
    public PropertiesTableModel(ResourceConfigData data) {
        this.data = data.getProperties();   //NOI18N
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public int getRowCount() {
        return data.size();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        NameValuePair pair = (NameValuePair)data.elementAt(rowIndex);
        if (columnIndex == 0) 
            return pair.getParamName();
        else
            return pair.getParamValue();
    }
    
    public String getColumnName(int col) {
        if (0 == col) 
            return bundle.getString("COL_HEADER_NAME"); //NOI18N
        if (1 == col)
            return bundle.getString("COL_HEADER_VALUE"); //NOI18N
        throw new RuntimeException(bundle.getString("COL_HEADER_ERR_ERR_ERR")); //NOI18N
    }
    
    public boolean isCellEditable(int row, int col) {
       return true;
    }
    
    public void setValueAt(Object value, int row, int col) {
        if((row >=0) && (row < data.size())){
            NameValuePair property = (NameValuePair)data.elementAt(row);
            if (col == 0){
                if(! isNotUnique((String)value))
                    property.setParamName((String)value);
            }else if (col == 1)
                property.setParamValue((String)value);
        }    
        fireTableDataChanged();
    }

    //Fix for bug#5026041 - Table should not accept duplicate prop names.
    private boolean isNotUnique(String newVal){
        for(int i=0; i<data.size()-1; i++){
            NameValuePair pair = (NameValuePair)data.elementAt(i);
            if(pair.getParamName().equals(newVal)){
                NotifyDescriptor d = new NotifyDescriptor.Message(bundle.getString("Err_DuplicateValue"), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return true;
            }    
        }
        return false;
    }
    
    public void setData(ResourceConfigData data) {
        this.data = data.getProperties();
        fireTableDataChanged();
    }
    
////    private boolean changed = false;
}
