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
package org.netbeans.modules.masterfs.ui.suspend;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(
    category = "System",
    id = "org.netbeans.modules.masterfs.ui.suspend.PauseAction"
)
@ActionRegistration(
    displayName = "#CTL_PauseAction",
    lazy = false
)
@ActionReference(path = "Toolbars/Memory", position = 15000)
@Messages({
    "CTL_PauseAction=Pause I/O Checks",
    "# {0} - number of pending events",
    "MSG_Resume=Resume (pending events: {0})"
})
public final class PauseAction extends AbstractAction implements Presenter.Toolbar {
    private static final RequestProcessor RP = new RequestProcessor("Pause Action"); // NOI18N
    private static final String SUSPEND_COUNT_PROPERTY = "org.netbeans.io.suspend".intern(); // NOI18N
    private static final Preferences PREFS = NbPreferences.forModule(PauseAction.class);

    public PauseAction() {
        putValue("iconBase", "org/netbeans/modules/masterfs/ui/suspend/pause.png"); // NOI18N
        putValue(SHORT_DESCRIPTION, Bundle.CTL_PauseAction());
        putValue(LONG_DESCRIPTION, Bundle.CTL_PauseAction());
        int initialValue = PREFS.getInt(SUSPEND_COUNT_PROPERTY, 0);
        synchronized (SUSPEND_COUNT_PROPERTY) {
            System.setProperty(SUSPEND_COUNT_PROPERTY, "" + initialValue); // NOI18N
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isSuspended()) {
            resume();
        } else {
            suspend(1);
        }
    }

    @Override
    public Component getToolbarPresenter() {
        final AbstractButton suspendIOButton = new JToggleButton();
        Actions.connect(suspendIOButton, this);
        updateButton(suspendIOButton);
        final class Controller implements Runnable, ActionListener {
            private RequestProcessor.Task updateTask;
            
            Controller() {
                updateTask = RP.create(this);
                scheduleUpdate(0);
            }
            
            @Override
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    EventQueue.invokeLater(this);
                    return;
                }
                if (!suspendIOButton.isShowing()) {
                    return;
                }
                updateButton(suspendIOButton);
                scheduleUpdate(0);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                updateButton(suspendIOButton);
                run();
                scheduleUpdate(100);
            }

            private void scheduleUpdate(int time) {
                if (time == 0) {
                    time = 1500;
                }
                updateTask.schedule(time);
            }
        }
        Controller c = new Controller();
        suspendIOButton.addActionListener(c);
        
        return suspendIOButton;
    }
    
    static final void updateButton(AbstractButton btnResume) {
        if (isSuspended()) {
            int pending = Integer.getInteger("org.netbeans.io.pending", 0); // NOI18N
            btnResume.setText(Bundle.MSG_Resume(pending));
            btnResume.setSelected(true);
        } else {
            btnResume.setText(null);
            btnResume.setSelected(false);
        }
    }

    static boolean isSuspended() {
        return Integer.getInteger(SUSPEND_COUNT_PROPERTY, 0) > 0;
    }

    static void suspend(int delta) {
        int prev;
        synchronized (SUSPEND_COUNT_PROPERTY) {
            prev = Integer.getInteger(SUSPEND_COUNT_PROPERTY, 0);
            prev += delta;
            System.setProperty(SUSPEND_COUNT_PROPERTY, "" + prev);
            SUSPEND_COUNT_PROPERTY.notifyAll();
        }
        PREFS.putInt(SUSPEND_COUNT_PROPERTY, prev);
    }

    static void resume() {
        synchronized (SUSPEND_COUNT_PROPERTY) {
            System.setProperty(SUSPEND_COUNT_PROPERTY, "" + 0);
            SUSPEND_COUNT_PROPERTY.notifyAll();
        }
        PREFS.putInt(SUSPEND_COUNT_PROPERTY, 0);
    }
}
