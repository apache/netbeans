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

package org.netbeans.lib.profiler.client;

import org.netbeans.lib.profiler.wireprotocol.Command;


/**
 * A utility interface, used to handle (by displaying things in GUI) various app status changes.
 *
 * @author  Misha Dmitriev
 */
public interface AppStatusHandler {
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    /**
     * An interface for a dialog that displays a message and a Cancel button.
     * The intended usage is in situations when some action is done in background, and the user should be able to
     * interrupt it at any moment.
     * The dialog is displayed using display(). Then the status of the Cancel button should be polled periodically
     * using cancelPressed() method, and finally the dialog can be closed using close(). Note that display() inevitably
     * blocks the thread that called it, so it should be called in a thread separate from the one in which the background
     * action is performed.
     */
    public static interface AsyncDialog {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void close();

        /**
         * This method is called to display the asynchronous wait dialog. It should block
         * until the user explicitely cancels or method AsyncDialog.close is called
         */
        public void display();
    }

    /**
     * A utility class, used to handle (by displaying things in GUI and by updating some parent class internal variables)
     * commands coming from the server.
     */
    public static interface ServerCommandHandler {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void handleServerCommand(Command cmd);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public AsyncDialog getAsyncDialogInstance(String message, boolean showProgress, Runnable cancelHandler);

    // The following methods should display messages asynchronously, i.e. they shouldn't block the current
    // thread waiting for the user pressing OK.
    public void displayError(String msg);

    // These 3 methods SHOULD wait for the user to press ok, since they may be used in a sequence of displayed
    // panels, and the next one shouldn't be displayed before the previous one is read and understood.
    public void displayErrorAndWaitForConfirm(String msg);

    public void displayErrorWithDetailsAndWaitForConfirm(String shortMsg, String detailsMsg);

    public void displayNotification(String msg);

    public void displayNotificationAndWaitForConfirm(String msg);

    public void displayNotificationWithDetailsAndWaitForConfirm(String shortMsg, String detailsMsg);

    public void displayWarning(String msg);

    public void displayWarningAndWaitForConfirm(String msg);

    /** Called each time profiling results will become available for the first time using current instrumentation */
    public void resultsAvailable();

    public void takeSnapshot();

    /**
     * Called from the profiler engine in case the waiting for reply timed out.
     * The profiler can decide (e.g. by asking the user) whether to keep waiting or cancel the profiling.
     *
     * @return true to keep waiting for reply, false to cancel profiling
     */
    boolean confirmWaitForConnectionReply();

    void handleShutdown();

    /**
     *  Called from the engine to signal that the profiler should not be getting results
     *  because some internal change is in progress.
     */
    void pauseLiveUpdates();

    /**
     *  Called from the engine to signal that it is again safe to start getting results
     */
    void resumeLiveUpdates();
}
