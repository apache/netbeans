/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.utils.ui;

import java.awt.Window;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.Cancellable;

/**
 * Utility class for displaying a modal Dialog (effectively blocking (but not
 * freezing) UI) while running a task in background.
 *
 * Deprecated. See {@link org.netbeans.api.progress.ProgressUtils}
 *
 */
@Deprecated
public final class ModalMessageDlg {

    private ModalMessageDlg() {
    }

    /**
     * Runs operation out of event dispatch thread, blocks UI (with a modal
     * dialog) while operation is in progress.
     *
     * <p>If operation is completed in less than 1 second, then UI is just
     * freezes for that second and no any dialog is displayed. After one second
     * delay cursor is changes to 'waiting', after another second delay a modal
     * dialog is displayed.</p>
     *
     * If canceler is not null, then a 'Cancel' button is a part of the
     * displayed dialog. Once 'Cancel' button is pressed, the operation is
     * considered to be canceled and no postEDTTask will be invoked.
     *
     * <p> This method is thread-safe, and will block until the operation has
     * completed, regardless of what thread calls this method. </p>
     *
     * @param parent <b>Not used</b>
     * @param workTask a Runnable to start in non-EDT thread. Unlike
     * {@link ProgressUtils}'s operation this argument is never tested for the
     * {@link Cancellable} interface. Use <b>canceler</b> for the task
     * interruption facility.
     * @param postEDTTask a task to run in the EDT after the <b>workTask</b> is
     * processed. <code>null</code> is allowed. <br><b>Note:</b> The postEDTTask
     * is called if <code>workTask</code>has not been canceled by pressing the
     * cancel button.
     * @param canceler if not <code>null</code> a cancel button is displayed
     * while <b>workTask</b> is running. <br/> Note that once the cancel button
     * is pressed, no <b>postEDTTask</b> will be called.
     * @param title title of a dialog.
     * @param message message to display. Could be <code>null</code>. In this
     * case <b>title</b> is used.
     *
     */
    public static void runLongTask(final Window parent, final Runnable workTask,
            final Runnable postEDTTask, final Cancellable canceler,
            String title, String message) {
        run(parent, title, message, new LongWorkerImpl(workTask, postEDTTask), canceler);
    }

    /**
     * See
     * {@link #runLongTask(java.awt.Window, java.lang.Runnable, java.lang.Runnable, org.openide.util.Cancellable, java.lang.String, java.lang.String)}
     *
     * @param parent - Not Used
     * @param title - title of a dialog
     * @param message - message to display inside a dialog or null
     * @param workTask - a task with an on-EDT continuation
     * @param canceler - a canceler to be invoked if user presses Cancel button
     */
    public static void runLongTask(final Window parent, final String title, final String message,
            final LongWorker workTask, final Cancellable canceler) {

        run(parent, title, message, workTask, canceler);
    }

    public interface LongWorker {

        void doWork();

        void doPostRunInEDT();
    }

// <editor-fold defaultstate="collapsed" desc="Private Code">
    private static void run(
            final Window parent,
            final String title,
            final String message,
            final LongWorker worker,
            final Cancellable canceler) {
        Worker r = (canceler != null) ? new CancellableWorker(worker, canceler) : new Worker(worker);
        ProgressUtils.runOffEventThreadWithCustomDialogContent(r, title, new ModalMessageDlgPane(message == null ? title : message), 1000, 2000);
    }

    private static class Worker implements Runnable {

        private final LongWorker worker;

        private Worker(LongWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            worker.doWork();
            if (!wasCancelled()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        worker.doPostRunInEDT();
                    }
                });
            }
        }

        public boolean wasCancelled() {
            return false;
        }
    }

    private static class CancellableWorker extends Worker implements Cancellable {

        private final Cancellable canceler;
        private final AtomicBoolean wasCancelled = new AtomicBoolean(false);

        private CancellableWorker(LongWorker worker, Cancellable canceler) {
            super(worker);
            this.canceler = canceler;
        }

        @Override
        public boolean cancel() {
            wasCancelled.set(true);
            return canceler.cancel();
        }

        @Override
        public boolean wasCancelled() {
            return wasCancelled.get();
        }
    }

    private static final class LongWorkerImpl implements LongWorker {

        private final Runnable main;
        private final Runnable postEDT;

        public LongWorkerImpl(Runnable main, Runnable postEDT) {
            this.main = main;
            this.postEDT = postEDT;
        }

        @Override
        public void doWork() {
            if (main != null) {
                main.run();
            }
        }

        @Override
        public void doPostRunInEDT() {
            if (postEDT != null) {
                postEDT.run();
            }
        }
    }
    // </editor-fold>
}
