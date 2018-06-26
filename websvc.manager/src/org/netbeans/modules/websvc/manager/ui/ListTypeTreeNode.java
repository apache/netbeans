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
