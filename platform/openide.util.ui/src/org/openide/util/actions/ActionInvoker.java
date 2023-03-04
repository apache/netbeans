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

package org.openide.util.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;

/** A mixture of a utility class allowing to invoke actions and also a
 * <a href="http://wiki.apidesign.org/wiki/CodeInjection">
 * code injection mechanism</a> to allow overall system to be aware of invoked
 * actions.
 * <p>
 * Callers shall use the {@link #invokeAction(javax.swing.Action, java.awt.event.ActionEvent, boolean, java.lang.Runnable)}
 * method.
 * <p>
 * Implementors register an implementation of this class via {@link ServiceProvider}
 * annotation.
 *
 * @since 8.1
 */
public abstract class ActionInvoker extends Object {
    /** thread to run actions in */
    private static final RequestProcessor RP = new RequestProcessor("Module-Actions", Integer.MAX_VALUE); // NOI18N
    
    /** Subclass constructor. */
    protected ActionInvoker() {}
    
    /** An infrastructure method that handles invocation of an an action.
     * @param action the action to invoke
     * @param ev the event used during invocation
     */
    protected abstract void invokeAction(Action action, ActionEvent ev);

    /** Invokes the action in the currently registered ActionsBridge.
     *
     * @param action the action that is to be invoked
     * @param ev the event used to invoke the action
     * @param asynchronous shall the execution be performed in a background thread?
     * @param invoker the actual code that shall be performed to "run" the action. If null, action.actionPerformed(ev) will be called
     */
    public static void invokeAction(Action action, ActionEvent ev, boolean asynchronous, final Runnable invoker) {
        ActionRunnable r = new ActionRunnable(ev, action, asynchronous) {
            @Override
            protected void run() {
                if (invoker == null) {
                    action.actionPerformed(ev);
                } else {
                    invoker.run();
                }
            }
        };
        doPerformAction(action, r);
    }

    private static void doPerformAction(Action action, final ActionInvoker.ActionRunnable r) {
        assert java.awt.EventQueue.isDispatchThread() : "Action " + action.getClass().getName() +
        " may not be invoked from the thread " + Thread.currentThread().getName() +
        ", only the event queue: http://www.netbeans.org/download/4_1/javadoc/OpenAPIs/apichanges.html#actions-event-thread";

        if (r.async && !r.needsToBeSynchronous()) {
            Runnable r2 = new Runnable() {
                    public void run() {
                        r.doRun();
                    }
                };

            RP.post(r2);
        } else {
            r.run();
        }
    }
    
    /** Special class that can be passed to invokeAction and delegates
     * to correct values
     */
    private abstract static class ActionRunnable implements Action {
        final ActionEvent ev;
        final Action action;
        final boolean async;

        public ActionRunnable(ActionEvent ev, SystemAction action, boolean async) {
            this(ev, (Action)action, async);
        }
        public ActionRunnable(ActionEvent ev, Action action, boolean async) {
            this.ev = ev;
            this.action = action;
            this.async = async;
        }

        public static ActionRunnable create(ActionEvent ev, Action a, boolean async) {
            return new ActionRunnable(ev, a, async) {
                @Override
                protected void run() {
                    action.actionPerformed(ev);
                }
            };
        }

        public final boolean needsToBeSynchronous() {
            return "waitFinished".equals(ev.getActionCommand()); // NOI18N
        }

        public final void doRun() {
            ActionInvoker bridge = Lookup.getDefault().lookup(ActionInvoker.class);
            if (bridge != null) {
                bridge.invokeAction (this, ev);
            } else {
                this.actionPerformed(ev);
            }
        }

        protected abstract void run();

        public final void actionPerformed(ActionEvent e) {
            run();
        }

        public final void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        public final Object getValue(String key) {
            return action.getValue(key);
        }

        public final boolean isEnabled() {
            return action.isEnabled();
        }

        public final void putValue(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        public final void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException();
        }

        public final void setEnabled(boolean b) {
            throw new UnsupportedOperationException();
        }
    }
    // end of ActionRunnable
}
