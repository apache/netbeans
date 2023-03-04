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

import java.beans.*;


/** A subclass of PropertyEnv that can be reused by the rendering infrastructure.
 *  All methods for attaching listeners are no-ops:  A renderer will only be
 *  momentarily attached to a given property, and property changes will result
 *  the property being rerendered (and the ReusablePropertyEnv being
 *  reconfigured correctly).<P>
 *  This class is <i>not thread safe</i>.  It assumes that it will
 *  only be called from the AWT thread, since it is used in painting
 *  infrastructure.  If property misrendering occurs, run NetBeans
 *  with the argument <code>-J-Dnetbeans.reusable.strictthreads=true</code>
 *  and exceptions will be thrown if it is called from off the
 *  AWT thread.
 *  <P>Note, the use of this class may be non-obvious at first - the value of
 *  <code>NODE</code> is set in the rendering loop, by the SheetTable instance,
 *  which knows about the nodes (other classes in the package should only
 *  be interested in the properties they represnt).  The instance is actually
 *  used in <code>PropertyEditorBridgeEditor.setPropertyEditor()</code>, but
 *  must rely on the table to configure it.
 * @author  Tim Boudreau
 */
final class ReusablePropertyEnv extends PropertyEnv {
    private Object NODE = null;
    private ReusablePropertyModel mdl;

    /** Creates a new instance of ReusablePropertyEnv */
    public ReusablePropertyEnv() {
        mdl = new ReusablePropertyModel(this);
    }

    public ReusablePropertyModel getReusablePropertyModel() {
        return mdl;
    }

    void clear() {
        NODE = null;

        if (mdl != null) {
            mdl.clear();
        }
    }

    void setReusablePropertyModel(ReusablePropertyModel mdl) {
        if (mdl == null) {
            throw new NullPointerException();
        }
        this.mdl = mdl;
    }

    /** Uses the <code>NODE</code> field to supply the beans - if it is an instance
     *  of ProxyNode (multi-selection), returns the nodes that ProxyNode represents. */
    public Object[] getBeans() {
        if (ReusablePropertyModel.DEBUG) {
            ReusablePropertyModel.checkThread();
        }

        if (getNode() instanceof ProxyNode) {
            return ((ProxyNode) getNode()).getOriginalNodes();
        } else if (getNode() instanceof Object[]) {
            return (Object[]) getNode();
        } else {
            return new Object[] { getNode() };
        }
    }

    public FeatureDescriptor getFeatureDescriptor() {
        return mdl.getProperty();
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removeVetoableChangeListener(VetoableChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    public boolean isEditable() {
        boolean result;

        if (mdl.getProperty() != null) {
            result = mdl.getProperty().canWrite();
        } else {
            result = true;
        }

        return result;
    }

    public void reset() {
        setEditable(true);
        setState(STATE_NEEDS_VALIDATION);
    }

    public Object getNode() {
        return NODE;
    }

    public void setNode(Object NODE) {
        this.NODE = NODE;
    }
}
