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
