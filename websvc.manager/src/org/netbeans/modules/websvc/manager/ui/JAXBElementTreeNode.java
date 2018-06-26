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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.ui;

import java.net.URLClassLoader;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author quynguyen
 */
public class JAXBElementTreeNode extends AbstractParameterTreeNode {

    private URLClassLoader urlClassLoader;

    /** Creates a new instance of HolderTypeTreeNode */
    public JAXBElementTreeNode(TypeNodeData userObject, URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
    }

    /**
     * This method will use the parameter name of the child to update the value of this element
     * @param inData - the TypeNodeData of the child that called this method.
     */
    public void updateValueFromChildren(TypeNodeData inData) {
        
        DefaultMutableTreeNode localPartNode = (DefaultMutableTreeNode) this.getChildAt(0);
        DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode)this.getChildAt(1);
        
        //TypeNodeData localPartData = (TypeNodeData) localPartNode.getUserObject();
        TypeNodeData valueData = (TypeNodeData) valueNode.getUserObject();
        
        Object value = valueData.getTypeValue();
        String localPart = (String)localPartNode.getUserObject();
        if (value != null && localPart != null) {
            Object jaxBElement = ((TypeNodeData) this.getUserObject()).getTypeValue();
            try {
                if (jaxBElement != null) {
                    ReflectionHelper.setJAXBElementValue(jaxBElement, value);
                }else {
                    jaxBElement = ReflectionHelper.makeJAXBElement(valueData.getTypeClass(), localPart, value, urlClassLoader);
                    this.setUserObject(jaxBElement);
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    /**
     * Update the child nodes based on the value of this UserObject.
     *
     */
    public void updateChildren() {
        DefaultMutableTreeNode localPartNode = (DefaultMutableTreeNode) this.getChildAt(0);
        DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode)this.getChildAt(1);
        
        TypeNodeData localPartData = (TypeNodeData)localPartNode.getUserObject();
        TypeNodeData valueData = (TypeNodeData) valueNode.getUserObject();
        
        Object jaxBElement = this.getUserObject();
        if (null != jaxBElement) {
            //Object holder = ((TypeNodeData) jaxBElement).getTypeValue();
            try {
                Object value = ReflectionHelper.getJAXBElementValue(jaxBElement);
                String localPart = ReflectionHelper.getQNameLocalPart(jaxBElement);
                
                localPartData.setTypeValue(localPart);
                localPartNode.setUserObject(localPartData);
                
                valueData.setTypeValue(value);
                valueNode.setUserObject(valueData);
            } catch (Exception wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() + ": Error trying to get values from JAXBElement: " + ((TypeNodeData) userObject).getRealTypeName() + "WebServiceReflectionException=" + cause);
                return;
            }
            



            /**
             * See if we need to continue to update children nodes.
             */
            if (valueNode instanceof ListTypeTreeNode || valueNode instanceof StructureTypeTreeNode) {
                ((ParameterTreeNode) valueNode).updateChildren();
            }
        }
    }
}
