/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
