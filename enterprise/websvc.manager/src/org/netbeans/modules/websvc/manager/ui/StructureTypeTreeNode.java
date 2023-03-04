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

import java.net.URLClassLoader;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author  David Botterill
 */
public class StructureTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    
    public StructureTypeTreeNode(TypeNodeData userObject,URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
        
    }
    
    /**
     * This method will use the parameter name of the child to update the value of this Structure.
     * @param inData - the TypeNodeData of the child that called this method.
     */
    public void updateValueFromChildren(TypeNodeData inData) {
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        try {
            Object structObject = data.getTypeValue();
            String propType = inData.getTypeClass();
            String propName = inData.getTypeName();
            Object propObject = inData.getTypeValue();
            
            ReflectionHelper.setPropertyValue(structObject, propName, propType, propObject, urlClassLoader);
        } catch(WebServiceReflectionException wsfe) {
            Throwable cause = wsfe.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() +
            ": Error trying to update Children of a Structure on: " + data.getRealTypeName() + "WebServiceReflectionException=" + cause);
            
        }
        /**
         * If this node is a member of a structure type, update it's parent.
         */
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
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        Object structObj = data.getTypeValue();
        if (structObj == null) return;
        
        try {
            String type = data.getTypeClass();
            List<String> fields = ReflectionHelper.getPropertyNames(type, urlClassLoader);
            
            for (int i = 0; i < fields.size(); i++) {
                String fieldName = fields.get(i);
                String fieldType = ReflectionHelper.getPropertyType(type, fieldName, urlClassLoader);
                Object newChildValue = ReflectionHelper.getPropertyValue(structObj, fieldName, urlClassLoader);
                
                
                DefaultMutableTreeNode currentChildNode = null;
                if (i >= this.getChildCount()) {
                    TypeNodeData childData = ReflectionHelper.createTypeData(fieldType, fieldName);
                    Object defaultChildValue = NodeHelper.getInstance().getParameterDefaultValue(childData);
                    childData.setTypeValue(defaultChildValue);
                    
                    currentChildNode = NodeHelper.getInstance().createNodeFromData(childData);
                    this.add(currentChildNode);
                    
                }else {
                    currentChildNode = (DefaultMutableTreeNode)this.getChildAt(i);
                }
                
                TypeNodeData childData = (TypeNodeData)currentChildNode.getUserObject();
                
                childData.setTypeValue(newChildValue);
                currentChildNode.setUserObject(childData);
                
                if (currentChildNode instanceof ParameterTreeNode) {
                    ((ParameterTreeNode)currentChildNode).updateChildren();
                }
            }
        }catch (WebServiceReflectionException wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() +
                ": Error trying to update Children of a Structure on: " + data.getRealTypeName() + "WebServiceReflectionException=" + cause);            
        }
        
    }
}
