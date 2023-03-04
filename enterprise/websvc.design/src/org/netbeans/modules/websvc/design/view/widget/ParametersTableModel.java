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

package org.netbeans.modules.websvc.design.view.widget;

import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ParamModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit
 */
public class ParametersTableModel implements TableModel<ParamModel>{
    
    private transient MethodModel method;
    private transient boolean nameEditable;
    
    /**
     *
     * @param method
     */
    public ParametersTableModel(MethodModel method, boolean nameEditable) {
        this.method = method;
        this.nameEditable = nameEditable;
    }
    
    public int getRowCount() {
        return method.getParams().size();
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public String getColumnName(int columnIndex) {
        switch(columnIndex) {
        case 0:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Name");
        case 1:
            return NbBundle.getMessage(ParametersTableModel.class, "LBL_Parameter_Type");
        default:
            throw new IllegalArgumentException("");
        }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return nameEditable;
        case 1:
            return false;
        default:
            return false;
        }
    }
    
    public String getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            switch(columnIndex) {
            case 0:
                return getUserObject(rowIndex).getName();
            case 1:
                return getUserObject(rowIndex).getParamType();
            default:
                throw new IllegalArgumentException("");
            }
        }
        return null;
    }
    
    public void setValueAt(String aValue, int rowIndex, int columnIndex) {
        if (rowIndex>=0 && rowIndex<getRowCount()) {
            switch(columnIndex) {
            case 0:
                //validate aValue
                getUserObject(rowIndex).setName(aValue);
                break;
            case 1:
                throw new IllegalArgumentException("");
            default:
                throw new IllegalArgumentException("");
            }
        }
    }

    public ParamModel getUserObject(int rowIndex) {
        return method.getParams().get(rowIndex);
    }
    
}
