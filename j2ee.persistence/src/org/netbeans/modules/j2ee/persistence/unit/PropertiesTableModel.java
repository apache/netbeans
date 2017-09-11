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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.unit;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.openide.util.NbBundle;

/**
 * Table model for the Properties Table
 * 
 * @author Dongmei Cao
 */
public class PropertiesTableModel extends AbstractTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(PropertiesTableModel.class, "LBL_Name"),
        NbBundle.getMessage(PropertiesTableModel.class, "LBL_Value")
    };
    private static final String jpa_prefix = "javax.persistence."; // NO18N
    private PropertiesPanel.PropertiesParamHolder propParam;
    private ArrayList<PropertyData> propsData;

    public PropertiesTableModel(PropertiesPanel.PropertiesParamHolder propParam) {
        this.propParam = propParam;
        getPropsData();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row, int column) {

        if (propParam == null || propParam.getPU()==null) {
            return null;
        } else {
            if (column == 0) {
                return propsData.get(row ).getPropName();
            } else {
                return propsData.get(row).getPropValue();
            }
        }
    }

    @Override
    public int getRowCount() {
        if (propParam == null || propParam.getPU()==null) {
            return 0;
        } else {
            return propsData.size();
        }
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (false);
    }

    public void addRow(String propName, String propValue) {
        Property prop = propParam.getPU().getProperties().newProperty();
        prop.setName(propName);
        prop.setValue(propValue);
        int index = propParam.getPU().getProperties().addProperty2(prop);
        // Update the local cache
        propsData.add( new PropertyData(index, propName, propValue ) );

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public void editRow(int row, String propValue) {
        
        PropertyData data = propsData.get(row);
        Property tmp = propParam.getPU().getProperties().getProperty2(data.getModelIndex());
        tmp.setValue(propValue);

        // Update local cache
        data.setPropValue( propValue );
        
        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        propParam.getPU().getProperties().removeProperty2(propParam.getPU().getProperties().getProperty2(propsData.get(row).getModelIndex()));

        // Needs to sync the local cache with the data model
        // since the index in the datamodel has changed
        getPropsData();

        fireTableRowsDeleted(row, row);
    }
    
    private void getPropsData() {
        propsData = new ArrayList<PropertyData>();
        
        // Get all the properties in this catetory
        List<String> allPropsOfThisCat = Util.getPropsNamesExceptGeneral(propParam.getProvider());
      
        if (propParam.getPU() != null && propParam.getPU().getProperties()!=null) {
            for (int i = 0; i < propParam.getPU().getProperties().sizeProperty2(); i++) {

                String propName = propParam.getPU().getProperties().getProperty2(i).getName();
                if (allPropsOfThisCat.contains(propName) ||
                        allPropsOfThisCat.contains(jpa_prefix + propName)) {
                    String propValue = propParam.getPU().getProperties().getProperty2(i).getValue();
                    propsData.add( new PropertyData(i, propName, propValue ) );
                }
            }
        }
    }

    // A class to encapsulate the property data.
    private static class PropertyData {
        private int modelIndex;
        private String name;
        private String value;
        
        public PropertyData( int modelIndex, String propName, String propValue ) {
            this.modelIndex = modelIndex;
            this.name = propName;
            this.value = propValue;
        }
        
        public int getModelIndex() {
            return this.modelIndex;
        }
        
        public String getPropName() {
            return this.name;
        }
        
        public String getPropValue() {
            return this.value;
        }
        
        public void setPropValue( String value ) {
            this.value = value;
        }
    }
}
