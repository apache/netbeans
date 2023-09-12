/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.spi.debugger;

import java.util.HashSet;
import java.util.Vector;


/**
 * Support for {@link ActionsProvider} implementation. You should implement
 * {@link #doAction} and {@link #getActions} only, and call {@link #setEnabled}
 * when the action state is changed.
 *
 * @author   Jan Jancura
 */
public abstract class ActionsProviderSupport extends ActionsProvider {

    private HashSet enabled = new HashSet ();
    private Vector<ActionsProviderListener> listeners = new Vector<>();


    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public abstract void doAction (Object action);
    
    /**
     * Returns a state of given action defined by {@link #setEnabled} 
     * method call.
     *
     * Do not override. Should be final - the enabled state is cached,
     * therefore this method is not consulted unless the state change
     * is fired.
     *
     * @param action action
     */
    public boolean isEnabled (Object action) {
        return enabled.contains (action);
    }
    
    /**
     * Sets state of enabled property.
     *
     * @param action action whose state should be changed
     * @param enabled the new state
     */
    protected final void setEnabled (Object action, boolean enabled) {
        boolean fire;
        if (enabled)
            fire = this.enabled.add (action);
        else
            fire = this.enabled.remove (action);
        if (fire)
            fireActionStateChanged (action, enabled);
    }
    
    /**
     * Fires a change of action state.
     *
     * @param action action whose state has been changed
     * @param enabled the new state
     */
    protected void fireActionStateChanged (Object action, boolean enabled) {
        Vector<ActionsProviderListener> v = new Vector<>(listeners);
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            v.elementAt (i).actionStateChange (
                action, enabled
            );
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public final void addActionsProviderListener (ActionsProviderListener l) {
        listeners.addElement (l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public final void removeActionsProviderListener (ActionsProviderListener l) {
        listeners.removeElement (l);
    }
}

