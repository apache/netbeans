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

package org.netbeans.api.debugger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract definition of watch. Each watch is created for
 * one String which contains the name of variable or some expression.
 *
 * @author   Jan Jancura
 */
public final class Watch {

    /** Name of the property for the watched expression. */
    public static final String PROP_EXPRESSION = "expression"; // NOI18N
    /** Name of the property for the value of the watched expression. This constant is not used at all. */
    public static final String PROP_VALUE = "value"; // NOI18N
    /** Name of the property for the enabled status of the watch.
     * @since 1.36 */
    public static final String PROP_ENABLED = "enabled"; // NOI18N

    private String          expression;
    private boolean         enabled = true;
    private PropertyChangeSupport pcs;
    
    private final Pin pin;
    
    Watch (String expr) {
        this(expr, null);
    }
    
    Watch (String expr, Pin pin) {
        this.expression = expr;
        this.pin = pin;
        pcs = new PropertyChangeSupport (this);
    }
    
    /**
     * Test whether the watch is enabled.
     *
     * @return <code>true</code> if the watch is enabled,
     *         <code>false</code> otherwise.
     * @since 1.36
     */
    public synchronized boolean isEnabled () {
        return enabled;
    }
    
    /**
     * Set enabled state of the watch.
     * @param enabled <code>true</code> if this watch should be enabled,
     *                <code>false</code> otherwise
     * @since 1.36
     */
    public void setEnabled(boolean enabled) {
        synchronized(this) {
            if (enabled == this.enabled) return ;
            this.enabled = enabled;
        }
        pcs.firePropertyChange (PROP_ENABLED, !enabled, enabled);
    }
    
    /**
     * Return expression this watch is created for.
     *
     * @return expression this watch is created for
     */
    public synchronized String getExpression () {
        return expression;
    }

    /** 
     * Set the expression to watch.
     *
     * @param expression expression to watch
     */
    public void setExpression (String expression) {
        String old;
        synchronized(this) {
            old = this.expression;
            this.expression = expression;
        }
        pcs.firePropertyChange (PROP_EXPRESSION, old, expression);
    }

    /**
     * Get a pin location, where the watch is pinned at, if any.
     * @return The watch pin, or <code>null</code>.
     * @since 1.54
     */
    public Pin getPin() {
        return pin;
    }

    /**
     * Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        dm.removeWatch (this);
    }

    /**
     * Add a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
     * Remove a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    /**
     * A base interface for a watch pin location. Implemented by specific
     * platform-dependent and location-dependent implementation.
     * See <code>org.netbeans.spi.debugger.ui.EditorPin</code> for the NetBeans
     * editor pin implementation.
     * @since 1.54
     */
    public static interface Pin {
        
    }
}

