/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
public class TypeRowModel implements RowModel {
    private ClassLoader classLoader;

    /** Creates a new instance of TypeRowModel */
    public TypeRowModel(ClassLoader loader) {
        this.classLoader = loader;
    }

    public Class getColumnClass(int column) {
        switch(column) {
         //   case 0: return String.class;
            case 0: return String.class;
            case 1: return Object.class;
            default: return String.class;
        }
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int column) {
        switch(column) {
           // case 0: return NbBundle.getMessage(this.getClass(), "PARAM_CLASS");
            case 0: return NbBundle.getMessage(this.getClass(), "PARAM_NAME");
            case 1: return NbBundle.getMessage(this.getClass(), "PARAM_VALUE");
            default: return "";
        }

    }

    public Object getValueFor(Object inNode, int column) {
        if(null == inNode) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return null;
        TypeNodeData data = (TypeNodeData)node.getUserObject();
        switch(column) {
            case 0: return data.getTypeName();
            case 1: return data.getTypeValue();
            default: return "";
        }

    }

    public boolean isCellEditable(Object inNode, int column) {
        if(null == inNode) return false;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return false;

        TypeNodeData data = (TypeNodeData)node.getUserObject();
        switch(column) {
            case 0: return false;
            case 1:
                if (!data.isAssignable()) {
                    return false;
                }

                String typeClass = data.getTypeClass();
                if (ReflectionHelper.isSimpleType(typeClass, classLoader)) {
                    if (typeClass.equalsIgnoreCase("java.util.Calendar")) { // NOI18N
                        return false;
                    }else {
                        return true;
                    }
                }else if (ReflectionHelper.isEnumeration(typeClass, classLoader)) {
                    return true;
                }else {
                    return false;
                }
            default: return false;
        }

    }

    public void setValueFor(Object inNode, int column, Object value) {
        if(null == inNode) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return;

        TypeNodeData data = (TypeNodeData)node.getUserObject();
        /**
         * Make sure they are only trying to edit the value column
         */
        if(column != 1) {
            return;
        }

        data.setTypeValue(value);
        /**
         * If this node's parent is a ArrayTypeTreeNode, StructureTypeTreeNode, or HolderTypeTreeNode,
         * update this value on the parent's
         * value.
         */

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
        if(parentNode instanceof ListTypeTreeNode) {
            ((ListTypeTreeNode)parentNode).updateValueFromChildren(data);
        } else if(parentNode instanceof StructureTypeTreeNode) {
            /**
             * This type should be a JavaStructureMember
             */
            ((StructureTypeTreeNode)parentNode).updateValueFromChildren(data);
        } else if(parentNode instanceof HolderTypeTreeNode) {

            ((HolderTypeTreeNode)parentNode).updateValueFromChildren(data);
        }




    }

}
