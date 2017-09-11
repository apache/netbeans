/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.openide.explorer.propertysheet;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditor;
import org.netbeans.modules.openide.explorer.NodeOperationImpl;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 * Provides access to package private classes around CustomEditorAction so
 * the NodeOperationImpl (in a different package) can invoke a custom editor
 * dialog for given property.
 * 
 * @author Tomas Pavek
 */
final class CustomEditorAccessorImpl implements NodeOperationImpl.CustomEditorAccessor {

    static void register() {
        NodeOperationImpl.registerCustomEditorAccessor(new CustomEditorAccessorImpl());
    }

    private CustomEditorAccessorImpl() {}

    /**
     * Shows a modal dialog with the custom editor of given property the same
     * way as property sheet, just like
     * it would be invoked when clicking the [...] button next to a property in
     * the property sheet.
     */
    @Override
    public void showDialog(Property property, Object[] beans) {
        new CustomEditorAction(new Invoker(property, beans))
            .actionPerformed(new ActionEvent(property, ActionEvent.ACTION_PERFORMED, "invokeCustomEditor")); // NOI18N
    }

    private static class Invoker implements CustomEditorAction.Invoker {
        private Node.Property property;
        private Object[] beans;
        private ReusablePropertyEnv propertyEnv;

        Invoker(Node.Property prop, Object[] beans) {
            property = prop;
            this.beans = beans;
            propertyEnv = new ReusablePropertyEnv();
            ReusablePropertyModel rpm = new ReusablePropertyModel(propertyEnv);
            rpm.setProperty(prop);
            propertyEnv.setNode(beans); // will unwrap as needed
        }

        @Override
        public FeatureDescriptor getSelection() {
            return property;
        }

        @Override
        public Object getPartialValue() {
            return null;
        }

        @Override
        public Component getCursorChangeComponent() {
            return null;
        }

        @Override
        public String getBeanName() {
            if (beans instanceof Node[]) {
                Node[] nodes = (Node[]) beans;
                StringBuilder name = new StringBuilder();
                String delim = NbBundle.getMessage(ProxyNode.class, "CTL_List_Delimiter"); // NOI18N
                for (int i=0; i < nodes.length; i++) {
                    name.append(nodes[i].getDisplayName());
                    if (i < nodes.length - 1) {
                        name.append(delim);
                        if (i >= 2) {
                            name.append(NbBundle.getMessage(ProxyNode.class, "MSG_ELLIPSIS")); // NOI18N
                            break;
                        }
                    }
                }
                return name.toString();
            }
            return null;
        }

        @Override
        public void editorOpening() {
        }

        @Override
        public void editorOpened() {
        }

        @Override
        public void editorClosed() {
        }

        @Override
        public void valueChanged(PropertyEditor editor) {
        }

        @Override
        public boolean allowInvoke() {
            return true;
        }

        @Override
        public void failed() {
        }

        @Override
        public boolean wantAllChanges() {
            return false; // do not set changed values to the property before the dialog closes
        }

        @Override
        public ReusablePropertyEnv getReusablePropertyEnv() {
            return propertyEnv;
        }
    }

}
