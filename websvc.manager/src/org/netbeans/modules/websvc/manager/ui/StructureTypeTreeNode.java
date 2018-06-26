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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
        if (parentNode != null && parentNode instanceof ParameterTreeNode) {
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
