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

package org.netbeans.spi.actions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 * Base class for context aware actions.  Adds notification of first/last
 * listener attachment to ContextAwareAction,
 *
 * @author Tim Boudreau
 */
public abstract class NbAction implements ContextAwareAction {
    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    final Map <String, Object> pairs = Collections.synchronizedMap (new HashMap<String, Object>());
    private final Object STATE_LOCK = new Object();
    private volatile boolean attached;
    public static final String PROP_ENABLED = "enabled";

    boolean attached() {
        return attached;
    }

    Object lock() {
        return STATE_LOCK;
    }

    /**
     * Called when the first listener (such as a UI component) is added
     * to this action.  If your action depends on some external property
     * to compute its enabled state, override this method to add listeners.
     */
    protected void addNotify() {
        //do nothing
    }
    
    /**
     * Called when the last listener (such as a UI component) is removed
     * to this action.  If your action depends on some external property
     * to compute its enabled state, override this method to detach listeners.
     */
    protected void removeNotify() {
        //do nothing
    }

    void internalAddNotify() {
        addNotify();
    }

    void internalRemoveNotify() {
        removeNotify();
    }

    /**
     * Fire a property change
     * @param name The property name
     * @param old The old value
     * @param nue The new value
     */
    protected final void firePropertyChange(final String name, final Object old, final Object nue) {
        supp.firePropertyChange(name, old, nue);
    }

    interface ActionRunnable<T> {
        //A bit of ugliness to accomplish two things:
        // - Cannot have a common supertype for ActionStub + ContextAction
        //   without exporting it to clients
        // - Need to run some code on an uninitialized ActionStub or
        //   contextAction *as if* it were being listened to
        T run(NbAction a);
    }

    /**
     * Run an ActionRunnable against the passed action.  This method first
     * invokes addNotify() if necessary, and then removeNotify() if
     * addNotify() was called, before running the ActionRunnable.  This
     * ensure that the action's state is as if it were in use even if it
     * is not actually, so that correct values can be returned for an
     * action that is not in use.
     * @param <T> The return type
     * @param ar A runnable that will call something on the action
     * @param a The action
     * @return The return value of the ActionRunnable
     */
    <T> T runActive(ActionRunnable<T> ar, NbAction a) {
        boolean wasActive = a.attached();
        try {
            if (!wasActive) {
                a.internalAddNotify();
            }
            return ar.run(a);
        } finally {
            if (!wasActive) {
                a.internalRemoveNotify();
            }
        }
    }

    public Object getValue(String key) {
        return pairs.get(key);
    }

    public void putValue(String key, Object value) {
        Object old = pairs.put(key, value);
        boolean fire = (old == null) != (value == null);
        if (fire) {
            fire = (value != null && !value.equals(old)) ||
                    (old != null && old.equals(value));
            if (fire) {
                supp.firePropertyChange(key, old, value);
            }
        }
    }

    /**
     * setEnabled() is a mistake in the Swing Action API - it should not be
     * there.
     * Overridden to throw an exception.  Do not call.
     * @param b
     */
    public final void setEnabled(boolean b) {
        throw new UnsupportedOperationException("Illegal");
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        supp.addPropertyChangeListener(listener);
        if (supp.getPropertyChangeListeners().length == 1) {
            attached = true;
            internalAddNotify();
        }
    }

    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        supp.removePropertyChangeListener(listener);
        if (supp.getPropertyChangeListeners().length == 0) {
            attached = false;
            internalRemoveNotify();
        }
    }

    /**
     * Create an instance over a specific context, for subclasses which are
     * context-aware (their enabled state depends on the current selection).
     * Calls <code>internalCreateContextAwareInstance()</code> to ensure
     * that the instance returned is a subclass of NbAction, not just an
     * implementation of ContextAwareAction.
     *
     * @param actionContext A selection context
     * @return An action
     */
    public final Action createContextAwareInstance(Lookup actionContext) {
        return internalCreateContextAwareInstance(actionContext);
    }

    /**
     * Create a context sensitive instance of this action over a specific
     * action context.
     * The default implementation of this method return <code>this</code> (i.e.
     * no context sensitivity whatsoever).
     * It should be overridden if the subclass is actually context sensitive.
     * @param actionContext
     * @return this
     */
    protected NbAction internalCreateContextAwareInstance(Lookup actionContext) {
        return this;
    }
    /**
     * Create an action that merges several <code>ContextAction</code>s.  This
     * is useful, for example, if you want to create a global action which is enabled
     * if the user has, say, selected a Project, <i>or</i> if
     * the selected Node is owned by a Project.  Instead of writing one
     * action with complex enablement logic, you write one action which is
     * sensitive to Nodes (or DataObjects, or whatever) and one which is
     * directly sensitive to Projects.  Each has its own fairly simple
     * enablement logic.
     * <p/>
     * Since this action merges multiple actions, some rules apply as far
     * as which action gets called when and for what, in the case of display
     * names and enablement status.  This works as follows:
     * <ul>
     * <li>If one of the actions in the array is enabled
     *     <ul>
     *     <li>The returned ContextAwareAction is enabled</li>
     *     <li>The first enabled action in the array supplies the return
     *         values for calls to <code>getValue("someKey")</code> - i.e. the
     *         first enabled action controls the display name, icon, etc.
     *         If the first enabled action returns null from <code>getValue()</code>,
     *         the next enabled action is tried, and so forth, until there
     *         is a non-null result.  If there is no enabled action which
     *         returns non-null from <code>getValue()</code>, then the first
     *         non-null value returned by any action in the array, starting
     *         with the first, is used.
     *     </li>
     *     </ul>
     * </li>
     * </li>
     * </ul>
     *
     * @param actions An array of ContextActions.
     * @param exclusive If true, the resulting action will be <i>disabled</i> if
     * more than one of the passed actions is <i>enabled</i>.  This is sometimes
     * useful, for example, if performing the same action over very disparate
     * types of object (say, closing both a file and a project) would be
     * non-intuitive.
     * @return An action.
     */
    public static NbAction merge (boolean exclusive, NbAction... actions) {
        return new MergeAction(actions, exclusive);
    }

    /**
     * Same as merge (actions, false);
     *
     * @param actions An array of context actions
     * @return An action which merges the passed actions
     */
    public static NbAction merge (NbAction... actions) {
        return new MergeAction(actions);
    }
}
