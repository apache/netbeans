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
