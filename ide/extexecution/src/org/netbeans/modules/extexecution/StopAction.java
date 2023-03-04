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
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * The StopAction is placed into the I/O window, allowing the user to stop
 * execution.
 *
 * @author Petr Hejl
 */
public final class StopAction extends AbstractAction {

    private Future<?> task;

    public StopAction() {
        setEnabled(false); // initially, until ready
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/extexecution/resources/stop.png", false)); // NOI18N
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(StopAction.class, "Stop"));
        putValue("OUTPUT_ACTION_TYPE", "stop"); // see bug 250245       //NOI18N
    }

    public void setTask(Future<?> task) {
        synchronized (this) {
            this.task = task;
        }
    }

    public void actionPerformed(ActionEvent e) {
        setEnabled(false); // discourage repeated clicking

        Future<?> actionTask;
        synchronized (this) {
            actionTask = task;
        }

        if (actionTask != null) {
            actionTask.cancel(true);
        }
    }

}
