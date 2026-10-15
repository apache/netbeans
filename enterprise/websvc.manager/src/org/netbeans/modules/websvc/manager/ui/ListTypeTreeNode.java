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

import java.util.Collection;
import javax.swing.tree.*;
import java.util.Iterator;

import java.net.URLClassLoader;

/**
 * Node for Collection and List types
 * 
 * @author  quynguyen
 */
public class ListTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    
    public ListTypeTreeNode(TypeNodeData userObject,URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
        
    }
    
    
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new ArrayList from all of the child values.
         */
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        
        Collection c = (Collection)data.getTypeValue();
        if (c == null) return;
        
        c.clear();
        for(int ii=0; ii < this.getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(ii);
            TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
            if(null != childData.getTypeValue()) {
                c.add(childData.getTypeValue());
            }
        }
        
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getParent();
        if (parentNode instanceof ParameterTreeNode) {
            ((ParameterTreeNode)parentNode).updateValueFromChildren(data);
        }
    }
    /**
     * Update the child nodes based on the value of this UserObject.
     * Fix for Bug: 5059732
     * - David Botterill 8/12/2004
     *
     */
    public void updateChildren() {
        TypeNodeData thisData = (TypeNodeData)this.getUserObject();
        /**
         * First get the Collection for this node.
         */
        Collection childCollection = (Collection)thisData.getTypeValue();
        /**
         * Next we need to delete all of the child nodes
         */
        this.removeAllChildren();
        /**
         * For each entry in the array, make a child node
         */
        String structureType = thisData.getGenericType();
        if (structureType == null || structureType.length() == 0) {
            structureType = "java.lang.Object"; // NOI18N
        }
        
        Iterator iter = childCollection.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            TypeNodeData data = ReflectionHelper.createTypeData(structureType, "[" + i + "]", iter.next());
            data.setAssignable(thisData.isAssignable());
            if (ReflectionHelper.isComplexType(data.getTypeClass(), urlClassLoader)) {
                StructureTypeTreeNode childNode = new StructureTypeTreeNode(data,urlClassLoader);
                childNode.updateChildren();
                this.add(childNode);
            }else if (ReflectionHelper.isCollection(data.getTypeClass(), urlClassLoader)) {
                ListTypeTreeNode childNode = new ListTypeTreeNode(data,urlClassLoader);
                childNode.updateChildren();
                this.add(childNode);
            }else {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(data);
                this.add(childNode);
            }
        }
    }
    
}
