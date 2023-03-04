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
