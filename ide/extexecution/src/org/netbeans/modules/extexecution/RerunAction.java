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
package org.netbeans.modules.extexecution;

import java.awt.event.ActionEvent;

import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor.RerunCallback;
import org.netbeans.api.extexecution.ExecutionDescriptor.RerunCondition;
import org.netbeans.api.extexecution.ExecutionService;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.InputOutput;


/**
 * The RerunAction is placed into the I/O window, allowing the user to restart
 * a particular execution context.
 *
 * @author Petr Hejl
 */
public final class RerunAction extends AbstractAction implements ChangeListener {

    private InputOutput parent;

    private ExecutionService service;

    private RerunCondition condition;

    private RerunCallback callback;

    private ChangeListener listener;

    public RerunAction() {
        setEnabled(false); // initially, until ready
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/extexecution/resources/rerun.png", false)); // NOI18N
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RerunAction.class, "Rerun"));
    }

    public void setParent(InputOutput parent) {
        synchronized (this) {
            this.parent = parent;
        }
    }

    public void setExecutionService(ExecutionService service) {
        synchronized (this) {
            this.service = service;
        }
    }

    public void setRerunCondition(RerunCondition condition) {
        synchronized (this) {
            if (this.condition != null) {
                this.condition.removeChangeListener(listener);
            }
            this.condition = condition;
            if (this.condition != null) {
                listener = WeakListeners.change(this, this.condition);
                this.condition.addChangeListener(listener);
            }
        }
        stateChanged(null);
    }

    public void setRerunCallback(RerunCallback callback) {
        synchronized (this) {
            this.callback = callback;
        }
    }

    public void actionPerformed(ActionEvent e) {
        setEnabled(false); // discourage repeated clicking

        ExecutionService actionService;
        RerunCallback actionCallback;
        InputOutput required;
        synchronized (this) {
            actionService = service;
            actionCallback = callback;
            required = parent;
        }

        if (actionService != null) {
            Future<Integer> task = Accessor.getDefault().run(actionService, required);
            if (actionCallback != null) {
                actionCallback.performed(task);
            }
        }
    }

    public void stateChanged(ChangeEvent e) {
        Boolean value = null;
        synchronized (this) {
            if (condition != null) {
                value = condition.isRerunPossible();
            }
        }

        if (value != null) {
            firePropertyChange("enabled", null, value); // NOI18N
        }
    }

    @Override
    public boolean isEnabled() {
        synchronized (this) {
            return super.isEnabled() && (condition == null || condition.isRerunPossible());
        }
    }

    public abstract static class Accessor {

        private static volatile Accessor accessor;

        public static void setDefault(Accessor accessor) {
            if (Accessor.accessor != null) {
                throw new IllegalStateException("Already initialized accessor");
            }
            Accessor.accessor = accessor;
        }

        public static Accessor getDefault() {
            if (accessor != null) {
                return accessor;
            }

            // invokes static initializer of ExecutionService.class
            // that will assign value to the DEFAULT field above
            Class c = ExecutionService.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException ex) {
                assert false : ex;
            }
            assert accessor != null : "The accessor field must be initialized";
            return accessor;
        }

        public abstract Future<Integer> run(ExecutionService service, InputOutput required);
    }

}
