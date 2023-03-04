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
/*
 * ReusablePropertyModel.java
 *
 * Created on February 6, 2003, 5:12 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.beans.PropertyEditor;

import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;


/** A reconfigurable property model for use by the rendering
 *  infrastructure, to avoid allocating memory while painting.
 *  Contains two static fields, PROPERTY and NODE which
 *  set the node and property this model acts as an interface to.<P>
 *  This class is <i>not thread safe</i>.  It assumes that it will
 *  only be called from the AWT thread, since it is used in painting
 *  infrastructure.  If property misrendering occurs, run NetBeans
 *  with the argument <code>-J-Dnetbeans.reusable.strictthreads=true</code>
 *  and exceptions will be thrown if any method is called from off the
 *  AWT thread.
 *
 * @author  Tim Boudreau
 */
class ReusablePropertyModel implements ExPropertyModel {
    static final boolean DEBUG = Boolean.getBoolean("netbeans.reusable.strictthreads");
    private transient Property PROPERTY = null;
    private final ReusablePropertyEnv env;

    /** Creates a new instance of ReusablePropertyModel */
    public ReusablePropertyModel(ReusablePropertyEnv env) {
        this.env = env;
        env.setReusablePropertyModel(this);
    }

    void clear() {
        PROPERTY = null;
    }

    /** Does nothing - if a property changes, the sheet will get notification
     *  and the model will be reconfigured with the new value and re-rendered */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    /** Does nothing - if a property changes, the sheet will get notification
     *  and the model will be reconfigured with the new value and re-rendered */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    public PropertyEditor getPropertyEditor() {
        Node.Property p = getProperty();

        // #52179: PropUtils.isExternallyEdited(p) - don't affect just 
        // externally edited properties or their current changes will be lost 
        // due to the firing PropertyChangeEvents to theirs UI counterpart
        return PropUtils.getPropertyEditor(p, !PropUtils.isExternallyEdited(p));
    }

    public Class getPropertyEditorClass() {
        if (DEBUG) {
            checkThread();
        }

        return getPropertyEditor().getClass();
    }

    public Class getPropertyType() {
        if (DEBUG) {
            checkThread();
        }

        return null == getProperty() ? Object.class : getProperty().getValueType();
    }

    public Object getValue() throws java.lang.reflect.InvocationTargetException {
        if (DEBUG) {
            checkThread();
        }

        try {
            return getProperty().getValue();
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }

        return null;
    }

    public void setValue(Object v) throws java.lang.reflect.InvocationTargetException {
        if (DEBUG) {
            checkThread();
        }

        try {
            getProperty().setValue(v);
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }
    }

    public Object[] getBeans() {
        if (DEBUG) {
            checkThread();
        }

        if (env.getNode() instanceof ProxyNode) {
            return ((ProxyNode) env.getNode()).getOriginalNodes();
        } else {
            return new Object[] { env.getNode() };
        }
    }

    public java.beans.FeatureDescriptor getFeatureDescriptor() {
        if (DEBUG) {
            checkThread();
        }

        return getProperty();
    }

    /** Ensure we're really running on the AWT thread, otherwise bad things can
     *  happen.  */
    static void checkThread() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("Reusable property model accessed from off the AWT thread.");
        }
    }

    public Node.Property getProperty() {
        return PROPERTY;
    }

    public void setProperty(Node.Property PROPERTY) {
        this.PROPERTY = PROPERTY;
    }
}
