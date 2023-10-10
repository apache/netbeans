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

import java.util.ArrayList;
import java.net.URLClassLoader;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author  David Botterill
 */
public class ArrayTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    
    public ArrayTypeTreeNode(Object userObject,URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
        
    }
    
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new ArrayList from all of the child values.
         */
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        ArrayList<Object> newList = new ArrayList<Object>();
        
        for(int ii=0; ii < this.getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(ii);
            TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
            if(null != childData.getTypeValue()) {
                newList.add(childData.getTypeValue());
            }
        }
        Object[] arr = newList.toArray();
        
        data.setTypeValue(arr);
        
        // Update the parent node
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getParent();
        if (parentNode instanceof ParameterTreeNode) {
            ((ParameterTreeNode)parentNode).updateValueFromChildren(data);
        }
    }
    
    /**
     * Update the child nodes based on the value of this UserObject.
     *
     */
    public void updateChildren() {
        TypeNodeData thisData = (TypeNodeData)this.getUserObject();
        Object arrayObj = thisData.getTypeValue();
        
        this.removeAllChildren();
        //For each entry in the array, make a child node
        try {
            String genericType = thisData.getGenericType();
            int arrayLength = ReflectionHelper.getArrayLength(arrayObj);
            for (int i = 0; i < arrayLength; i++) {
                Object entry = ReflectionHelper.getArrayValue(arrayObj, i);
                TypeNodeData entryData = ReflectionHelper.createTypeData(genericType, "[" + i + "]", entry); // NOI18N
                
                DefaultMutableTreeNode entryNode = NodeHelper.getInstance().createNodeFromData(entryData);
                this.add(entryNode);
            }
        }catch (Exception ex) {
            Throwable cause = ex.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() + 
                    ": Error using reflection on array: " + thisData.getRealTypeName() + "WebServiceReflectionException=" + cause); // NOI18N
        }
    }
    
}
