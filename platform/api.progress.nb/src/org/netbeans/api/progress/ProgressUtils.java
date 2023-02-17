/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.progress;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.progress.spi.RunOffEDTProvider;
import org.netbeans.modules.progress.spi.RunOffEDTProvider.Progress2;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * Useful static methods. Most of methods were migrated to a non-swing class,
 * {@link BaseProgressUtils}. 
 * 
 * @author Tomas Holy
 * @since 1.16
 */
public final class ProgressUtils {
    private static final RunOffEDTProvider PROVIDER = getProvider();
    private static final int DISPLAY_DIALOG_MS = 9450;
    private static final int DISPLAY_WAIT_CURSOR_MS = 50;

    private ProgressUtils() {
    }

    private static RunOffEDTProvider getProvider() {
        RunOffEDTProvider p = Lookup.getDefault().lookup(RunOffEDTProvider.class);
        return p != null ? p : new Trivial();
    }

    /**
     * Runs operation out of event dispatch thread, blocks UI while operation is in progress. First it shows
     * wait cursor after <i>waitCursorAfter</i> elapses, if operation takes longer than <i>dialogAfter</i> a dialog with Cancel button is shown.
     * <p>
     * This method is supposed to be used by user invoked foreground actions, that are expected to run very fast in vast majority of cases.
     * However, in some rather rare cases (e.g. extensive IO operations in progress), supplied operation may need longer time. In such case
     * this method first displays wait cursor and if operation takes even more time it displays dialog allowing to cancel operation.
     * DO NOT use this method for operations that may take long time under normal circumstances!
     * @param operation operation to perform
     * @param operationDescr text shown in dialog
     * @param cancelOperation set to true if user canceled the operation
     * @param waitForCanceled true if method should wait until canceled task is finished (if it is not finished in 1s ISE is thrown)
     * @param waitCursorAfter time in ms after which wait cursor is shown
     * @param dialogAfter time in ms after which dialog with "Cancel" button is shown
     * @since 1.19
     * @deprecated Use {@link BaseProgressUtils}
     */
    @Deprecated
    public static void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorAfter, int dialogAfter) {
        BaseProgressUtils.runOffEventDispatchThread(operation, operationDescr, cancelOperation, waitForCanceled, waitCursorAfter, dialogAfter);
    }

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
     * @param waitCursorAfter amount of time, in milliseconds, after which wait
     *                        cursor is shown
     * @param dialogAfter amount of time, in milliseconds, after which dialog
     *                    is shown
     * 
     * @since 1.30
     * @deprecated Use {@link BaseProgressUtils}
     */
    @Deprecated
    public static void runOffEventThreadWithProgressDialog(
            final Runnable operation,
            final String dialogTitle, 
            final ProgressHandle progress, 
            final boolean includeDetailLabel,
            int waitCursorAfter,
            int dialogAfter)
    {
        BaseProgressUtils.runOffEventThreadWithProgressDialog(operation, dialogTitle, progress, includeDetailLabel, waitCursorAfter, dialogAfter);
    }

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
     * @param waitCursorAfter amount of time, in milliseconds, after which wait
     *                        cursor is shown
     * @param dialogAfter amount of time, in milliseconds, after which dialog
     *                    is shown
     * 
     * @since 1.30
     * @deprecated Use {@link BaseProgressUtils}
     */
    @Deprecated
    public static void runOffEventThreadWithCustomDialogContent(
            final Runnable operation,
            final String dialogTitle,
            final JPanel content,
            int waitCursorAfter,
            int dialogAfter)
    {
        if (PROVIDER instanceof Progress2) {
            Progress2 p = (Progress2) PROVIDER;
            p.runOffEventThreadWithCustomDialogContent(operation, dialogTitle, content, waitCursorAfter, dialogAfter);
        } else {
            BaseProgressUtils.runOffEventDispatchThread(operation, 
                    dialogTitle, 
                    new AtomicBoolean(false),
                    true, 
                    DISPLAY_WAIT_CURSOR_MS,
                    DISPLAY_DIALOG_MS);
        }
    }

    private static class Trivial implements RunOffEDTProvider {
        private static final RequestProcessor WORKER = new RequestProcessor(ProgressUtils.class.getName());

        @Override
        public void runOffEventDispatchThread(Runnable operation, String operationDescr, AtomicBoolean cancelOperation, boolean waitForCanceled, int waitCursorAfter, int dialogAfter) {
            if (SwingUtilities.isEventDispatchThread()) {
                Task t = WORKER.post(operation);
                t.waitFinished();
            } else {
                operation.run();
            }
        }
    }
}
