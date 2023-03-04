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
package org.netbeans.modules.web.inspect.webkit.knockout;

import javax.swing.Action;
import org.netbeans.modules.web.webkit.debugging.api.debugger.PropertyDescriptor;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 * Node that represents some JavaScript value in Knockout view.
 *
 * @author Jan Stola
 */
public class KnockoutNode extends AbstractNode {
    /** Request processor for this class. */
    private static final RequestProcessor RP = new RequestProcessor(KnockoutNode.class);
    /** {@code RemoteObject} wrapped by this node. */
    private final RemoteObject remoteObject;
    /** Value property of this node. */
    private final ValueProperty valueProperty;
    /** Factory for children of this node. */
    KnockoutChildFactory childFactory;

    /**
     * Creates a new {@code KnockoutNode} that wraps the given {@code RemoteObject}.
     * 
     * @param name name of the node.
     * @param remoteObject {@code RemoteObject} to encapsulate.
     * @param childFactory factory for children of the node (or {@code null}
     * when the node should have no children).
     */
    private KnockoutNode(String name, RemoteObject remoteObject, KnockoutChildFactory childFactory) {
        super(childFactory == null ? Children.LEAF : Children.create(childFactory, true));
        this.childFactory = childFactory;
        this.remoteObject = remoteObject;
        this.valueProperty = new ValueProperty(valueFor(remoteObject));
        setName(name);
        unwrap();
    }

    /**
     * Creates a new {@code KnockoutNode} that wraps the given {@code RemoteObject}.
     * 
     * @param name name of the node.
     * @param remoteObject {@code RemoteObject} to encapsulate.
     */
    KnockoutNode(String name, RemoteObject remoteObject) {
        this(name, remoteObject, childFactoryFor(remoteObject));
    }

    /**
     * Returns child factory for the given {@code RemoteObject}.
     * 
     * @param remoteObject remote object for which a child factory should be returned.
     * @return {@code KnockoutChildFactory} (possibly {@code null})
     * for the given {@code RemoteObject}
     */
    static final KnockoutChildFactory childFactoryFor(RemoteObject remoteObject) {
        boolean isLeaf = true;
        if (remoteObject != null) {
            RemoteObject.Type type = remoteObject.getType();
            if (type == RemoteObject.Type.OBJECT && remoteObject.getDescription() != null) {
                isLeaf = false;
            }
        }
        return isLeaf ? null : new KnockoutChildFactory(remoteObject);
    }

    /**
     * Script used to unwrap Knockout observables.
     */
    final String UNWRAP_SCRIPT = "function() {var ko = NetBeans.getKnockout(); return [ko.isObservable(this), ko.utils.unwrapObservable(this)]}"; // NOI18N
    
    /**
     * Attempts to unwrap the value represented by this node.
     */
    final void unwrap() {
        if (remoteObject != null && remoteObject.getType() == RemoteObject.Type.FUNCTION) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    RemoteObject observableInfo = remoteObject.apply(UNWRAP_SCRIPT);
                    boolean isObservable = false;
                    RemoteObject observable = null;
                    for (PropertyDescriptor descriptor : observableInfo.getProperties()) {
                        String name = descriptor.getName();
                        if ("0".equals(name)) { // NOI18N
                            isObservable = "true".equals(descriptor.getValue().getValueAsString()); // NOI18N
                        } else if ("1".equals(name)) { // NOI18N
                            observable = descriptor.getValue();
                        }
                    }
                    if (isObservable) {
                        childFactory = childFactoryFor(observable);
                        setChildren(childFactory == null ? Children.LEAF : Children.create(childFactory, true));
                        setName(getName()+"()"); // NOI18N
                        valueProperty.setValueInternal(valueFor(observable));
                        firePropertyChange(ValueProperty.NAME, null, null);
                    }
                }
            });
        }
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = Sheet.createPropertiesSet();
        set.put(valueProperty);
        sheet.put(set);
        return sheet;
    }

    /**
     * Refreshes the children of this node.
     */
    void refresh() {
        if (childFactory != null) {
            childFactory.refresh();
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RefreshAction.class)
        };
    }

    /**
     * Returns {@code String} value of the given {@code RemoteObject}.
     * 
     * @param remoteObject object whose value should be returned.
     * @return {@code String} value of the given {@code RemoteObject}.
     */
    private static String valueFor(RemoteObject remoteObject) {
        String value;
        if (remoteObject == null) {
            value = null;
        } else if (remoteObject.getType() == RemoteObject.Type.UNDEFINED) {
            value = NbBundle.getMessage(KnockoutNode.class, "KnockoutNode.valueUndefined"); // NOI18N
        } else {
            value = remoteObject.getDescription();
            if (value == null && remoteObject.getType() != RemoteObject.Type.OBJECT) {
                value = remoteObject.getValueAsString();
            }
        }
        return value;
    }

    /**
     * Value property.
     */
    static final class ValueProperty extends PropertySupport.ReadOnly<String> {
        /** Name of the property. */
        static final String NAME = "value"; // NOI18N
        /** Display name of the property. */
        private static final String DISPLAY_NAME = NbBundle.getMessage(
                ValueProperty.class, "KnockoutNode.valueProperty.displayName"); // NOI18N
        /** Description of the property. */
        private static final String DESCRIPTION = NbBundle.getMessage(
                ValueProperty.class, "KnockoutNode.valueProperty.description"); // NOI18N
        /** Value of the property. */
        private String value;

        /**
         * Creates a new {@code ValueProperty}.
         * 
         * @param value value of the property.
         */
        ValueProperty(String value) {
            super(NAME, String.class, DISPLAY_NAME, DESCRIPTION);
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of this property.
         * 
         * @param value new value of this property.
         */
        void setValueInternal(String value) {
            this.value = value;
        }
        
    }
    
}
