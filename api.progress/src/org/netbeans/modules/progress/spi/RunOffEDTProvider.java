/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.progress.spi;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;

/**
 * Interface for ProgressUtils.runOffEventDispatchThread() methods
 * @author Tomas Holy
 * @since org.netbeans.api.progress/1 1.18
 */
public interface RunOffEDTProvider {

    void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorAfter, int dialogAfter);
    /**
     * Interface all RunOffEDTProviders should implement, which allows for
     * blocking the main window and showing a progress bar in a dialog while
     * executing a runnable.  If not present, ProjectUtils will delegate to
     * runOffEventDispatchThread(), but this provides an inferior user experience
     * and sometimes useless cancel button.
     *
     * @since 1.19
     */
     public interface Progress extends RunOffEDTProvider {

        /**
         * Show a modal progress dialog that blocks the main window while running
         * a background process.  This call should block until the work is
         * completed.
         * <p/>
         * The resulting progress UI should show a cancel button if the passed
         * runnable implements org.openide.util.Cancellable.
         *
         * @param operation A runnable that needs to be run with the UI blocked
         * @param handle A progress handle that will be updated to reflect
         * the progress of the operation
         * @param showDetails If true, a label should be provided in the progress
         * dialog to show detailed progress messages
         */
        public void showProgressDialogAndRun(Runnable operation, ProgressHandle handle, boolean includeDetailLabel);

        /**
         * Show a modal progress dialog that blocks the main window while running
         * a background process.  This call should block until the work is
         * completed.
         * <p/>
         * The resulting progress UI should show a cancel button if the passed
         * runnable implements org.openide.util.Cancellable.
         *
         * @param <T> The type of the return value
         * @param toRun A ProgressCallable which will be passed a progress handle
         * on a background thread, can do its work and (optionally) return a value
         * @param displayName The display name of the work being done
         * @param includeDetailLabel Show the detail levels.  Set to true if the
         * caller will use ProgressHandle.progress (String, int) to provide
         * detailed progress messages
         * @return The result of the call to ProgressRunnable.call()
         */
        public <T> T showProgressDialogAndRun(ProgressRunnable<T> toRun, String displayName, boolean includeDetailLabel);

        /**
         * Show a modal progress dialog that blocks the main window while running
         * a background process.  This call should block until the work is
         * started, and then return a Future which can be monitored for completion
         * or cancelled.
         * <p/>
         * The resulting progress UI should show a cancel button if the passed
         * runnable implements org.openide.util.Cancellable.
         *
         * @param toRun The operation to run
         * @param handle A progress handle
         * @param includeDetailLabel Whether or not to include a detail label
         * @return A future which can be cancelled
         */
        public <T> Future<T> showProgressDialogAndRunLater (ProgressRunnable<T> toRun, ProgressHandle handle, boolean includeDetailLabel);
    }
        
    public interface Progress2 extends Progress {
        
        /**
         * Runs operation out of the event thread, blocking the whole UI. When
         * operation takes more than 1s, the method first displays wait cursor.
         * If operation will not end in 3s interval, modal dialog with
         * progress is shown up.
         * If operation is marked with {@link org.openide.util.Cancellable}
         * interface, cancel button is part of dialog and can be used 
         * to interrupt the operation.
         * 
         * @param operation  task to perform in the background
         * @param dialogTitle dialog title
         * @param progress  progress handle. Do not invoke any methods before
         *                  passing to this method. Start/progress/finish it
         *                  only in {@code operation}
         * @param includeDetailLabel  show progress detail label in the dialog
         * @param waitCursorAfter amount of time, in milliseconds, after which 
         *                        wait cursor is shown
         * @param dialogAfter amount of time, in milliseconds, after which 
         *                    dialog is shown
         * 
         * @since 1.30
         */
        public void runOffEventThreadWithProgressDialog(
            final Runnable operation, 
            final String operationDescr,
            final ProgressHandle progress, 
            final boolean includeDetailLabel,
            int waitCursorAfter,
            int dialogAfter);


        /**
         * Runs operation out of the event thread, blocking the whole UI. When
         * operation takes more than 1s, the method first displays wait cursor.
         * If operation will not end up in 3s interval, modal dialog with
         * {@code content} panel is shown.
         * If operation is marked with {@link org.openide.util.Cancellable} 
         * interface, cancel button is part of dialog and can be used to 
         * interrupt the operation.
         * 
         * @param operation  task to perform in the background
         * @param dialogTitle dialog title
         * @param content  panel to be shown in the dialog
         * @param waitCursorAfter amount of time, in milliseconds, after which 
         *                        wait cursor is shown
         * @param dialogAfter amount of time, in milliseconds, after which 
         *                    dialog is shown
         * 
         * @since 1.30
         * @deprecated interface SwingProgress should be used instead.
         */
        public void runOffEventThreadWithCustomDialogContent(
            final Runnable operation,
            final String dialogTitle,
            final JPanel content,
            int waitCursorAfter,
            int dialogAfter);
        }
}
