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

import javax.swing.tree.*;

import org.openide.ErrorManager;
import java.net.URLClassLoader;

/**
 *
 * @author David Botterill
 */
public class HolderTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    
    /** Creates a new instance of HolderTypeTreeNode */
    public HolderTypeTreeNode(TypeNodeData userObject,URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
        
    }
    /**
     * This method will use the parameter name of the child to update the value of this Holder.
     * @param inData - the TypeNodeData of the child that called this method.
     */
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new Holder from the child values.
         */
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(0);
        TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
        Object holderValue = childData.getTypeValue();
        if(holderValue != null) {
            Object holder = ((TypeNodeData)this.getUserObject()).getTypeValue();
            try {
                ReflectionHelper.setHolderValue(holder, holderValue);
            }catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
        }
    }
    
    /**
     * Update the child nodes based on the value of this UserObject.
     * Fix for Bug: 5059732
     * - David Botterill 8/12/2004
     *
     */
    
    public void updateChildren() {
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(0);
        TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
        Object heldValue = null;
        Object userObject = this.getUserObject();
        if(null != userObject) {
            Object holder = ((TypeNodeData)userObject).getTypeValue();
            try {
                heldValue = ReflectionHelper.getHolderValue(holder);
            } catch(Exception wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() +
                ": Error trying to get the held value on Holder: " +((TypeNodeData)userObject).getRealTypeName() + "WebServiceReflectionException=" + cause);
                
            }
            /**
             * set the value of the child node.
             */
            childData.setTypeValue(heldValue);
            childNode.setUserObject(childData);
            
            
            /**
             * See if we need to continue to update children nodes.
             */
            if (childNode instanceof ParameterTreeNode) {
                ((ParameterTreeNode)childNode).updateChildren();
            }
        }
        
    }
    
}
