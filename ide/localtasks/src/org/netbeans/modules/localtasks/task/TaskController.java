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

package org.netbeans.modules.localtasks.task;

import java.awt.Container;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

/**
 *
 * @author Ondrej Vrabec
 */
final class TaskController implements IssueController {
    private final LocalTask task;
    private final TaskPanel panel;
    private boolean opened;

    public TaskController (LocalTask task) {
        this.task = task;
        this.panel = new TaskPanel(task);
    }

    @Override
    public JComponent getComponent () {
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx("org.netbeans.modules.localtasks.editor.TaskPanel"); //NOI18N
    }

    @Override
    public void opened () {
        if (!opened) {
            opened = true;
            panel.opened();
            task.opened();
        }
    }

    @Override
    public void closed () {
        if (opened) {
            task.closed();
            panel.closed();
            opened = false;
        }
    }

    void refreshViewData () {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run () {
                panel.refreshViewData();
            }
        });
    }

    void modelStateChanged (boolean dirty) {
        panel.modelStateChanged(dirty);
    }

    void attachmentDeleted () {
        Mutex.EVENT.readAccess(new Runnable() {

            @Override
            public void run () {
                panel.attachmentDeleted();
            }
        });
    }

    @Override
    public boolean saveChanges() {
        return panel.saveChanges();
    }

    @Override
    public boolean discardUnsavedChanges() {
        return panel.discardUnsavedChanges();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        task.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        task.removePropertyChangeListener(l);
    }

    @Override
    public boolean isChanged() {
        return task.hasUnsavedChanges() || task.hasUnsavedAttachments();
    }
    
    void taskDeleted () {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {

            @Override
            public Void run () {
                Container tc = SwingUtilities.getAncestorOfClass(TopComponent.class, panel);
                if (tc instanceof TopComponent) {
                    ((TopComponent) tc).close();
                }
                return null;
            }
        });
    }
}
