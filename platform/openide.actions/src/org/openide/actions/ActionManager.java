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
package org.openide.actions;

import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

import java.awt.event.ActionEvent;

import java.beans.*;

import javax.swing.Action;


/** Collects access methods to implementation depended functionality
* for actions package.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public abstract class ActionManager extends Object {
    /** name of property that is fired when set of context actions
    * changes.
    */
    public static final String PROP_CONTEXT_ACTIONS = "contextActions"; // NOI18N

    /** Utility field used by event firing mechanism. */
    private PropertyChangeSupport supp = null;

    /**
     * Get the default action manager from lookup.
     * @return some default instance
     * @since 4.2
     */
    public static ActionManager getDefault() {
        ActionManager am = Lookup.getDefault().lookup(ActionManager.class);

        if (am == null) {
            am = new Trivial();
        }

        return am;
    }

    /** Get all registered actions that should be displayed
    * by tools action.
    * Can contain <code>null</code>s that will be replaced by separators.
    *
    * @return array of actions
    */
    public abstract SystemAction[] getContextActions();

    /** Invokes action in a RequestPrecessor dedicated to performing
     * actions.
     * Nonabstract since 4.11.
     * @deprecated Just use {@link java.awt.event.ActionListener#actionPerformed} directly instead. Since 4.11.
     */
    @Deprecated
    public void invokeAction(Action a, ActionEvent e) {
        a.actionPerformed(e);
    }

    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        if (supp == null) {
            supp = new PropertyChangeSupport(this);
        }

        supp.addPropertyChangeListener(listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (supp != null) {
            supp.removePropertyChangeListener(listener);
        }
    }

    /** Notifies all registered listeners about the event.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected final void firePropertyChange(String name, Object o, Object n) {
        if (supp != null) {
            supp.firePropertyChange(name, o, n);
        }
    }

    /**
     * Trivial impl.
     * @see "#32092"
     */
    private static final class Trivial extends ActionManager {
        public SystemAction[] getContextActions() {
            return new SystemAction[0];
        }
    }
}
