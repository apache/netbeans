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


package org.netbeans.modules.websvc.manager.ui;

import org.netbeans.swing.outline.RowModel;
import org.openide.util.NbBundle;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author  David Botterill
 */
public class ResultRowModel implements RowModel {

    /** Creates a new instance of TypeRowModel */
    public ResultRowModel() {
    }

    public Class getColumnClass(int column) {
        switch(column) {
         //   case 0: return String.class;
            case 0: return Object.class;
            default: return String.class;
        }
    }

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        switch(column) {
            case 0: return NbBundle.getMessage(this.getClass(), "PARAM_VALUE");
            default: return "";
        }

    }

    public Object getValueFor(Object inNode, int column) {
        if(null == inNode) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return null;
        TypeNodeData data = (TypeNodeData)node.getUserObject();
        switch(column) {
            case 0: return data.getTypeValue();
            default: return "";
        }

    }

    public boolean isCellEditable(Object inNode, int column) {
        return true;
    }

    public void setValueFor(Object inNode, int column, Object value) {
        return;
    }
}
