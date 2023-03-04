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

package org.netbeans.modules.form;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Action;

import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.util.actions.SystemAction;
import org.openide.loaders.DataObject;

/**
 * A common superclass for nodes used in Form Editor.
 *
 * @author Tomas Pavek
 */

public class FormNode extends AbstractNode implements FormCookie {

    private FormModel formModel;

    protected Action[] actions;

    protected FormNode(Children children, FormModel formModel) {
        super(children);
        this.formModel = formModel;
        getCookieSet().add(this);
    }

    // FormCookie implementation
    @Override
    public final FormModel getFormModel() {
        return formModel;
    }

    // FormCookie implementation
    @Override
    public final Node getOriginalNode() {
        return this;
    }

    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> type) {
        T cookie = super.getCookie(type);
        if (cookie == null
            && (DataObject.class.isAssignableFrom(type)
                || SaveCookie.class.isAssignableFrom(type)
                || CloseCookie.class.isAssignableFrom(type)
                || PrintCookie.class.isAssignableFrom(type)))
        {
            FormDataObject fdo = FormEditor.getFormDataObject(formModel);
            if (fdo != null)
                cookie = fdo.getCookie(type);
        }
        return cookie;
    }

    // because delegating cookies to FormDataObject we have a bit complicated
    // way of updating cookies on node - need fire a change on nodes explicitly
    void updateCookies() {
        super.fireCookieChange();
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        if (actions == null) {
            actions = new Action[] { SystemAction.get(PropertiesAction.class) };
        }
        return actions;
    }

    /**
     * A wrapper for the standard Properties action to ensure that standalone properties
     * windows opened by the user do not stay around after the form is closed.
     */
    private static class PropertiesAction extends org.openide.actions.PropertiesAction {
        @Override
        protected void performAction(Node[] nodes) {
            if (nodes != null) {
                FormEditor formEditor = null;
                for (Node n : nodes) {
                    if (n instanceof FormNode) {
                        FormNode fn = (FormNode) n;
                        if (formEditor == null) {
                            formEditor = FormEditor.getFormEditor(fn.getFormModel());
                        }
                        if (formEditor != null) {
                            formEditor.registerNodeWithPropertiesWindow(fn);
                        }
                    }
                }
            }
            super.performAction(nodes);
        }
    }

    @Override
    public Component getCustomizer() {
        Component customizer = createCustomizer();
        if (customizer instanceof Window) {
            // register the customizer window (probably a dialog) to be closed
            // automatically when the form is closed
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            if (formEditor != null) {
                Window customizerWindow = (Window) customizer;
                formEditor.registerFloatingWindow(customizerWindow);
                // attach a listener to unregister the window when it is closed
                customizerWindow.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        if (e.getSource() instanceof Window) {
                            Window window = (Window) e.getSource();
                            FormEditor formEditor = FormEditor.getFormEditor(formModel);
                            if (formEditor != null)
                                formEditor.unregisterFloatingWindow(window);
                            window.removeWindowListener(this);
                        }
                    }
                });
            }
        }
        return customizer;
    }

    // to be implemented in FormNode descendants (instead of getCustomizer)
    protected Component createCustomizer() {
        return null;
    }
    
    /** Provides access for firing property changes
     * 
     * @param name property name
     * @param oldValue old value of the property
     * @param newValue new value of the property
     */
    public void firePropertyChangeHelper(String name,
                                         Object oldValue, Object newValue) {
        super.firePropertyChange(name, oldValue, newValue);
    }

    void fireNodeDestroyedHelper() {
        fireNodeDestroyed();
    }

    // ----------
    // automatic children updates

    void updateChildren() {
        Children children = getChildren();
        if (children instanceof FormNodeChildren)
            ((FormNodeChildren)children).updateKeys();
    }

    // Special children class - to be implemented in FormNode descendants (if
    // they know their set of children nodes and can update them).
    protected abstract static class FormNodeChildren extends Children.Keys<Object> {
        protected void updateKeys() {}
    }

    // ----------
    // Persistence hacks - for the case the node is selected in some
    // (standalone) properties window when IDE exits. We don't restore the
    // original node after IDE restarts (would require to load the form), but
    // provide a fake node which destroys itself immediately - closing the
    // properties window. [Would be nice to find some better solution...]

    @Override
    public Node.Handle getHandle() {
        return new Handle();
    }

    static class Handle implements Node.Handle {
        static final long serialVersionUID = 1;
        @Override
        public Node getNode() throws java.io.IOException {
            return new ClosingNode();
        }
    }

    static class ClosingNode extends AbstractNode implements Runnable {
        ClosingNode() {
            super(Children.LEAF);
        }
        @Override
        public String getName() {
            java.awt.EventQueue.invokeLater(this);
            return super.getName();
        }
        @Override
        public Node.PropertySet[] getPropertySets() {
            java.awt.EventQueue.invokeLater(this);
            return super.getPropertySets();
        }
        @Override
        public void run() {
            this.fireNodeDestroyed();
        }
    }
}
