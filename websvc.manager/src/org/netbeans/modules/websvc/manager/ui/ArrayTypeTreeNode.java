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
        if (parentNode != null && parentNode instanceof ParameterTreeNode) {
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
