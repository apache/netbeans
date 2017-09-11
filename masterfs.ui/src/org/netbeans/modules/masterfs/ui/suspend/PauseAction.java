/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
